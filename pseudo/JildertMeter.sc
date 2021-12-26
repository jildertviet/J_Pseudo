JildertMeter{
	*kr {|name="JildertMeter", in, id=0, netAddr=nil, netAddr2=nil, lagTime=0, frameRate=25, muteOnset=1|
		var out, send;
		var slope, osc;
		var onset;

		// "JildertMeter made".postln;

		if(lagTime!==0, {in = Lag.kr(in, lagTime);}); // Only apply lag if lagTime>0

		slope = Slope.kr(in);
		slope = slope.abs; // Of ook negatief laten worden?


		// onset = Jildert.ar(in, 1.3, muteOnset);

		// send = SendTrig.kr(onset,[0,1]+id+1000);
		// send = SendTrig.kr(Impulse.kr(frameRate),[0,1]+id,[in, slope]);

/*		osc = OSCFunc({ |msg|
			var id, value;
			id = msg[2];
			value = msg[3];
			if(id<1000,{
					netAddr.sendMsg("/"++id, 0, value) ; // 0 is continous signal
					netAddr2.sendMsg("/"++id, 0, value) ; // 0 is continous signal
				}, {
					netAddr.sendMsg("/"++id, 1, value) ; // 1 is onset message
					netAddr2.sendMsg("/"++id, 1, value) ; // 1 is onset message
			});
		}, '/tr');*/

		// out=[in, slope];
		out = in;
		^out
	}
}