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
	var <> batteryPct = 0;
	var <> batteryPctField;
	var <> fwVersionField;
	var < brightness = 0.3; // Getters
	var < attack = 1;
	var < sustain = 1;
	var < release = 1;
	var <> bLive = false;
	var <>brightnessAdd = 0.5;
	var <>colorMap = #[0,1,2,3];
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
		synth = Synth(\jonisk, [out: bus, curve: 0]);
	}
	*loadSynthDef{ // This should write a SynthDef file
		SynthDef(\jonisk, {
			|
			brightnessAdd=1, rgbw=#[0,0,0,0], mode=0, out=0, rgbwAdd=#[0,0,0,0], curve = 0,
			trigRand=1, a=0.1, s=1.0, r=1.0, gate=0, level=1, noiseMul=0, lagTime=0.01,
			colorMap=#[0,1,2,3]
			|
			var output;
			var env = EnvGen.kr(Env.linen(a, s, r, level, curve: curve), Changed.kr(gate));
			var brightness = Mix.kr([env, Lag.kr(brightnessAdd, 1)]).min(1);
			var noise = LFDNoise1.ar(1/4, noiseMul).abs.min(1);
			output = Lag2.kr(brightness , lagTime) + noise * rgbw;
			output = (output + rgbwAdd).min(1);
			output = Select.ar(colorMap, output);
			Out.kr(out, output * \amp.kr(1, 1));
		}).load();
	}
	send{
		|msg|
		if(serial != nil, {
			// msg.postln;
			serial.putAll(msg);
		});
	}
	ota{
		|ssid="x", password="x"|
		var msg;
		msg = 0xFF!6 ++ [0x07] ++ address ++ ssid ++ ";" ++ password ++ end;
		this.send(msg);
		"Turn Jonisk into OTA update mode".warn;
	}
	setColor {
		|c = #[255, 0, 0 ,0]|
		var msg;
		color = Color.fromArray(c.at([0,1,2]) / 255).alpha_(c[3] / 255);
		if(bLive == false, {
			var data = 0x00!(4*30); // 30 Jonisks
			c.do{|v, i| data[(id*4)+i] = v};
			msg = 0xFF!6 ++ [0x05] ++ data ++ end;
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
		var msg = 0xFF!6 ++ [0x08] ++ address ++ end;
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
	setBrightness{
		|val=1.0|
		brightness = val;
		synth.set(\amp, val);
	}
	setBrightnessAdd{
		|val=1.0|
		brightnessAdd = val;
		synth.set(\brightnessAdd, val);
	}
	createBatteryField{
		batteryPctField = StaticText.new().background_(Color.black.alpha_(0.1)).align_(\center).string_((batteryPct.asString) ++ "%");
		^ batteryPctField;
	}
	createFwVersionField{
		fwVersionField = StaticText.new().background_(Color.black.alpha_(0.1)).align_(\center).string_("...");
		^ fwVersionField;
	}
	setBatteryPct{
		|v|
		batteryPct = v;
		{batteryPctField.string_((batteryPct.asString) ++ "%");}.defer;
	}
	deepSleep{
		|minutes=1|
		var msg;
		msg = 0xFF!6 ++ [0x10] ++ address ++ minutes.asInteger.asInt16.asBytes ++ end;
		this.send(msg);
	}
	setOTAServer{
		|ssid="", password="", url=""|
		var json = "{\"ssid\":\""++ ssid ++"\", \"password\":\""++password++"\", \"url\":\""++url++"\"}";
		var msg = 0xFF!6 ++ [0x15] ++ address ++ json ++ end;
		this.send(msg);
	}
}