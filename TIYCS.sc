TIYCS{
	var <> scenes, <>img, <>size, <>frameDur, <>frameRate;
	var <>scenesDict, t;
	var <>routines;
	var <>screens;
	var <>counter;
	var <>flyHeight;
	var <>noteOn, <>noteOff, <>cc;
	var <>bingo;
	var <>onSwitch;
	var <>buffer, <>captainSample;
	var <> blackToggle = 0;
	var <> childWindows;
	var <> bShiftPressed = false;
	*new{
		|ip="127.0.0.1"|
		^super.new.init(ip);
	}
	init{
		|ip|
		if(ip.isString, {
			ip = ip!1;
		});
		ip = ip.asArray;
		screens = [
			NetAddr(ip.wrapAt(0), 5000),
			NetAddr(ip.wrapAt(1), 5001),
			NetAddr(ip.wrapAt(2), 5002),
		];

		size = [1280, 800];
		this.setFramerate(30);
		this.initBingo();
		noteOn = List.new();
		noteOff = List.new();
		cc = List.new();
		scenes = List.new();
		routines = List.new();
		this.initDefaultMidi();
		buffer = Buffer.alloc(Server.local, 400*3);
		captainSample = Buffer.readChannel(Server.local, "/Users/jildertviet/of_v0.11.2_osx_release/apps/TIYCS/capt weg kwijt v2.wav", channels: 1);
		this.initScenes();
		childWindows = List.new();
	}
	makeRoutine{
		|num=1, func|
		var routine = {
			num.do{
				|i|
				func.value(i);
				frameDur.wait;
			}
		}.fork;
		^routine;
	}
	clearRoutines{
		routines.do{|routine| routine.stop;};
		routines.clear();
	}
	setFramerate {
		|rate|
		frameDur = (1/rate);
		frameRate = rate;
	}
	setScene {
		|id, screen= -1|
		if(screen == -1, {
			screens.do{|g| g.sendMsg("/setScene", id)};
		}, {
			if(screen.isArray, {
				screens.at(screen).do{|g| g.sendMsg("/setScene", id)};
			},{
				screens[screen].sendMsg("/setScene", id)
			});
		});
	}
	initBingo{
		bingo = [4, 5, 6, 9, 3, -1].reverse;
	}
	setBus {
		|id, value,screen= -1|
		if(screen == -1, {
			screens.do{|g| g.sendMsg("/setBus", id, value)};
		}, {
			if(screen.isArray, {
				screens.at(screen).do{|g| g.sendMsg("/setBus", id, value)};
			},{
				screens[screen].sendMsg("/setBus", id, value);
			});
		});
	}
	eventById {
		|id, value=0, screen= -1|
		if(screen == -1, {
			screens.do{|g| g.sendMsg("/eventById", id, value)};
		}, {
			screens[screen].sendMsg("/eventById", id, value);
		});
	}
	valyueById{
		|id, value=0, screen= -1|
		if(screen == -1, {
			screens.do{|g| g.sendMsg("/setValueById", id, value)};
		}, {
			screens[screen].sendMsg("/setValueById", id, value);
		});
	}
	gui{
		var numColumns = 5;
		var numRows = ceil(scenes.size / numColumns);
		var window = Window("TIYCS - Scenes").front;
		var a = { { Button(window) } ! numColumns } ! numRows;
		var buttonMatrix = View().layout_(VLayout(*a.collect { |x| HLayout(*x) }));
		var brightnessSlider;
		window.view.palette_(QPalette.dark);
		counter = NumberBox().step_(1).normalColor_(Color.white);
		// brightnessSlider = Slider.new(window, label:"brightness", controlSpec: ControlSpec(0, 255, 'lin', 0.001)).action_(
		// {|e| this.valyueById(2, e.value * 2);}).orientation_(\horizontal).valueAction = 255;


		window.layout = VLayout(
			[buttonMatrix],
			[counter],
			// [brightnessSlider, 0.1]
		);
		// Assign scene-functions to buttons
		a = a.reshape(scenes.size, 1);
		a = a.collect({|e| e[0]});
		a.do{|e, i|
			e.string_(scenes[i][0]);
			e.action_({
				onSwitch.value();
				if(bShiftPressed == false, {
					this.closeChildWindows();
				});
				onSwitch = {}; // Clear
				this.clearRoutines;
				scenes[i][1].value()
			});
		};
		window.view.keyDownAction = {
			|doc, char, mod, unicode, keycode, key|
			// [doc, char, mod, unicode, keycode, key].postln;
			switch(key,
				70, { this.whiteFrame(3);}, // 'f'
				71, { this.blackFrame(3);}, // 'g'
				16777248, {bShiftPressed = true},
			);
		};
		window.view.keyUpAction = {
			|doc, char, mod, unicode, keycode, key|
			switch(key,
				16777248, {bShiftPressed = false},
			);
		};
	}
	clearMidiFunc{
		|num|
		[noteOn, noteOff].do{
			|list| // noteOn and noteOff
			var indexToRemove = -1;
			list.do{
				|e, i|
				if(e.msgNum == num, {
					indexToRemove = i;
				});
			};
			if(indexToRemove >= 0, {
				list.removeAt(indexToRemove);
			});
		}
	}
	initDefaultMidi{
		if(~j != nil, {~j.sequence = [0,1,2,3,4]});
		MIDIdef.cc(\brightnessVisuals, {|value, note| this.valyueById(2, value * 2); "Brightness".postln}, 8);
		// MIDIdef.cc(\brightness, {|value, note| this.valyueById(2, value * 2); "Brightness".postln}, 8);
		// MIDIdef.noteOn(\captainScene, {|value, note| scenes[9][1].value(); "Captain scene".postln}, 34, chan: 14);
		MIDIdef.noteOn(\arp, {
			|value, note|
			"ARP".postln;
			// ~j.jonisks.choose.trigger();
			~j.triggerNextInSeq();
		}, noteNum: 36 + (0..30), chan: 14);
		MIDIdef.noteOn(\blackout, { // Pad 1
			|val, num|
			scenesDict[\BLACKOUT][1].value();
		}, 0, chan: 0, srcID: -1625549786);
		MIDIdef.noteOn(\jonisksToWhite, { // Pad 8
			|val, num|
			"All Jonisks to white".postln;
			if(~j != nil, {
				~j.slidersDict[\rgbw].do{|e, i| e.valueAction = [0,0,0,255].at(i)}; // Slider at range 0-255
				~j.slidersDict[\brightness].valueAction = 0.35;
				~j.jonisks.do{|j|
					var c = [0,0,0,255];
					j.synth.set(\rgbw, c/255); // Scaled to 1
					j.setColor(c);
					j.setBrightness(0.35);
				};
			});
		}, 7, chan: 0, srcID: -1625549786);
	}
	whiteFrame{
		|dur=1|
		this.valyueById(16, dur);
	}
	blackFrame{
		|bOverride = false, value|
		if(bOverride == true, {
			blackToggle = value;
		}, {
			blackToggle = blackToggle + 1 % 2;
		});
		this.valyueById(17, blackToggle);
	}
	closeChildWindows{
		childWindows.do{|w|w.close};
	}
}
