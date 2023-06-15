JLedBeam : Jonisk{
	var broadcastAddr = #[0xFF,0xFF,0xFF,0xFF,0xFF,0xFF];
	setPixelsGrayScale{
		|id=0, pixels = 0, dur=100, delayTime=0, retry=0|
		var msgId = 0; // ??? This comes from JLCD ...
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 8];
		msg = msg ++ dur ++ pixels;
		msg = address ++ [1] ++ [msg.size+1] ++ msg ++ [address.size + msg.size + end.size + 3] ++ end;
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
	setPixels{
		|id=0, pixels = 0, dur=100,delayTime=0, retry=0|
		var msgId = 0; // ??? This comes from JLCD ...
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 9];
		msg = msg ++ dur ++ pixels;
		msg = address ++ [1] ++ [msg.size+1] ++ msg ++ [address.size + msg.size + end.size + 3] ++ end;
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
	testLed{ // Override
		var msg = (0xFF!6) ++ [0x15] ++ address ++ "end";
		msg.postln;
		this.send(msg);
	}
	checkIfBroadcast{
		|broadcast|
		if(broadcast == true, {^broadcastAddr}, {^address});
	}
	setMode{
		|mode="oscillator", optionsHint="oscillator, perlin"|
		var m, a;
		var msg;
		switch(mode,
			"oscillator", {m = 0x01},
			"perlin", {m = 0x07},
		);
		a = this.checkIfBroadcast(true);
		msg = a ++ [0x10, m] ++ "end";
		this.send(msg);
	}
	setFrequency{
		|f=1.0, broadcast=false|
		var a = this.checkIfBroadcast(broadcast);
		if(broadcast == true, {
			this.send(a++[0x08, 0x00]++[0]++(f.asFloat.asBytes)++"end") // Set freq
		}, {
			this.send(broadcastAddr++[0x17]++a++[0x00]++[0]++(f.asFloat.asBytes)++"end") // 0 = oscID
		});
	}
	setRange{
		|r=1.0, broadcast=false|
		var a = this.checkIfBroadcast(broadcast);
		if(broadcast, {
			this.send(a++[0x08, 0x03]++[0]++(r.asFloat.asBytes)++"end") // Set range
		}, {
			this.send(broadcastAddr++[0x17]++a++[0x03]++[0]++(r.asFloat.asBytes)++"end") // 0 = oscID
		});
	}
	setWavetable{
		|id=0, broadcast=false, optionsHint="range: 0-7"|
		var a = this.checkIfBroadcast(broadcast);
		if(broadcast, {
			this.send(a++[0x08, 0x04]++[0]++(id)++"end")
		}, {
			this.send(broadcastAddr++[0x17]++a++[0x04]++[0]++(id)++"end")
		});

	}
	setAddValue{
		|value=0.0, broadcast=false, optionsHint="0.0 - 1.0"|
		var a = this.checkIfBroadcast(broadcast);
		var msg;
		if(broadcast, {
			msg = broadcastAddr++[0x08]++[0x06]++(value.asFloat.asBytes)++"end";
		}, {
			msg = broadcastAddr++[0x17]++a++[0x06]++(value.asFloat.asBytes)++"end";
		});
		this.send(msg) // Set add value (0 - 1)
	}
	doEnv{
		|attack=100, sustain=500, release=1000, brightness=1.0, broadcast=false|
		var a = this.checkIfBroadcast(broadcast);
		if(broadcast == true, {
			this.send(a++[0x08, 0x05]++(attack.asFloat.asBytes)++(sustain.asFloat.asBytes)++(release.asFloat.asBytes)++(brightness.asFloat.asBytes)++"end") // Envelope
		}, {
			this.send(broadcastAddr++[0x17]++a++[0x05]++(attack.asFloat.asBytes)++(sustain.asFloat.asBytes)++(release.asFloat.asBytes)++(brightness.asFloat.asBytes)++"end") // Envelope
		});
	}
	setPhase{

	}
	setColorAll{
		|r=200, g=200, b=200|
		var a = this.checkIfBroadcast(true);
		this.send(a ++ [0x06] ++ [r,g,b] ++ "end");
	}
}