JoniskMain{
	var <> jonisks;
	// var <> serial;
	var <> serialReadRoutine;
	var <> serial; // Use one SerialPort instance
	var <> serialPorts;
	var <> baud = 230400;
	*new{
		^super.new.init();
	}
	init{
		serialPorts = List.new();
		jonisks = List.new();
		this.readConfig();
		this.initSerial();
	}
	readConfig{
		var file = JSONFileReader.read("/Users/jildertviet/of_v0.11.2_osx_release/apps/TIYCS/jonisk.config");
		file.do{
			|j|
			var addr = j[1];
			addr.postln;
			addr.collect({|e| e.split($x)[1].asHexIfPossible});
			jonisks.add(Jonisk(j[0], serial).address_(addr));
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
				if(e.find("usbserial") != nil, {
					e.postln;
					portsToOpen.add(e);
			})});
		};
		portsToOpen.do{
			|e, i|
			(e ++ " opened").postln;
			serialPorts[i] = SerialPort.new(e, baud, crtscts: true);
		};
	}
	initSerial {
		|serialID=0|
		serial = serialPorts[serialID];
		if(serial != nil, {
			if(serial.isOpen, {
				serialReadRoutine = Routine({
					var byte;
					inf.do{|i|
						byte = serial.next;
						while(byte != nil, {
							byte.postln;
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
	gui{
		var w = Window("TIYCS - Jonisk control").front;
		var v;
		w.view.decorator_(FlowLayout(w.view.bounds));
		w.view.palette_(QPalette.dark);
		v = ListView(w, w.view.bounds)
		.items_(jonisks.collect({|e|e.id ++ " - " ++ e.address.asCompileString.replace("\"","")}))
		.background_(Color.clear)
		.hiliteColor_(Color.white().alpha_(0.1))
		.selectedStringColor_(Color.black)
		.action_({ arg sbs;
			[sbs.value, v.items[sbs.value]].postln; // .value returns the integer
		});
		v.enterKeyAction = {
			|a, b|
			("Open Jonisk " ++ v.items[a.value].split($-)[0] ++ ", for now just use the array: FIX").postln;
			jonisks[a.value].gui;
		};
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