Jonisk{
	var <>synth;
	var <>address = #[0xFF,0xFF,0xFF,0xFF,0xFF,0xFF];
	var <>	addrToPrint = "0xFF";
	var <>color;
	var <>whiteValue=0;
	var <> serial = nil;
	var end = #[101,110,100];
	var <>id = nil;
	var <>bus;
	var <> batteryPct = 100;
	var < brightness = 1; // Getters
	var < attack = 1;
	var < sustain = 1;
	var < release = 1;
	var <> bLive = false;
	*new{
		|index, serial|
		Jonisk.loadSynthDef();
		^super.new.init(index, serial);
	}
	init{
		|index=nil, serial_|
		serial = serial_;
		color = Color.black;
		color.alpha = 0;
		id = index;
		bus = Bus.alloc(\control, Server.default, 4);
		synth = Synth(\jonisk, [out: bus]);
	}
	*loadSynthDef{ // This should write a SynthDef file
		SynthDef(\jonisk, {
			|b=1, rgbw=#[0,0,0,0], mode=0, out=0, rgbwAdd=#[0,0,0,0], curve = -4, trigRand=1, a=0.1, s=1.0, r=1.0, gate=0, level=1, noiseMul=0, lagTime=0.01|
			var output;
			var env = EnvGen.kr(Env.linen(a, s, r, level, curve: curve), Changed.kr(gate));
			var brightness = Mix.kr([env, b]).min(1);
			var noise = LFDNoise1.ar(1/4, noiseMul).abs.min(1);
			output = Lag2.kr(brightness , lagTime) + noise * rgbw;
			output = (output + rgbwAdd).min(1);
			Out.kr(out, output);
		}).load();
	}
	send{
		|msg|
		if(serial != nil, {
			msg.postln;
			serial.putAll(msg);
		});
	}
	ota{
		var msg;
		msg = address ++ [0x07, 0x00] ++ end;
		this.send(msg);
		"Turn Jonisk into OTA update mode".warn;
	}
	setColor {
		|c = #[255, 0, 0 ,0]|
		var msg;
		color = Color.fromArray(c.at([0,1,2]) / 255).alpha_(c[3] / 255);
		msg = address ++ [0x05] ++ c ++ end;
		if(bLive == false, {
			this.send(msg);
		});
	}
	off {
		this.setColor(0!4);
	}
	testLed{
		var oneColor = [100,0,0,0];
		{
			4.do{
				|i|
				this.setColor(oneColor.rotate(i));
				0.5.wait;
			};
			this.off();
		}.fork;
	}
	requestBattery{
		var msg = address ++ [0x08, 0x00, 0x00] ++ end;
		this.send(msg);
	}
	// For GUI see JoniskGui.sc
	trigger{
		synth.set(\gate, 1.0.rand);
	}
	setAttack{
		|val|
		attack = val;
		synth.set(\a, val);
	}
	setSustain{
		|val|
		sustain = val;
		synth.set(\s, val);
	}
	setRelease{
		|val|
		release = val;
		synth.set(\r, val);
	}
	setBrightess{
		|val|
		brightness = val;
		synth.set(\b, val);
	}
}