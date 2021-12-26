JildertTest{
	classvar <>testVal = 13;
	*new{
		^super.new.init;
	}
	*ar{|b|
		var out, id, osc, send;
		id = 345;
		out = SinOsc.ar(100);
		send = SendTrig.kr(Impulse.kr(10),id,out);
		osc = OSCFunc({ |msg|
			{
				var value = msg[3];
				value.postln;
				value = this.functie;
				value.postln;
			}.defer;
		},'/tr');
		^out
	}
	init{
		testVal = 12;
	}
	functie{
		^testVal;
	}
}