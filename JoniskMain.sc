JoniskMain{
	var <> jonisks;
	// var <> serial;
	var <> serialReadRoutine;
	var <> serial; // Use one SerialPort instance
	var <> serialPorts;
	var <> baud = 230400;
	var <> updateRoutine;
	var <> frameDur = 1;
	var <>msgBuffer = #[0,0,0];
	var <> message = "";
	var <>bWriteMsg = false;
	var liveButton;
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
// });
	}
	readConfig{
		var file = JSONFileReader.read("/Users/jildertviet/of_v0.11.2_osx_release/apps/TIYCS/jonisk.config");
		file.do{
			|j|
			var addr = j[1];
			var addrToPrint = addr;
			addr.postln;
			addr = addr.collect({|e| e.split($x)[1].asHexIfPossible});
			jonisks.add(Jonisk(j[0], serial).address_(addr).addrToPrint_(addrToPrint));
		}
		// This loads the Jonisk instances
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
		if(byte == 'g', {
			if(msgBuffer[1] == 's', {
				if(msgBuffer[0] == 'm', {
					bWriteMsg = true;
				});
			});
		});
	}
	checkForMsgEnd{
		|byte|
		if(byte == 'd', {
			if(msgBuffer[1] == 'n', {
				if(msgBuffer[0] == 'e', {
					bWriteMsg = false;
					this.parseMsg();
				});
			});
		});
	}
	parseMsg{
		var msg = message;
		msg = msg.replace("en", ""); // Remove last
		("MSG: " ++ msg).postln;
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
							this.checkForMsgStart(byte);
							this.checkForMsgEnd(byte);
							if(bWriteMsg == true, {
								message = message + byte.asAscii;
							});
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
			v.postln;
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
		updateRoutine.stop;
	}
	gui{
		var w = Window("TIYCS - Jonisk control").front;
		liveButton = Button().string_("Idle").action_({
			|e|
			"X".postln;
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

		w.view.palette_(QPalette.dark);
		w.layout = VLayout(
			HLayout([liveButton, stretch: 1], w.view.bounds.width * 0.8),
			*(jonisks.collect({|e| HLayout(
				[StaticText.new().string_(e.id).background_(Color.black.alpha_(0.1)), stretch: 1],
				[StaticText.new().string_(e.address.asCompileString.replace("\"", "").replace("]", "").replace("[", "")).background_(Color.black.alpha_(0.1)), stretch: 6],
				[StaticText.new().string_(e.batteryPct.asString ++ "%").background_(Color.black.alpha_(0.1)).align_(\center), stretch: 1],
				[Button.new().states_([
					["GUI", Color.white, Color.black.alpha_(0.1)],
				]).action_({var guiWindow = e.gui; var bounds = guiWindow.bounds; guiWindow.bounds_(bounds + Rect(w.bounds.width, 0, 0, 0))}), stretch: 1]
			)}));
		)
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