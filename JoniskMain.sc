JoniskMain{
	var <> jonisks;
	// var <> serial;
	var <> serialReadRoutine;
	var <> serial; // Use one SerialPort instance
	var <> serialPorts;
	var <> baud = 230400;
	var <> updateRoutine;
	var <> frameDur = 0.04;
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
	*new{
		^super.new.init();
	}
	init{
		serialPorts = List.new();
		jonisks = List.new();
		this.openDefaultSerial();
		this.initSerial();
		// Server.default.waitForBoot({
		this.readConfig(); // Creates Jonisk objects, that need a Bus allocated on the server
		states = (0)!jonisks.size;
		lastSeen = (0)!jonisks.size;
		this.checkLastSeen();
		this.iniTestPattern();
// });
	}
	readConfig{
		var file = JSONFileReader.read("/Users/jildertviet/of_v0.11.2_osx_release/apps/TIYCS/jonisk.config");
		file[0]["activeJonisks"].do{
			|j, i|
			var addr = j[0];
			var addrToPrint = addr;
			addr.postln;
			addr = addr.collect({|e| e.split($x)[1].asHexIfPossible});
			jonisks.add(Jonisk(i, serial).address_(addr).addrToPrint_(addrToPrint));
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
							{states[i].value_(0)}.defer;
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
		};
		portsToOpen.do{
			|e, i|
			(e ++ " opened").postln;
			serialPorts.add(SerialPort.new(e, baud, crtscts: true));
		};
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
				});
			});
		});
	}
	parseMsg{
		var msg = message;
		msg = msg.replace("en", ""); // Remove last
		("\nMSG: " ++ msg).postln;
		if(msg.find("b") == 0, { // Battery update
			var addr = (0!6);
			var batteryVoltage;
			msg = msg.replace("b", "");
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
		if(msg.find("a") == 0, {
			var addr = (0!6);
			var batteryVoltage;
			msg = msg.replace("a", "");
			for(0, 5, {|e, i| addr[i] = msg.split($:)[i].asInteger});
			addr = addr + [0, 0, 0, 0, 0, 1];
			addr.postln;
			batteryVoltage = msg.split($:).last.asInteger;
			batteryVoltage = batteryVoltage / 2.pow(12); // 0V is 0, 16.8V is 4096. Value is now a percentage / 100.
			batteryVoltage = batteryVoltage - (2/3); // 0.66 is shut-down voltage
			batteryVoltage = (batteryVoltage * 300).asInteger; // 1/3 is scaled back to 100%
			jonisks.do{
				|e, i|
				if(e.address == addr, {
					{states[i].value_(1)}.defer;
					e.setBatteryPct(batteryVoltage);
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
							this.checkForMsgEnd(byte);
							// msgBuffer.postln;
							if(bWriteMsg == true, {
								message = message ++ byte.asAscii;
							});
							this.checkForMsgStart(byte);
							if(byte < 127, {
								if(byte == 10, {
									"".postln;
								}, {
									byte.asAscii.post;
								});
							});
							byte = serial.next;
						});
						0.1.wait;
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
			var msg = (0xFF!6) ++ [0x05] ++ ((v*255).asInteger) ++ end;
			msg.postln;
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
		window = Window("TIYCS - Jonisk control").front;
		window.bounds_(window.bounds.width_(600));
		window.bounds_(window.bounds.width_(600));
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

		window.view.palette_(QPalette.dark);
		states = Array.fill(jonisks.size, {Button().states_([
					["", Color.grey, Color.grey],
					["", Color.grey, Color.green],
		])});
		window.layout = VLayout(
			HLayout(
				[liveButton],
				// [NumberBox().value_(frameDur).action_({|e| frameDur = e.value}).normalColor_(Color.white)],
				[PopUpMenu().items_([10, 25, 30, 60]).stringColor_(Color.white).action_({
					|menu|
					menu.item.postln;
					frameDur = (1/menu.item);
					frameRate = menu.item;
				}).value_(frameRateIndex)],
				Button().string_("Global").action_({this.openGlobalGui}),
				Button().string_("Config").action_({this.configLights}),
				Button().string_("Test pattern").action_({this.toggleTestPatttern()}),
/*				window.view.bounds.width * 0.25,*/
			),
			*(jonisks.collect({|e, i| HLayout(
				[StaticText.new().string_(e.id).background_(Color.black.alpha_(0.1)), s: 10],
				[StaticText.new().string_(e.addrToPrint.asCompileString.replace("\"", "").replace("]", "").replace("[", "")).background_(Color.black.alpha_(0.1)), s: 100],
				[e.createBatteryField(), s: 10],
				[Button().states_([
					["GUI", Color.white, Color.black.alpha_(0.1)],
				]).action_({var guiWindow = e.gui; var bounds = guiWindow.bounds; guiWindow.bounds_(bounds + Rect(window.bounds.width, 0, 0, 0))}), s:10],
				[Button().states_([
					["Test", Color.white, Color.black.alpha_(0.1)],
				]).action_({e.testLed()}), s:10],
				[states[i], s: 1]
			)}));
		);
	}
	configLights{
		{
			jonisks.do{
				|jonisk, i|
				var addr = jonisk.address;
				var id = i;
				var end = [101,110,100];
				serial.putAll(addr ++ [0x03, id] ++ end);
				0.1.wait;
			};
			"Config lights done".postln;
		}.fork
	}
	openGlobalGui{
		var bounds;
		var guiWindow = Jonisk.getGuiWindow(nil, Dictionary.newFrom([
			\test, {"Inactive".postln},
			\ota, {},
			\battery, {},
			\testEnv, {jonisks.do{|j| j.trigger()}},
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
			\getAddress, {
				"All Jonisks";
			},
			\getBus, {
				jonisks[0].bus;
			}
		]));
		bounds = guiWindow.bounds;
		guiWindow.bounds_(bounds + Rect(window.bounds.width, 0, 0, 0));
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
				\delta, 1
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