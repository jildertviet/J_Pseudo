
/*
JoniskMain{
	var <> jonisks;
*new{
		||
		^super.new.init();
	}
	init{
		||
		jonisks = List.new();
		File.open(
	}
}


This class should handle the SerialPort: initialize it, and pass it to the containing Jonisk objects.
So it should contain the Jonisk objects.
It should read an address / ID - list
It should poll the SerialPort, if new data: write to Jonisk object (or GUI).

It should handle the Bus for the Jonisk objects

Is it possible to give all Jonisk objects their own adjecent bus?
Like: Jonisk.new(); Jonisk.new(), which will then have Bus index x and x+1?
Read with: Bus.getn({}) ?




//read 10bit serial data sent from Arduino's Serial.println
(
r.stop;
r= Routine({
    var byte, str, res;
    99999.do{|i|
		// ".".postln;
		// if(p.read, {
		// str = "";
			byte = p.next;
		if(byte != nil, {
			byte.postln;
		}
		);
// });
		0.1.wait;
    };
}).play;
)
*/