JoniskMain{
	var <> jonisks;
	// var <> serial;
	var <> serialReadRoutine;
	var <> serial; // Use one SerialPort instance
	var <> serialPorts;
	var <> baud = 230400;
	var <> updateRoutine;
	var frameDur = 0.04;
	var <>msgBuffer = #[0,0,0];
	var <> message = "";
	var <>bWriteMsg = false;
	var liveButton;
	var <> window;
	var frameRate;
	var frameRateIndex = 2;
	var <> lastSeen;
	var <>states;
	var <>testPattern;
	var <>patternDelay = 0.1;
	var <> patterns;
	var <>sequence;
	var <>seqIndex = 0;
	var guiDict;
	var <>slidersDict;
	var <>serialName = "/dev/ttyUSB0";
	var <> lagTime = 30;
	*linuxPath{^"/home/jildert/of_v0.11.2_linux64gcc6_release/apps/TIYCS/jonisk.config"}
	*new{
		|path="/Users/jildertviet/of_v0.11.2_osx_release/apps/TIYCS/jonisk.config", serialNameTemp="/dev/ttyUSB0"|
		^super.new.init(path, serialNameTemp);
	}
	init{
		|path, serialNameTemp|
		serialPorts = List.new();
		jonisks = List.new();
		serialName = serialNameTemp;
		this.openDefaultSerial();
		this.initSerial();
		// Server.default.waitForBoot({
		this.readConfig(path); // Creates Jonisk objects, that need a Bus allocated on the server
		states = (0)!jonisks.size;
		lastSeen = (0)!jonisks.size;
		this.checkLastSeen();
		this.iniTestPattern();
		this.initMIDI();
		this.initGuiDict();
		sequence = (0..(jonisks.size-1));
		patterns = 0!4;
		this.uniqueInit();
		// });
	}
	uniqueInit{

	}
	createChildObject{
		|i, serial|
		^Jonisk.new(i, serial);
	}
	readConfig{
		|path|
		var file = JSONFileReader.read(path);
		file[0]["activeJonisks"].do{
			|j, i|
			var addr = j[0];
			var addrToPrint = addr;
			addr.postln;
			addr = addr.collect({|e| e.split($x)[1].asHexIfPossible});
			jonisks.add(this.createChildObject(i, serial).address_(addr).addrToPrint_(addrToPrint));
		}
		// This loads the Jonisk instances
	}
	checkLastSeen{
		{
			inf.do{
				lastSeen.do{
					|t, i|
					if(t > 0, { // Only check actual set timestamps
						if(Date.getDate.rawSeconds - t > 120, {
							// {states[i].value_(0)}.defer; // Button
							{states[i].background_(Color.gray)}.defer;
						});
					});
				};
				2.wait;
			}
		}.fork;
	}

	openDefaultSerial {
		var portsToOpen = List.new();
		SerialPort.devices.do{
			|e|
			if(e.find("SLAB") != nil, {
				if(e.find("tty") != nil, {
					e.postln;
					portsToOpen.add(e);
			})});
			if(e.find("cu") != nil, {
				if(e.find("wchusbserial") != nil, {
					e.postln;
					portsToOpen.add(e);
			})});
			if(e.find(serialName) != nil, {
				if(e.find(serialName) != nil, {
					e.postln;
					portsToOpen.add(e);
			})});
		};
		portsToOpen.do{
			|e, i|
			(e ++ " opened").postln;
			serialPorts.add(SerialPort.new(e, baud, crtscts: true));
		};
		if(portsToOpen.size == 0,{
			"No serial port found!".error;
		});
	}
	storeByte{
		|byte|
		msgBuffer = msgBuffer.rotate(-1);
		msgBuffer[2] = byte;
	}
	checkForMsgStart{
		|byte|
		if(byte == 'g'.ascii[0], {
			if(msgBuffer[1] == 's'.ascii[0], {
				if(msgBuffer[0] == 'm'.ascii[0], {
					bWriteMsg = true;
				});
			});
		});
	}
	checkForMsgEnd{
		|byte|
		if(byte == 'd'.ascii[0], {
			if(msgBuffer[1] == 'n'.ascii[0], {
				if(msgBuffer[0] == 'e'.ascii[0], {
					bWriteMsg = false;
					this.parseMsg();
					^ true;
				});
			});
		});
		^ false;
	}
	parseMsg{
		var msg = message;
		msg = msg.replace("en", ""); // Remove last
		("\nMSG: " ++ msg).postln;
		if(msg.find("b") == 0, { // Battery update
			var addr = (0!6);
			var batteryVoltage;
			msg = msg.replace("b", ""); // ?
			for(0, 5, {|e, i| addr[i] = msg.split($:)[i].asInteger});
			addr = addr + [0, 0, 0, 0, 0, 1];
			addr.postln;
			batteryVoltage = msg.split($:).last.asInteger;
			batteryVoltage = batteryVoltage / 2.pow(12); // 0V is 0, 16.8V is 4096. Value is now a percentage / 100.
			batteryVoltage = batteryVoltage - (2/3); // 0.66 is shut-down voltage
			batteryVoltage = (batteryVoltage * 300).asInteger; // 1/3 is scaled back to 100%
			batteryVoltage.postln;
			jonisks.do{|e| if(e.address == addr, {
				e.setBatteryPct(batteryVoltage);
			})};
			/*			{
			window.close;
			this.gui();
			}.defer;*/
		});
		if(msg.find("a") == 0, { // Alive ping?
			var addr = (0!6);
			var batteryVoltage;
			var fwVersion = "...";
			msg = msg.replace("a", "");
			for(0, 5, {|e, i| addr[i] = msg.split($:)[i].asInteger});
			addr = addr + [0, 0, 0, 0, 0, 1];
			addr.postln;


			// ("fwVersion: " ++ fwVersion[0] ++ "," ++ fwVersion[1]).postln;
			batteryVoltage = msg.split($:)[6].asInteger;
			batteryVoltage = batteryVoltage / 2.pow(12); // 0V is 0, 16.8V is 4096. Value is now a percentage / 100.
			batteryVoltage = batteryVoltage - (2/3); // 0.66 is shut-down voltage
			batteryVoltage = (batteryVoltage * 300).asInteger; // 1/3 is scaled back to 100%

			if(msg.split($:).size >= 8, {
				fwVersion = msg.split($:).at([7, 8]).ascii.reshape(2);
			});

			jonisks.do{
				|e, i|
				if(e.address == addr, {
					{states[i].background_(Color.green)}.defer;
					e.setBatteryPct(batteryVoltage);
					{e.fwVersionField.string_(fwVersion.asString)}.defer;
					lastSeen[i] = Date.getDate.rawSeconds;
				}
			)};
		});
		message = "";
	}
	initSerial {
		|serialID=0|
		serial = serialPorts[serialID];
		if(serial != nil, {
			if(serial.isOpen, {
				"Start reading serial".postln;
				serialReadRoutine = Routine({
					var byte;
					inf.do{|i|
						byte = serial.next;
						while({byte != nil}, {
							this.storeByte(byte);
							if(this.checkForMsgEnd(byte), { // This calles MsgParse()

							}, {
								if(byte < 127, {
									if(byte == 10, { // Newline?
										"".postln;
									}, {
										// byte.asAscii.post; // Monitor incoming bytes
									});
								});
							});
							if(bWriteMsg == true, {
								message = message ++ byte.asAscii;
							});

							this.checkForMsgStart(byte);
							byte = serial.next;
						});
						0.05.wait; // Was 0.1
					};
				}).play;
			}, {
				"Serial not open".error;
			});
		});
	}
	update{
		jonisks[0].bus.getn(jonisks.size * 4, {
			|v|
			var end = [101,110,100];
			// Or use msgType 0x05 for NO_LAG
			var msg = (0xFF!6) ++ [0x09] ++ lagTime.asInteger.asInt16.asBytes ++ ((v*255).asInteger) ++ end;

			// msg.postln;
			serial.putAll(msg);
		});
	}
	start{
		if(serial != nil, {
			if(serial.isOpen, {
				updateRoutine.stop;
				updateRoutine = {
					inf.do{
						this.update();
						frameDur.wait;
					}
				}.fork;
				jonisks.do{|e| e.bLive = true};
				^true;
			}, {
				"Serial port not open".error;
				^false;
			});
		}, {
			"No serial port initiated".error;
			^false;
		});
	}
	stop{
		jonisks.do{|e| e.bLive = false};
		updateRoutine.stop;
	}
	gui{
		var canvasLocal = View(); // .background_(Color.black);
		var globalButton;
		var canvas,layout;
		var bounds = Rect(0, 0, 500, 700);
		window = Window("TIYCS - Jonisk control", bounds, scroll: true).front;
		window.view.hasBorder_(false);
		window = ScrollView(window, bounds:  bounds.insetBy(2,0)).hasBorder_(false);
		window.palette_(QPalette.dark);
		liveButton = Button().string_("Idle").action_({
			|e|
			e.states_([["GO LIVE", Color.white, Color.new255(49,222,75)], ["STOP", Color.white, Color.new255(255,65,54)]]).action_({
				|e|
				if(e.value == 1, {
					"Start live mode".postln;
					if(this.start() == true, {

					}, {
						e.value_(0);
					});
				}, {
					"Stop live mode".postln;
					this.stop();
				})
			});
			e.valueAction_(1);
		});

		canvasLocal.palette_(QPalette.dark);
		states = Array.fill(jonisks.size, {Button().states_([
			["", Color.grey, Color.grey],
			["", Color.grey, Color.green],
		]).canFocus_(false)});
		states = Array.fill(jonisks.size, {
			View().background_(Color.gray);
		});
		globalButton = Button().string_("Global").action_({this.openGlobalGui});
		window.layout = VLayout(
			HLayout(
				[liveButton],
				// [NumberBox().value_(frameDur).action_({|e| frameDur = e.value}).normalColor_(Color.white)],
				[PopUpMenu().items_([10, 25, 30, 60]).stringColor_(Color.white).action_({
					|menu|
					menu.item.postln;
					frameDur = (1/menu.item);
					lagTime = frameDur;
					frameRate = menu.item;
				}).value_(frameRateIndex)],
				globalButton,
				Button().string_("Config").action_({this.configLights}),
				Button().string_("Test pattern").action_({this.toggleTestPatttern()}),
				/*				window.view.bounds.width * 0.25,*/
			),
			*(jonisks.collect({|e, i|
				var guiButtonView = View();
				var guiButton = Button(guiButtonView, Rect(0, 0, 40, 20)).states_([
					["GUI", Color.white, Color.black.alpha_(0.1)],
				]).action_({
					var guiWindow = e.gui;
					var bounds = guiWindow[0].bounds;
					guiWindow[0].bounds_(bounds + Rect(window.bounds.width, 0, 0, 0))});
				var testButtonView = View();
				var testButton = Button(testButtonView, Rect(0, 0, 40, 20)).states_([
					["Test", Color.white, Color.black.alpha_(0.1)],
				]).action_({e.testLed()});
				HLayout(
					[StaticText.new().string_(e.id).background_(Color.black.alpha_(0.1)), s: 5],
					[StaticText.new().string_(e.addrToPrint.asCompileString.replace("\"", "").replace("]", "").replace("[", "")).background_(Color.black.alpha_(0.1)), s: 60],
					[e.createBatteryField(), s: 10],
					[e.createFwVersionField(), s: 10],
					[guiButtonView, s: 10],
					[testButtonView, s: 10],
					[states[i], s: 1]
			)}));
		);
		canvasLocal.layout = layout;
		window.canvas = canvasLocal;
		globalButton.valueAction_(1);
	}
	configLights{
		|mode=1|
		{
			jonisks.do{
				|jonisk, i|
				var addr = jonisk.address;
				var id = i;
				var end = [101,110,100];
				if(mode == 0, {
					serial.putAll(addr ++ [0x03, id] ++ end);
				}, {
					if(mode == 1, {
						serial.putAll((0xFF!6) ++ [0x11] ++ addr ++ [id] ++ end);
					});
				});
				0.1.wait;
			};
			"Config lights done".postln;
		}.fork
	}
	openGlobalGui{
		var bounds;
		var globalJoniskWindow;
		var windowAndSliders = Jonisk.getGuiWindow(nil, guiDict);
		slidersDict = windowAndSliders[1];
		globalJoniskWindow = windowAndSliders[0];
		bounds = globalJoniskWindow.bounds;
		globalJoniskWindow.bounds_(bounds + Rect(window.bounds.width, 0, 0, 0));
		// globalGuiSliders
	}
	iniTestPattern{
		var ids = jonisks.collect({|e|e.id});
		Event.addEventType(\triggerJonisk, {
			~jonisk.trigger();
			~jonisk.id.postln;
		});
		Pdef(\joniskTestPattern,
			Pbind(\type, \triggerJonisk,
				\jonisk, Pseq(jonisks, inf),
				\delta, patternDelay
			)
		)
	}
	toggleTestPatttern {
		var pattern = Pdef(\joniskTestPattern);
		if(pattern.isPlaying, {
			pattern.pause;
		}, {
			pattern.play;
		});
	}
	triggerNextInSeq{
		var id = sequence.wrapAt(seqIndex);
		seqIndex = seqIndex + 1;
		jonisks[id].trigger();
	}
	initGuiDict{
		guiDict = Dictionary.newFrom([
			\test, {"Inactive".postln},
			\ota, {},
			\battery, {},
			\testEnv, {jonisks.do{|j| j.trigger()}},
			\deepsleep, {
				var w = Window("Deep sleep duration: all", Rect(window.bounds.left, 500, window.bounds.width, 100));
				var b = NumberBox(w, Rect(150, 10, 100, 20));
				b.value = 1;
				b.action = {
					arg numb;
					var min = numb.value;
					w.close;
					// object.deepSleep(numb.value);
					{
						("Put all Jonisks to sleep for " ++ min.asString ++ " minutes").postln;
						jonisks.do{|e|
							e.postln;
							e.deepSleep(min);
							0.25.wait;
						};
					}.fork;
				};
				w.front;
			},
			\setColor, {
				|e, i|
				jonisks.do{
					|object|
					var color = object.color;
					var newColor = [color.red, color.green, color.blue, color.alpha];
					newColor[i] = e.value / 255;
					object.color = color = Color(newColor[0], newColor[1], newColor[2], newColor[3]);
					object.setColor(color.toJV ++ (color.alpha * 255));
					object.synth.set(\rgbw, color.asArray);
				}
			},
			\getColor, {
				|i|
				Color.black.alpha_(0).toJV[i];
			},
			\getEnv, {
				|i|
				1
			},
			\setEnv, {
				|e, i|
				jonisks.do{
					|object|
					switch(i,
						0, {object.setAttack(e.value)},
						1, {object.setSustain(e.value)},
						2, {object.setRelease(e.value)}
					);
				}
			},
			\getBrightness, {
				1
			},
			\setBrightness, {
				|e|
				jonisks.do{ |object|
					object.setBrightness(e.value);
				}
			},
			\getBrightnessAdd, {
				1
			},
			\setBrightnessAdd, {
				|e|
				jonisks.do{ |object|
					object.setBrightnessAdd(e.value);
				}
			},
			\getAddress, {
				"All Jonisks";
			},
			\getBus, {
				jonisks[0].bus;
			}
		]);
	}
	initMIDI{
		MIDIdef.cc(\joniskBrightness, {|val, note| "Brightness MIDI".postln; guiDict[\setBrightness].value(val/128)}, 1);
		MIDIdef.cc(\joniskBrightnessAdd, {|val, note| "Brightness Add MIDI".postln; guiDict[\setBrightnessAdd].value(val/128)}, 2);
		MIDIdef.cc(\joniskAttack, {|val, note| guiDict[\setEnv].value(val.linlin(0, 127, 0, 1), 0)}, 5);
		MIDIdef.cc(\joniskSustain, {|val, note| guiDict[\setEnv].value(val.linlin(0, 127, 0, 2), 1)}, 6);
		MIDIdef.cc(\joniskRelease, {|val, note| guiDict[\setEnv].value(val.linlin(0, 127, 0, 3), 2)}, 7);
	}
	setFramerate{
		|f=30|
		frameRate = f;
		frameDur = 1.0 / f;
	}
	send{
		|msg|
		if(serial != nil, {
			// msg.postln;
			serial.putAll(msg);
		});
	}
	placementGui{
		var a;
		var filePath = "/home/jildert/of_v0.11.2_linux64gcc6_release/apps/TIYCS/placement.csv";
		var dimensions = [10, 5];
		var w = Window("Placement", Rect(0, 600, 1000, 500)).front;
		w.view.palette_(QPalette.dark);
		a = { {
			var width = w.view.bounds.width / dimensions[0];
			var height = w.view.bounds.height / dimensions[1];
			var c = View.new(w, Rect(0, 0, width, 100));
			var b = NumberBox(c,Rect(0, height*0.5 - (17.5), width * 0.9, 25)).value_(-1).background_(Color.gray).maxDecimals_(0).align_(\center);
			c.background = Color(0, 0, 0, 0.1);
			c;
		} ! dimensions[0] } ! dimensions[1];
		w.layout = VLayout(*a.collect { |x| HLayout(*x) });
		a.flat.do({|e| e.children[0].action_(
			{
				|e|
				if(e.value >= 0, {e.background_(Color.white)}, {e.background_(Color.gray)});
				patterns[0] = a.collect({|row, i|
					var r = row.collect({|cell| cell.children[0].value.asInteger});
					if(i.odd, {r = r.reverse});
					r;
				}).flat.select({|e| e >= 0});
				patterns[0].postln; // Chaser per row, not scanning, continous line
				patterns[1] = a.collect({|row, i|
					var r = row.collect({|cell| cell.children[0].value.asInteger});
					r;
				}).flat.select({|e| e >= 0});
				patterns[1].postln; // Chaser per row, scanning
				patterns[2] = a.flop.collect({|row, i|
					var r = row.collect({|cell| cell.children[0].value.asInteger});
					r;
				}).flat.select({|e| e >= 0});
				patterns[2].postln; // Chaser per column, not scanning, continous line
				patterns[3] = a.flop.collect({|row, i|
					var r = row.collect({|cell| cell.children[0].value.asInteger});
					if(i.odd, {r = r.reverse});
					r;
				}).flat.select({|e| e >= 0});
				patterns[3].postln; // Chaser per column, scanning
		})});
		w.view.keyDownAction_({|doc, char, mod, unicode, keycode, key| if(keycode == 115, {
			"Save position mapping".postln;
			File.use("/home/jildert/of_v0.11.2_linux64gcc6_release/apps/TIYCS/placement.csv".standardizePath, "w+", { |f|
				// f.write("Doesn't this work?\n is this thing really on ?\n");
				a.do{|row|
					row.do{|e|
						f.write(e.children[0].value.asString ++ ",");
					};
					f.write("\n");
				}
			});
		})});

		if(File.exists(filePath), {
			var x = CSVFileReader.readInterpret(filePath);
			"CSV: ".postln; x.postln;
			x.do{
				|row, i|
				row.do{
					|e, j|
					if(e != nil, {
						a[i][j].children()[0].valueAction_(e);// = e;
					});
				}
			}
		}, {
			"No placement file found".warn;
		});
	}
}
// This class should handle the SerialPort: initialize it, and pass it to the containing Jonisk objects.
// So it should contain the Jonisk objects.
// It should read an address / ID - list
// It should poll the SerialPort, if new data: write to Jonisk object (or GUI).
//
// It should handle the Bus for the Jonisk objects
//
// Is it possible to give all Jonisk objects their own adjecent bus?
// Like: Jonisk.new(); Jonisk.new(), which will then have Bus index x and x+1?
// Read with: Bus.getn({}) ?
