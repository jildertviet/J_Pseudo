+ TIYCS {
	initScenes {
		var buttonSize = Rect(0, 0, 100, 30);
		scenes = scenes ++ [
			["None", {
				this.setScene(16);
				~j.jonisks.do{|j|
					var c = [0,0,0,255]; // White
					j.synth.set(\rgbw, c/255); // Scaled to 1
					j.setColor(c);
					j.setBrightness(0.35);
				};
			}],
			["Test", {
				this.setScene(17);
			}],
			["Intro", {
				var rotateSynth, bus;
				var window = Window("TIYCS - Intro").front.setInnerExtent(400, 60);
				var toggleButton = Button.new(window).states_([
					["Rotate", Color.black, Color.green],
					["Stop", Color.white, Color.red],
				]).action_({
					|button|
					if(button.value == 1, {
						bus = Bus.alloc(\control, Server.default);
						// rotateSynth = {Out.kr(bus, SinOsc.kr(1/4).pow(0.1) * Line.kr(0, 1, 1))}.play;
						rotateSynth.free; rotateSynth = {Out.kr(bus, VarLag.kr(LFPulse.kr(1/6, 0.75), 1))}.play;
						this.makeRoutine(inf, {
							|i|
							bus.get({
								|val|
								this.setBus(1, val.linlin(0, 1, 0, 180)); // Rotate
							});
						});
					}, {
						rotateSynth.free; bus.free; this.setBus(1, 0);
						routines.pop.stop;
					});
				});
				var numFrames = (frameRate * 30); // 30 sec
				childWindows.add(window);
				window.view.palette_(QPalette.dark);
				window.view.decorator_(FlowLayout(window.view.bounds));
				this.setScene(0, -1);
				this.makeRoutine(numFrames, {
					|i|
					this.setBus(0, linlin(i, 0, numFrames, 0, 2)); // Movement-intensity
				});
				window.view.onClose = { rotateSynth.free; bus.free; this.setBus(1, 0); this.clearRoutines(); };
				onSwitch = {window.close};
			}],
			["Instructions", {
				var prevVal = 0;
				this.setScene(2, -1);
				this.setBus(0, 0);
				counter.value_(0);
				counter.action_({|e|
					var dir = 1;
					var startPoint = prevVal;
					if(e.value < prevVal, {dir = -1});
					this.makeRoutine(16, {
						|i|
						this.setBus(0, startPoint + ((i/15)*dir));
					});
					prevVal = e.value;
				}).focus;
				this.clearRoutines();
			}],
			["Countdown",{
				this.setScene(3, -1);
				this.setBus(0, 15);
				counter.value_(15);
				counter.action_({|e|this.setBus(0, e.value)}).focus;
			}],
			["Stars", {
				var window = Window("TIYCS - Stars").front.setInnerExtent(400, 30 * 8);
				var speed, height, map, startRoute, planetID, overlay, joniskPos;
				var moveJonisk, emptyBenzine;
				var q;
				childWindows.add(window);
				// var buttonSize;
				window.view.palette_(QPalette.dark);
				// window.view.decorator_(FlowLayout(window.view.bounds));
				q = window.addFlowLayout();

				height = EZSlider.new(window, label:"Height", controlSpec: ControlSpec(0, 2, 'lin')).action_(
					{|e|this.setBus(0, e.value)}).valueAction = 0; // Start @ ground
				speed = EZSlider.new(window, label:"Speed", controlSpec: ControlSpec(0.2, 10, 'lin', 0.001)).action_(
					{|e|this.setBus(1, e.value)}).valueAction = 7;
				map = EZSlider.new(window, label:"Map", controlSpec: ControlSpec(0, 1, 'lin', 0.001)).action_(
					{|e|this.setBus(8, e.value, 1)}).valueAction = 0;
				planetID = EZSlider.new(window, label:"planetID", controlSpec: ControlSpec(0, 1, 'lin', 1)).action_(
					{|e|this.setBus(7, e.value)}).valueAction = 0;
				overlay = EZSlider.new(window, label:"overlay", controlSpec: ControlSpec(0, 255, 'lin', 0.001)).action_(
					{|e|this.setBus(9, e.value)}).valueAction = 0;
				joniskPos = EZSlider.new(window, label:"joniskPos", controlSpec: ControlSpec(0, 200, 'lin', 1)).action_(
					{|e|this.setBus(14, e.value)}).valueAction = 0;

				startRoute = Button.new(window, buttonSize).string_("Start route").action_({
					var numFrames = 300 * frameRate; // 300 sec
					this.clearRoutines();
					this.makeRoutine(numFrames, {
						|i|
						this.setBus(9, i.linlin(0, numFrames, 0, 1));
					});
				});
				moveJonisk = Button.new(window, buttonSize).string_("Move Jonisk").action_({
					var numFrames = 300 * frameRate; // 300 sec
					// this.clearRoutines();
					this.makeRoutine(numFrames, {
						|i|
						// this.setBus(9, i.linlin(0, numFrames, 0, 1));
						{joniskPos.valueAction = i.linlin(0, numFrames, 0, 200);}.defer;
					});
				}).bounds.width_(100);
				emptyBenzine = Button.new(window, buttonSize).string_("Empty benzine").action_({
					var numFrames = 50 * frameRate; // 60sec
					// this.clearRoutines();
					this.makeRoutine(numFrames, {
						|i|
						this.setBus(10, i.linlin(0, numFrames, 150, 0));
						// {joniskPos.valueAction = i.linlin(0, numFrames, 0, 200);}.defer;
					});
				});
				[startRoute, moveJonisk, emptyBenzine].do{|e| e.bounds().resizeBy(100, 0)};
				this.setBus(10, 150);

				[speed,height,map,joniskPos,overlay,planetID].do{|e| e.setColors(numNormalColor: Color.white)};

				window.bounds_(window.bounds + [0,20 + window.bounds.height,0,0]); // Move 80 px to top
				this.setScene(4, -1);

				// this.setBus(3, 0);
				// this.setBus(4, 0);
				// this.setBus(5, 0);

				3.do{
					|i|
					this.setBus(6, [0,1,2].at(i) * -1, i); // Offset all screens
				};
				~forNextStarScene = [height, speed, planetID];
			}],
			["Commercials", {
				var window = Window("TIYCS - Commercials").front.setInnerExtent(400, 30);
				var buttons =
				[
					Button().string_("One").action_({this.setBus(0, 0); this.setScene(6, -1); this.eventById(1, 0)}),
					Button().string_("Two").action_({this.setBus(0, 0); this.setScene(6, -1); this.eventById(1, 1)}),
					Button().string_("Three").action_({this.setBus(0, 0); this.setScene(6, -1); this.eventById(1, 2)}),
					Button().string_("Stop").action_({this.setBus(0, 0); this.setScene(6, -1); this.eventById(1, -1)})
				];
				childWindows.add(window);
				this.setScene(6);
				this.setBus(0, 0); // Bool
				window.bounds_(window.bounds + [0,80,0,0]); // Move 80 px to top
				window.view.palette_(QPalette.dark);
				window.layout = HLayout(
					*buttons
				);
				MIDIdef.noteOn(\commercials, {|val, num| "Commercial".postln; num.postln; {buttons[num-20].valueAction = 1}.defer }, 20 + (0..3), chan: 14);
				MIDIdef.noteOn(\inflightshopping, {|val, num| "Shopping".postln; num.postln; this.setBus(0, 1);}, 19, chan: 14);
			}
			],
			["Stars2", {
				var controls = ~forNextStarScene;
				scenesDict[\Stars][1].value(); // Execute Stars default
				controls[0].value_(2).valueAction = 2; // Height
				controls[1].value_(2).valueAction = 2; // Speed
				controls[2].value_(1).valueAction = 1; // Planet
			}],
			["Benzine", {
				var window = Window("TIYCS - alarm").front.setInnerExtent(400, 60 + 30);
				var fillPct, redFill, fillButton;
				var toggleVal = 1;
				childWindows.add(window);

				if(~j != nil, {
					~j.slidersDict[\rgbw].do{|e, i| e.valueAction = [255,0,0,0].at(i)}; // Slider at range 0-255
					~j.slidersDict[\asr].do{|e, i| e.valueAction = [0, 0.5, 0].at(i)};
					~j.slidersDict[\brightness].valueAction = 0.35;
					~j.slidersDict[\brightnessAdd].valueAction = 0;
				});

				MIDIdef.noteOn(\alarmOn, {
					|val, num|
					{redFill.valueAction = 255;}.defer; ("Note on " + num).postln;
					~j.jonisks.do{|j| j.setColor([255,0,0,0]); j.synth.set(\rgbw, [1,0,0,0]); j.trigger()};
				}, 35, chan: 14);
				MIDIdef.noteOff(\alarmOff, {
					|val, num|
					{redFill.valueAction = 0;}.defer; ("Note off " + num).postln
					// ~j.jonisks.do{|j| j.setColor([0,0,0,0]);};
				}, 35, chan: 14);

				this.setScene(9, -1); // All to scene
				// this.setScene(16, [0,2]); // Outer two to black
				this.setBus(10, 0); // Benzine level
				this.setBus(3, 255, 1); // Show meter
				this.setBus(3, 0, [0,2]); // Outter two, don't dislay meter
				this.setBus(13, 255); // Show stars layer
				this.setBus(9, 0); // Hide dashboard overlay
				this.setBus(7, 1); // Planet ID

				window.view.palette_(QPalette.dark);
				window.view.decorator_(FlowLayout(window.view.bounds));
				fillPct = EZSlider.new(window, label:"fillPct", controlSpec: ControlSpec(0, 100, 'lin')).value_(100).action_({|e|this.setBus(4, e.value)}).valueAction = 100;
				redFill = EZSlider.new(window, label:"redFill", controlSpec: ControlSpec(0, 255, 'lin')).value_(255).action_({|e|this.setBus(5, e.value)}).valueAction = 255;
				fillButton = Button.new(window).string_("Fill").action_({
					var numFrames = (frameRate * 15); // 30 sec
					this.makeRoutine(numFrames, {
						|i|
						this.setBus(10, linlin(i, 0, numFrames, 0, 180)); // Movement-intensity
					});
				});
				noteOn.add(MIDIdef.noteOn(\fillTank, {|val, num| "Fill tank".postln; {fillButton.valueAction = 1;}.defer }, 25, chan: 14));
				MIDIdef.noteOn(\switchScene, {|val, num| "Switch to return scene".postln; {scenesDict[\ReturnToShip][1].value();}.defer; MIDIdef(\switchScene).clear; /* Return to ship */ }, 26, chan: 14);
				fillPct.setColors(numNormalColor: Color.white);
				redFill.setColors(numNormalColor: Color.white);
				window.bounds_(window.bounds + [0,80 + 30,0,0]); // Move 80 px to top
				window.view.keyDownAction = {
					|doc, char, mod, unicode, keycode, key|
					if(keycode == 49, {
						toggleVal = toggleVal + 1 % 2;
						redFill.valueAction = toggleVal * 255;
					});
				};
				window.onClose = {MIDIdef(\alarmOn.free()); MIDIdef(\alarmOff).free(); MIDIdef(\fillTank).free};
				if(~j != nil, {
					~j.jonisks.do{|j| j.synth.set(\a, 0.0001); j.synth.set(\s, 0.3); j.synth.set(\r, 0.0001);};
				});
			}],
			["ReturnToShip", {
				"ReturnToShip".postln;
				this.setScene(10);
				this.setBus(0, -1); // Black
				this.counter.action_(
					{|e|
						e.value.postln;
						switch(e.value.asInteger,
							0, {this.setBus(0, 0, 0); this.setBus(0, 2, 1); this.setBus(0, 4, 2);},
							1, {this.setBus(0, 1, 0)},
							// 2, {this.setBus(0, 2, 1)},
							2, {this.setBus(0, 3, 1)},
							// 4, {this.setBus(0, 4, 2)},
							3, {this.setBus(0, 5, 2)},
							4, {this.setBus(0, 6, 1); this.setBus(0, -1, [0,2])},
							5, {this.setBus(0, 7, 1)},
						);

				}).valueAction_(0).focus();
				// this.counter.valueAction = 0; // All to grey
				MIDIdef.noteOn(\returnToShip, {
					|val, num|
					num.postln;
					{this.counter.valueAction = num - 26}.defer;
				}, 26 + (0..5), chan: 14);
				onSwitch = {
					MIDIdef(\returnToShip).free;
				};
			}],
			["Captain", {
				var recordSynth = {
					var input = CombC.ar(LPF.ar(SoundIn.ar(0), 10000), 0.2, 0.01, 1);
					input = PlayBuf.ar(1, captainSample.bufnum);
					// RecordBuf.ar(Lag.ar(input, 0.001), buffer.bufnum);
					RecordBuf.ar(LPF.ar(input, 400), buffer.bufnum);
					0
				};
				var updateWaveformT = {
					inf.do{
						buffer.getToFloatArray(count: -1, action:{
							arg array;
							var subArrays = 0!3;
							subArrays.do{|e, i| // Divide the array to 3 sub-arrays (each screen)
								subArrays[i] = array.at((((array.size/3)*i) .. ((array.size/3)*(i+1)-1)));
								subArrays[i] = Array.newFrom(subArrays[i]).collect({|e|e.asBytes}).reshape(subArrays[i].size * 8).asInteger;
								subArrays[i] = Int8Array.newFrom(subArrays[i]);
								screens[[0,1,2].at(i)].sendMsg("/setWave", subArrays[i]);
							};
						});
						(1/30).wait;
					}
				};
				this.clearRoutines();
				// this.setScene(11, 1);
				this.setScene(12, [0,1,2]);
				this.setBus(0, 255, 1); // Show captain picto
				onSwitch = {
					updateWaveformT.stop;
					updateWaveformT.postln;
					// buffer.free;
					recordSynth.free;
				};
				counter.action_({
					|e|
					"X".postln;
					e.value.postln;
					switch(e.value.asInteger,
						0, { // Calling
							this.setBus(0, 0); // All: waveform black
							this.setBus(1, 255, 1); // Center to incoming call icon
							this.setBus(1, 0, [0,2]); // Others not
							this.setBus(2, 0); // During call to 0
							this.setBus(2, 0); // During call to 0
							this.setBus(3, 0); // Tel icon
							"Incoming call".postln;
						},
						1, { // During call
							this.setBus(2, 255, 1); // During call icon
							this.setBus(0, 255); // All: waveform full white
							this.setBus(1, 0, 1); // Center to incoming call icon OFF
							this.setBus(3, 255, 0); // Tel icon
							if(Server.local.serverRunning, {updateWaveformT = updateWaveformT.fork}, {"Server not booted!".error});
							recordSynth.play;
							this.makeRoutine(59 * this.frameRate, {
								|i|
								var second = i / this.frameRate;
								second = second.floor;
								this.setBus(4, second, 1);
							});
						},
						2, {}
					);
				});
				counter.focus.valueAction = 0;
				// Midinote 34 starts the samples (@ initMidi)
			}],
			["Bingo", {
				/*
				BACKGROUND,
				WHEEL,
				NUMBER,
				BACKGROUND_AND_WHEEL
				*/
				var window = Window("TIYCS - Bingo").front.setInnerExtent(400, 30);
				childWindows.add(window);
				this.setScene(13, -1);
				this.setBus(0, 3, 1); // Bingo mode
				this.setBus(0, 0, [0,2]); // Bingo mode
				this.setBus(1, 1, 1); // Gravity full
				this.setBus(3, 1, [0,2]); // numberScale
				this.blackFrame(true, 0);
				window.bounds_(window.bounds + [0,80,0,0]); // Move 80 px to top
				window.view.palette_(QPalette.dark);
				window.layout = HLayout(
					[Button().states_([["Roll"],["Stop"]]).action_({|e| if(e.value == 1, {
						var routine = this.makeRoutine(inf, {|i| if( i % (frameRate*0.5) == 0, {this.setBus(1, [0, 1, 0.5].choose)})}); // Roll (set rotational gravity intensity)
					}, {
						this.setBus(1, 1); // Why 1? ;
						this.clearRoutines(); // Stop
					})})],
					[Button().string_("Pick number").action_({
						this.setBus(0, 2, [0,2]); // Set outter screens to certain bingo-drawMode
						if(bingo != nil, {
							this.eventById(2, bingo.pop);
						});
					})],
					[Button().string_("Repeat").action_({
						this.makeRoutine(16, {
							|i|
							this.setBus(3, 1 - i.linlin(0, 15, 0.25, 0), [0,2])
						});
					})],
					[Button().string_("Reset").action_({this.initBingo(); this.eventById(3)})],
					[Button().states_([["Rotate"],["Stop"]]).action_({|e| if(e.value == 1, {
						this.screens.do{|screen|screen.sendMsg("/setValueById", 15, 1)};
					}, {
						this.screens.do{|screen|screen.sendMsg("/setValueById", 15, 0)};
					})})],
				);
				if(~j != nil, {
					~j.slidersDict[\brightness].valueAction = 0.35;
					~j.jonisks.do{|j|
						var c = [[0,0,0,255],[0,0,255,0]].choose;
						j.synth.set(\rgbw, c/255); // Scaled to 1
						j.setColor(c);
						j.setBrightness(0.35);
					};
				});
			}],
			["Code", {
				this.setScene(14, -1);
				this.setBus(0,0);
				this.counter.value_(0).action_({|e|this.setBus(0, e.value)}).focus();
			}],
			["Autopilot", {
				var numFrames = 10 * frameRate;
				{
					var routine;
					10.wait;
					routine = this.makeRoutine(numFrames, {|i|
						this.setBus(1, linlin(i, 0, numFrames, 0, 101)); // %
					}); // Roll
				}.fork;
				this.setScene(15, -1);
				this.setBus(1,0); // Percentage
				this.setBus(0,0); // Hover
			}],
			["Countdown2",{
				this.setScene(3, -1);
				this.setBus(0, 5);
				counter.value_(5);
				counter.action_({|e|this.setBus(0, e.value)}).focus;
			}],
			["Starsfinal",{
				var oscFunc;
				var bFireBallStarted = false;
				this.setScene(21);
				this.setBus(10, 0);
				this.setBus(11, 0);
				this.setBus(12, 0);
				this.setBus(13, 0);
				this.valyueById(2, 255);
				"Final stars".postln;
				MIDIdef.noteOn(\fireball, {
					|val, num|
					"Fireball".postln;
					num.postln;
					if(bFireBallStarted == false, {
						"Start fireball".postln;
						bFireBallStarted = true;
						{
							// var num = 30 * 99.758;
							var num = 30 * 98;
							this.setBus(10, -3.0);
							// 10.wait;
							this.clearRoutines;
							{
								var dur = 98;
								var x;
								var noise = LFDNoise3.ar(Line.kr(0.1, 10, dur)!2).pow(0.25);
								var xy = LPF.ar(noise, 1);
								var line = Line.kr(0, 1, dur);
								var r = line.pow(20.0).linlin(0, 1, 0.001, 4.5).max(Line.kr(0, 0.8, 10));
								var noiseAmp = line.pow(2).linlin(0, 1, 0.01, 0.05) + 0.02;
								var speed = (line.pow(2)).linlin(0, 1, 1.0, 10.0);
								xy = xy * Line.kr(1, 0, dur);
								SendReply.kr(Impulse.kr(30), '/starsFinal', xy ++ r ++ noiseAmp ++ speed);
								Line.kr(0, 1, dur, doneAction: 2);
							}.play;
							oscFunc = OSCdef(\receiveStarsFinal, {|msg|
								this.setBus(10, msg[3]); // X, y, r, noiseAmp, speed
								this.setBus(11, msg[4]);
								this.setBus(12, msg[5]);
								this.setBus(13, msg[6]);
								this.setBus(14, msg[7]);
							}, '/starsFinal');
							this.makeRoutine(num, { // X
								|i|
								// var x, y, r, noiseAmp;
								// var value = i / num;
								//x = value.pow(2);
								//this.setBus(0, x.linlin(0, 1, -5.0, 0.0));
								// if(i > (num * 0.5), {
									// var temp = (i - (num*0.5)) / (num*0.5);
									// x = temp.pow(2);
									// this.setBus(10, x.linlin(0, 1, -3.0, 0.0));
								// });
								// this.setBus(11, value.linlin(0, 1, -0.5, 0));
								// r = value.pow(20.0); // Was 8.0
								// this.setBus(12, r.linlin(0, 1, 0.001, 4.5).max(0.8));
								// noiseAmp = value.pow(2);
								// this.setBus(13, noiseAmp.linlin(0, 1, 0.01, 0.08));
								// this.setBus(14, (value.pow(2)).linlin(0, 1, 1.0, 10.0)); // Speed
								if(i == (num.asInteger-1), {
									"Set to black".postln;
									this.valyueById(2, 0); // Brightness 0
									this.loadScene(\Einde);
								});
							});
						}.fork;
					});
				}, 96, chan: 14);
			}],
			["Party", {
				var colors = ["ffbe0b","fb5607","ff006e","8338ec","3a86ff"]; // https://coolors.co/palettes/popular/rainbow (export, code, array)
				colors = colors.collect({|c| Color.fromHexString(c)});
				if(~j != nil, {
					~j.jonisks.do{|j|
						var color = colors.choose;
						j.synth.set(\rgbw, [color.red, color.green, color.blue, 30/255]);
						j.setColor((color.toJV)[3] = 30);
					}; // R G B and W is minimal
					~j.slidersDict[\brightness].valueAction = 0.55;
					~j.slidersDict[\brightnessAdd].valueAction = 0.0;
					~j.slidersDict[\asr].do{|e, i| e.valueAction = [0.1, 0.5, 3.0].at(i)};
				});
				MIDIdef.noteOn(\partyNote, {
					|val, num|
					// "partynote".postln;
					2.do{~j.jonisks.choose.trigger();}; // Trigger 2 Jonisks at once
					if(10.rand == 3, {~j.jonisks.do{|j| j.setColor((colors.choose.toJV)[3] = 30)};}); // Random colors
				}, 97, chan: 14);
				onSwitch = {
					MIDIdef(\partyNote).free;
				};
			}],
			["BLACKOUT", {
				// this.blackFrame();
				MIDIdef.noteOn(\arp).free;
				this.valyueById(2, 0); // Brightness 0
				if(~j != nil, {
					~j.jonisks.do{|j| j.synth.set(\rgbw, [0,0,0,0])};
				});
				"BLACKOUT".postln;
			}],
			["Einde", {
				this.valyueById(2, 0); // Black
				"/usr/local/bin/brightness 0.0".unixCmd;
				{10.wait; "/usr/local/bin/brightness 1.0".unixCmd;}.fork;
				this.setScene(18);
				{
					4.wait;
					this.valyueById(2, 255);
				}.fork;
			}],
			["QandA", {
				// All Jonisks to low freq noise?
				var options = (0..24);
				var picked = (0!9);
				9.do{|e,i| picked[i] = options.removeAt(options.size.rand)};
				// picked = picked.sort;
				this.setScene(20);

				if(~j != nil, {
					~j.jonisks.do{|j| j.synth.set(\noiseMul, 0.5)};
					// ~j.setBrightnessAdd(0.35);
					~j.slidersDict[\brightnessAdd].valueAction = 0.35;
					~j.slidersDict[\brightness].valueAction = 0.6;
					// ~j.setBrightness(0.6);
				});
				counter.action_({
					|e|
					if(picked.size != 0, {
						var toLightUp = picked.removeAt(0);
						toLightUp.postln;
						if(~j != nil, {
							~j.slidersDict[\brightnessAdd].valueAction = 0.35;
							// ~j.setBrightnessAdd(0.05);
							~j.jonisks[toLightUp].setBrightnessAdd(1.0);
						});
					}, {
						"Restart QandA scene".postln;
					});
				});
				counter.focus();
				onSwitch = {
					~j.jonisks.do{|j| j.synth.set(\noiseMul, 0.0)};
				};
				// this.setScene(16,[0,2]);
			}]
		];
		t = List();
		scenes.do{|e| t.add(e[0].asSymbol); t.add(e)};
		scenesDict = Dictionary.newFrom(t.asArray);
	}
}