JVisuals : JMIDI{
	const maxEvents = 1024;
	var listener, <freePointers, <>audioMapFunctions, <>bRemoveFunction = false;
	var brightness, <alpha=0, <>maskBrightness=0;
	var bgColor = #[0,0,0];
	var <>bShiftPressed = false;
	classvar <>defaultPort = 6061;
	var <>netAddr;
	var <size = #[1920, 1080];
	var <w = 1920;
	var <h = 1080;
	var <>frameRate = 60;
	var <>guiWindow = nil;
	var <>brightnessSlider = nil, <>alphaSlider=nil, <>maskBrightnessSlider=nil;
	var <> mapValues;
	var <> shaderPath = "/home/jildert/of_v0.11.2_linux64gcc6_release/addons/ofxJVisuals/libs/shaders/";
	*new{
		|netAddr_|
		^super.new.init(netAddr_)
	}
	init {
		|netAddr_|
		netAddr = netAddr_;
		"Visuals client is created, using NetAddr w/ port ".post; netAddr.port.postln;
		freePointers = (1..(maxEvents-1));
		audioMapFunctions = Array.newClear(16);
		presets = Array.fill(5, {Array.newClear(64)});
		"Initializing MIDI...".postln;
		this.initMIDI();
		this.initMapValues
	}
	setSize {
		|si = #[1280,800]|
		w = si[0]; h = si[1];
		size = si;
	}
	setAlpha{
		|a=127|
		alpha = a;
		netAddr.sendMsg("/setAlpha", alpha);
		if(this.alphaSlider != nil, {{this.alphaSlider.value_(alpha.asInteger)}.fork(AppClock)});
	}
	getAllEvents {
		listener = OSCFunc({
			|msg, time, addr, recvPort|
			msg.postln;
			{
				var w = Window.new("Events", Rect(0, 0, 400, 400)).front;
				// List all nodes, per layer?
			}.fork(AppClock);
		}, '/allEvents', recvPort: 6063).oneShot; // once only
		netAddr.sendMsg("/getInfo", 0); // Get all events
		"Send request".postln;
	}
	// initMidi { // Deprecated? Maybe try to run JMIDI.initMIDI()?
	// ("/Users/jildertviet/Desktop/Visuals/midiMapping.scd").load;
	// }
	setFreePointers {
		|new|
		freePointers = new;
	}
	getFreeAddress {
		var freeID;
		if(freePointers.size == 0, {
			OSCFunc({
				|msg, time, addr, recvPort|
				msg.removeAt(0);
				freePointers = msg;
				("Received " ++ freePointers.size ++ " new free pointers").postln;
				freePointers.postln;
			}, '/freePointers', recvPort: 6063).oneShot;
			netAddr.sendMsg("/getFreePointers", 0, 6063); // Get free pointers
			freeID = nil;
			"VISUALIZER FULL!!!".postln;
		}, {
			freeID = freePointers.removeAt(0);
			// freePointers.size.postln;
		});
		^freeID;
	}
	schedule {
		|i, func|
		audioMapFunctions[i] = {
			inf.do{
				|i|
				func.value();
				(0.033333333333).wait; // 30 fps
			}
		}.fork;
	}
	clearAll {
		// scheduledFunctions.do{
		// |func|
		// func.stop;
		// };
		// REMOVE!!!
	}
	stop {
		|i|
		audioMapFunctions[i].stop;
	}
	clearAPCNotes{
		apcFunctions.do{
			|function, i|
			if(function != nil, {~mOut.noteOn(16, i, 0)});
		};
		apcFunctions = Array.newClear(64);
	}
	removeFunction {
		|i|
		apcFunctions[i] = nil;
		~mOut.noteOn(16, i, 0);
	}
	resetAndStartOF{
		"WIP".postln;
	}
	killAll{
		netAddr.sendMsg("/killAll");
		freePointers = Array.series(512-1, 1, 1); // Omit 0
	}
	setBrightness{
		|b = 255|
		brightness = b;
		netAddr.sendMsg("/setMasterBrightness", b.asInteger);
		// this.guiWindow.postln;
		if(this.brightnessSlider != nil, {{this.brightnessSlider.value_(b.asInteger)}.fork(AppClock)});
	}
	setMaskBrightness{
		|val=0|
		maskBrightness = val;
		netAddr.sendMsg("/setMaskBrightness", maskBrightness.asInteger);
		if(this.maskBrightnessSlider != nil, {{this.maskBrightnessSlider.value_(maskBrightness.asInteger)}.fork(AppClock)});
	}
	setBackground{
		|color=nil|
		if(color.isNil,{
			var colorPicker = ColorPicker.new(bgColor, {|result| this.setBackground(result)}); ~tempWindow  = Window.new.front; ~tempWindow.close;
			colorPicker.value_(bgColor);
		}, {
			bgColor = color;
			if(guiWindow != nil, {guiWindow.asView.children[1].states_([["", Color.white, bgColor]])});
			color = color.toJV;
			color.postln;
			netAddr.sendMsg("/setBackground", color[0], color[1], color[2]);
		});
	}
	resetCam{
		netAddr.sendMsg("/resetCam");
	}
	camTilt{
		|d=1.0|
		netAddr.sendMsg("/camTilt", d);
	}
	camPan{
		|d=1.0|
		netAddr.sendMsg("/camPan", d);
	}
	camRoll{
		|d=1.0|
		netAddr.sendMsg("/camRoll", d);
	}
	camRotateAround{
		|d=1.0, axis=#[1,0,0]|
		netAddr.sendMsg("/camRotateAround", d, axis[0], axis[1], axis[2]);
	}
	receiveRBWindow{
		"/Users/jildertviet/Desktop/Visuals/receiveFromRB.scd".load;
	}
	drawNegativeLayer{
		|value=true|
		netAddr.sendMsg("/setBDrawNegative", value);
	}
	openEditMode {
		|note|
		var doc;
		var theFunction = apcFunctions[note];
		doc = Document("", "(\n~v.apcFunctions["++note++"] = " ++ theFunction.asTextArchive ++ "\n)");
		doc.front;
		doc.promptToSave = false;
		doc.selectLine(2);
	}
	startSequenceAPC {
		var offset = 0.21; // For MIDI-controller lag
		"startSequenceAPC".postln;
		TempoClock.default.schedAbs(
			TempoClock.default.beats.ceil,
			{ |beat|
				var apcNoteIndex = (((beat % 4)*2)+1) % 8;
				var prevNote = (apcNoteIndex - 1) % 8;
				// beat.postln;
				{
					(offset * TempoClock.default.beatDur).wait;
					~mOut.noteOn(16, 56 + apcNoteIndex, 5);
					~mOut.noteOn(16, 56 + prevNote, 0);
				}.fork;
				0.5 // Time to wait?
		});
	}
	startClickTrack {
		TempoClock.default.schedAbs(TempoClock.default.beats.ceil, {|beat|(beat%8); {EnvGen.kr(Env.perc(0.01, 0.01), doneAction: 2); HPF.ar(WhiteNoise.ar([0.6,0.3].wrapAt(beat)!2), ([10000]++((5000!3))).wrapAt(beat * 2))}.play; 0.5});
	}
	testLR {
		var t, j;
		t = JText.new(3);
		t.create();
		t.setColor([255,255,255,255]);
		t.setVal("text", "L");
		t.setLoc([w * -0.25, 0]);
		// t.addEnv("x", [1000 + 1000.rand, 100, 1000 + 800.rand] * 2, [-500, w, w, 100], false);
		t.addEnv("brightness", [500, 5000, 1000 + 200.rand], [0, 255, 255, 0] * 1.0.rand.range(0.4, 1.0),true);

		j = JRectangle.new(2);
		j.create();
		j.setLoc([w * -0.5, h* -0.5]);
		j.setSize([w * 0.5, h]);
		j.setColor([255, 0, 0, 255]);
		j.addEnv("brightness", [500, 5000, 1000 + 200.rand], [0, 255, 255, 0],true);

		t = JText.new();
		t.create();
		t.setColor([255,255,255,255]);
		t.setVal("text", "R");
		t.setLoc([w * 0.25, 0]);
		// t.addEnv("x", [1000 + 1000.rand, 100, 1000 + 800.rand] * 2, [-500, w, w, 100], false);
		t.addEnv("brightness", [500, 5000, 1000 + 200.rand], [0, 255, 255, 0] * 1.0.rand.range(0.4, 1.0),true);

		j = JRectangle.new(2);
		j.create();
		j.setLoc([0, h* -0.5]);
		j.setSize([w * 0.5, h]);
		j.setColor([0, 255, 0, 255]);
		j.addEnv("brightness", [500, 5000, 1000 + 200.rand], [0, 255, 255, 0], true);
	}
	setCamEnabled{
		|state=true|
		netAddr.sendMsg("/setCamEnabled", state);
	}
	openTempoTapper{
		~window = Window.new("Tap tempo", Rect(200, 200, 300, 100)).front;
		EZText.new(~window, Rect(0, 0, 300, 20),initVal: "Press space for tapping (8x) and 's' for sync").enabled_(false);
		~difSamples = List.newClear(8);
		~difSamplesWritePos = 0;
		~lastPressedNum = 0;
		~dif = 1;
		~window.view.keyDownAction = { |doc, char, mod, unicode, keycode, key|
			if(unicode == 32,{
				//     [doc, char, mod, unicode, keycode, key].postln;
				var d = Date.getDate.rawSeconds - ~lastPressedNum;
				d.postln;
				if(d < 4, {~difSamples.wrapPut(~difSamplesWritePos, d).postln; ~difSamplesWritePos = ~difSamplesWritePos + 1; ~dif = d}, {~difSamplesWritePos = 0});
				~lastPressedNum = Date.getDate.rawSeconds;
				~dif.postln;
			});
			if(unicode == 115, {
				if(~difSamplesWritePos > ~difSamples.size, {
					TempoClock.default.tempo = 1/~difSamples.mean; TempoClock.default.beats = TempoClock.default.beats.round(8).postln
				},{
					TempoClock.default.tempo = 1/~dif; TempoClock.default.beats = TempoClock.default.beats.round(8).postln
				});

			});
		};
	}
	listShaders{
		var p = PathName.new(shaderPath);
		p.entries.do{
			|e|
			if(e.extension == "frag", {
				e.fileNameWithoutExtension.postln;
			});
		};
	}
}

+ Function{
	mapMidi{ // Call this on a function, like {"hi".postln}.mapMidi, and then press a MIDI button on the APC mini
		|val|
		this.postln;
		"Function saved, check ~functionToSave".postln;
		~v[0].functionToSave = this;
		~bListenForAPC = true;
	}
	mapTo { // Map a midi by id, like {"hi".postln}.mapTo(0), maps it to the first button
		|i|
		~v[0].apcFunctions[i] = this;
		~mOut.noteOn(16, i, 1);
	}
	mapToCC{ // Same, but with CC
		|i|
		~v[0].cc[i] = this;
		{0.001.wait; ~mOut.noteOn(16, 64+i, 1)}.fork;
	}
}

+ Array{
	mapMidi{
		~v[0].bListenForOnOff = true;
		~v[0].functionToSave = this;
	}
}