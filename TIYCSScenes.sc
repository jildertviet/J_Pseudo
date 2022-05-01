+ TIYCS {
	initScenes {
		scenes = scenes ++ [
			["None", {
				this.setScene(16);
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
						routines.add(this.makeRoutine(inf, {
							|i|
							bus.get({
								|val|
								this.setBus(1, val.linlin(0, 1, 0, 180)); // Rotate
							});
						}));
					}, {
						rotateSynth.free; bus.free; this.setBus(1, 0);
						routines.pop.stop;
					});
				});
				var numFrames = (frameRate * 30); // 30 sec

				window.view.palette_(QPalette.dark);
				window.view.decorator_(FlowLayout(window.view.bounds));
				this.setScene(0, -1);
				routines.add(this.makeRoutine(numFrames, {
					|i|
					this.setBus(0, linlin(i, 0, numFrames, 0, 2)); // Movement-intensity
				}));
				window.view.onClose = { rotateSynth.free; bus.free; this.setBus(1, 0); this.clearRoutines(); };
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
					routines.add(this.makeRoutine(16, {
						|i|
						this.setBus(0, startPoint + ((i/15)*dir));
					}));
					prevVal = e.value;
				}).focus;
			}],
			["Countdown",{
				this.setScene(3, -1);
				this.setBus(0, 15);
				counter.value_(15);
				counter.action_({|e|this.setBus(0, e.value)}).focus;
			}],
			["Stars", {
				var window = Window("TIYCS - Stars").front.setInnerExtent(400, 30 * 5);
				var speed, height, map, startRoute, planetID;

				window.view.palette_(QPalette.dark);
				window.view.decorator_(FlowLayout(window.view.bounds));
				height = EZSlider.new(window, label:"Height", controlSpec: ControlSpec(0, 2, 'lin')).action_(
					{|e|this.setBus(0, e.value)}).valueAction = 1;
				speed = EZSlider.new(window, label:"Speed", controlSpec: ControlSpec(0.2, 10, 'lin', 0.001)).action_(
					{|e|this.setBus(1, e.value)}).valueAction = 7;
				map = EZSlider.new(window, label:"Map", controlSpec: ControlSpec(0, 1, 'lin', 0.001)).action_(
					{|e|this.setBus(8, e.value, 1)}).valueAction = 0;
				planetID = EZSlider.new(window, label:"planetID", controlSpec: ControlSpec(0, 1, 'lin', 1)).action_(
					{|e|this.setBus(7, e.value)}).valueAction = 0;

				startRoute = Button.new(window).string_("Start route").action_({
					var numFrames = 300 * frameRate; // 300 sec
					this.clearRoutines();
					this.routines.add(this.makeRoutine(numFrames, {
						|i|
						this.setBus(9, i.linlin(0, numFrames, 0, 1));
					}));
				});

				speed.setColors(numNormalColor: Color.white);
				height.setColors(numNormalColor: Color.white);
				map.setColors(numNormalColor: Color.white);
				window.bounds_(window.bounds + [0,20 + window.bounds.height,0,0]); // Move 80 px to top
				this.setScene(4, -1);

				// this.setBus(3, 0);
				// this.setBus(4, 0);
				// this.setBus(5, 0);

				3.do{
					|i|
					this.setBus(6, [0,1,2].at(i) * -1, i); // Offset all screens
				};
				~forNextStarScene = [height, speed];
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
			["Stars 2", {
				var controls = ~forNextStarScene;
				scenes[5][1].value(); // Execute Stars default
				controls[0].valueAction = 2; // Height
				controls[1].valueAction = 2; // Speed
			}],
			["Benzine", {
				var window = Window("TIYCS - alarm").front.setInnerExtent(400, 60 + 30);
				var fillPct, redFill, fillButton;
				var toggleVal = 1;

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
				this.setBus(0, 0);
				this.setBus(3, 255, 1);
				this.setBus(3, 0, [0,2]); // Outter two, don't dislay meter

				window.view.palette_(QPalette.dark);
				window.view.decorator_(FlowLayout(window.view.bounds));
				fillPct = EZSlider.new(window, label:"fillPct", controlSpec: ControlSpec(0, 100, 'lin')).value_(100).action_({|e|this.setBus(4, e.value)}).valueAction = 100;
				redFill = EZSlider.new(window, label:"redFill", controlSpec: ControlSpec(0, 255, 'lin')).value_(255).action_({|e|this.setBus(5, e.value)}).valueAction = 255;
				fillButton = Button.new(window).string_("Fill").action_({
					var numFrames = (frameRate * 15); // 30 sec
					routines.add(this.makeRoutine(numFrames, {
						|i|
						this.setBus(0, linlin(i, 0, numFrames, 0, 145)); // Movement-intensity
					}));
				});
				noteOn.add(MIDIdef.noteOn(\fillTank, {|val, num| {fillButton.valueAction = 1;}.defer }, 25, chan: 14));
				MIDIdef.noteOn(\switchScene, {|val, num| {scenes[8][1].value();}.defer; MIDIdef(\switchScene).clear; /* Return to ship */ }, 26, chan: 14);
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
				}.play;
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
				if(Server.local.serverRunning, {updateWaveformT = updateWaveformT.fork}, {"Server not booted!".error});
				// this.setScene(11, 1);
				this.setScene(12, [0,1,2]);
				this.setBus(0, 255, 1); // Show captain picto
				onSwitch = {
					updateWaveformT.stop;
					updateWaveformT.postln;
					// buffer.free;
					recordSynth.free;
				};
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
						routines.add(routine);
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
					routines.add(routine);
				}.fork;
				this.setScene(15, -1);
				this.setBus(1,0); // Percentage
				this.setBus(0,0); // Hover
			}],
			["Countdown 2",{
				this.setScene(3, -1);
				this.setBus(0, 5);
				counter.value_(5);
				counter.action_({|e|this.setBus(0, e.value)}).focus;
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
				this.setScene(18);
			}]
		];
		t = List();
		scenes.do{|e| t.add(e[0].asSymbol); t.add(e)};
		scenesDict = Dictionary.newFrom(t.asArray);
	}
}