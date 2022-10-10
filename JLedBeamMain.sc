JLedBeamMain : JoniskMain{
	var <> oscDef;
	var <> updateNegativeMaskFunc;
	var <> negativeMask;
	uniqueInit{ // Override
		negativeMask = List.new();
	}
	createChildObject{
		|i, serial|
		^JLedBeam.new(i, serial);
	}
	listenForOSC{
		oscDef = OSCdef(\maskLines, {
			|msg|
			// msg.postln;
			negativeMask.clear();
			msg.removeAt(0); // Remove address
			(msg.size/2).do{
				|i|
				var line = [msg[i*2], msg[i*2+1]];
				negativeMask.add(line);
			};
			// negativeMask.postln;
		}, "/maskLines", recvPort: 7575);
	}
	updateNegativeMask{
		|on=true|
		updateNegativeMaskFunc.stop;
		if(on, {
			updateNegativeMaskFunc = {
				inf.do{
					// Construct the message
					var msg = 0xFF!6 ++ 0x16 ++ negativeMask.asArray.reshape(jonisks.size*2) ++ "end";
					// msg.postln;
					this.send(msg);
					frameDur.wait;
				}
			}.fork;
		});
	}
}

// [[0,2]].reshape(2)