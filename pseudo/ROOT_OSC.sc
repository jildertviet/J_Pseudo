ROOT_OSC{
	*kr {|name="JildertMeter", in, id=0, netAddr=nil, netAddr2=nil, lagTime=0, frameRate=25, muteOnset=1|
		var out, send;
		var onset;

		"ROOT_OSC made".postln;

		out = SinOsc.ar(200);
		// send = SendTrig.kr(Impulse.kr(frameRate),[0,1]+id,[in]);

/*		osc = OSCFunc({ |msg|
			var id, value;
			id = msg[2];
			value = msg[3];
			netAddr.sendMsg("/"++id, 0, value) ; // 0 is continous signal
		}, '/tr');*/

		^out
	}

	test {
		|a|
		"test".postln;
	}
}