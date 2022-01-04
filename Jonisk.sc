Jonisk{
	/*
	This should have a Color obj
	This should have a reference to a synth
	*/
	var <>synth;
	*new{
		|bus, index|
		^super.new.init(bus, index);
	}
	init{
		|bus=nil, index|
		if(bus == nil, {
			"Can't create synth, no output bus set".error
		}, {
			synth = Synth(\jonisk, [out: bus.subBus(index*4)]);
		});
	}
	*loadSynthDef{ // This should write a SynthDef file
		SynthDef(\jonisk, {
			|b=1, rgb=#[1,1,1], mode=0, out=0, rgbAdd=#[0,0,0], curve = -4, trigRand=1, a=0.1, s=1.0, r=1.0, gate=0, level=1, noiseMul=0, lagTime=0.01|
			var output;
			var env = EnvGen.kr(Env.linen(a, s, r, level, curve: curve), Changed.kr(gate));
			var brightness = Mix.kr([env, b]).min(1);
			var noise = LFDNoise1.ar(1/4, noiseMul).abs.min(1);
			output = Lag2.kr(brightness , lagTime) + noise * rgb;
			output = (output + rgbAdd).min(1);
			Out.kr(out, output ++ 0); // Also add an empty white channel
		}).load;
	}
}