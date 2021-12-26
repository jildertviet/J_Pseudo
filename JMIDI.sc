/*
Class that handles the communication with MIDI devices
For the current setup: APC Mini: 9 CC sliders and 64 buttons + more buttons (shift, etc...)
And a 5 CC (0-4) ESP32S2_midi device of own making
*/

JMIDI {
	var <>apc, j_midi;
	var <>ccFunction;
	var <>apcFunctions;
	var <>cc;
	var <>presets, <>currentPreset = 0;
	var <>apc = nil;
	var <>j_midi = nil; // MIDI controllers
	var uids = #[844241712, -402664769]; // APC and JMIDI
	var <>bListenForOnOff = false;
	var <>functionToSave;
	var <>connectedDeviceNames;

	initMIDI {
		connectedDeviceNames = List.new(2);
		if(MIDIClient.initialized == false, {
			"Init MIDIClient".postln;
			MIDIClient.init();
			"MIDIClient initialized".postln;
		});

		this.connectAPC();
		this.connectJMIDI();
		MIDIIn.connectAll;

		apcFunctions = Array.newClear(64);
		cc = Array.fill(9, {{"Empty function".postln;}});
		ccFunction = Array.fill(5, {Array.newClear(16)}); // 9 for APC + 5 for J_MIDI

		~bRecord = false;

		// ~mOut = MIDIOut.newByName("APC MINI", "APC MINI");
		// 1.) Get array of MIDI destinations 2.) translate the devices to -1 or 1 (if it's the APC) 3.) Remove all negative numbers 4.) Left with the index of the APC in the array
		~mainMidiFunc.free;
		~mainMidiFunc = MIDIFunc.noteOn({ // For saving the functions
			|val, note|
			if(~bListenForAPC == true, {
				apcFunctions[note] = functionToSave;
				~mOut.noteOn(16, note, 1);
				~bListenForAPC = false},
			{
				if(~v[0].bShiftPressed == true, {
					~v[0].openEditMode(note);
				}, {
					if(~v[0].bListenForOnOff == true, {
						~v[0].apcFunctions[note] = ~v[0].functionToSave ++ false; // [{ON}, {OFF}, false]
						~mOut.noteOn(16, note, 5);
						~v[0].bListenForOnOff = false;
					}, {
						if(~v[0].bRemoveFunction == false, { // Is this a trigger-noteOn, of a 'remove function'-noteOn
							if(~v[0].apcFunctions[note].size != 0, { // Is the function to be triggered an Array (i.e. onOff-state), or a one-shot?
								if(~v[0].apcFunctions[note][2] == false, { // Is the onOff-state function running?
									~v[0].apcFunctions[note][0].value();
									~v[0].apcFunctions[note][2] = true;
									~mOut.noteOn(16, note, 6); // Set to blink
								}, {
									~v[0].apcFunctions[note][1].value();
									~v[0].apcFunctions[note][2] = false;
									~mOut.noteOn(16, note, 5); // Static orange
								});
							}, {
								if(~bRecord, {
									var currentPreset = ~v[0].currentPreset;
									currentPreset.postln;
									TempoClock.default.schedAbs(TempoClock.default.beats.round(0.5).postln, {~v[0].presets[currentPreset][note].value(); 4});
								}, {
									~v[0].apcFunctions[note].value(); // One-shot
								})
							});
						}, {
							~v[0].removeFunction(note);
						});
					});
				});
			});
		}, (0..61));

		// Should use apc.addFunc? To link this function to one specific controller?
		// MIDIIn.addFuncTo(\noteOn, ~noteOn);

		MIDIFunc.noteOn({~bRecord = true; ~mOut.noteOn(16, 83, 1);}, 83); // "Record", Scene launch 2nd
		MIDIFunc.noteOff({~bRecord = false; ~mOut.noteOn(16, 83, 0);}, 83);
		MIDIFunc.noteOn({~v[0].bShiftPressed = true}, 98);
		MIDIFunc.noteOff({~v[0].bShiftPressed = false}, 98);
		MIDIFunc.noteOn({|val, note| ~v[0].loadPreset(4-(note-85)); },(85..89));
		MIDIFunc.noteOn({TempoClock.default.clear;  ~v[0].startSequenceAPC;}, 82); // Clear scheduled tasks "Clip stop"
		~mOut.noteOn(16, 82, 1);

		this.loadPreset(0);
		~cc.free;
		~cc = MIDIFunc.cc({
			|val, note|
			var i = note - 48; // Range 0-8
			cc[i].value(val);
		},(48..56), srcID: uids[0]); // APC MINI
		MIDIFunc.cc({
			|val, note|
			var i = note + 9; // 9 -13
			// (	"J_MIDI"+[val, note, i]).postln;
			~v.cc[i].value(val);
		},(0..5), srcID: uids[1]); // J_MIDI

		cc[13] = {|val| ~v[0].setBrightness((val*2));}; // J_MIDI
		cc[12] = {|val| this.setAlpha(val*2)}; // J_MIDI
		cc[11] = {|val| this.setMaskBrightness(val*2)}; // J_MIDI
		cc[10] = {|val| if(abs(val - 64) > 30, {
			if(val < 64, {
				"Move down".postln;
			}, {
				"Move up".postln;
			});
		}, {
			"Stop movement / neutral position".postln;
		}
		)};
		cc[9] = {|val| if(abs(val - 64) > 30, {
			if(val < 64, {
				"Move L".postln;
			}, {
				"Move R".postln;
			});
		}, {
			"Stop movement / neutral position".postln;
		}
		)};

	}
	loadPreset{
		|i=0|
		currentPreset = i;
		apcFunctions = presets[i];
		cc = ccFunction[i];

		5.do{ |j| if(j!=i, {~mOut.noteOn(16, 85+j, 0)})}; // All other leds out
		~mOut.noteOn(16, 89-i, 1); // Selected preset to light
		cc.do{
			|f, i|
			if(f != nil, {~mOut.noteOn(16, 64+i, 1)}, {~mOut.noteOn(16, 64+i, 0)});
		};
		cc[8] = {|val| ~v[0].setBrightness(val*2)};
		{
			apcFunctions.do{
				|f, i|
				if(i < 56, {
					if(f != nil, {
						if(f.size != 0, {
							if(f[2] == true, {
								~mOut.noteOn(16, i, 6); // Orange
							}, {
								~mOut.noteOn(16, i, 5); // Orange
							});
						}, {
							~mOut.noteOn(16, i, 1); // Green
						});
					}, {
						~mOut.noteOn(16, i, 0); // Off
					});
				});
				0.001.wait;
			}

		}.fork;
	}
	connectAPC{
		var midiOutID = nil;
		if(MIDIClient.sources.size > 0, {
		this.apc = MIDIClient.sources.select({|e| if(e.uid == uids[0], {true}, {false})})[0]; // Returns an array, so take index 0 for the string. If [] result is nil
		if(this.apc != nil, {connectedDeviceNames.add(this.apc.device)});
		midiOutID = MIDIClient.destinations.collect({|e, i| if(e.uid == 1801513190, {i}, {-1})}).select({|e| if(e >= 0, {true}, {false})}).at(0);
		});
		if(midiOutID != nil, {~mOut = MIDIOut(midiOutID)}, {~mOut = MIDIOut(0)}); // Use the first MIDI port as output? Shouldn't use one at all :(
	}
	connectJMIDI{
		if(MIDIClient.sources.size > 0, {
			this.j_midi= MIDIClient.sources.select({|e| if(e.uid == uids[1], {true}, {false})})[0];
			if(this.j_midi!= nil, {connectedDeviceNames.add(this.j_midi.device)});
		});
	}
}