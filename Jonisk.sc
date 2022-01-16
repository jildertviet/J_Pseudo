Jonisk{
	/*
	This should have a Color obj
	This should have a reference to a synth
	*/
	var <>synth;
	var <>address = #[0xFF,0xFF,0xFF,0xFF,0xFF,0xFF];
	var <>color;
	var <>whiteValue=0;
	var <> serial = nil;
	var end = #[101,110,100];
	var id = nil;
	*new{
		|bus, index, serial|
		^super.new.init(bus, index, serial);
	}
	init{
		|bus=nil, index=nil, serial_|
		if(bus == nil, {
			"Can't create synth, no output bus set".error
		}, {
			synth = Synth(\jonisk, [out: bus.subBus(index*4)]);
		});
		serial = serial_;
		color = Color.black;
		id = index;
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
	ota{
		var msg;
		msg = address ++ [0x07, 0x00] ++ end;
		serial.putAll(msg);
		"Turn Jonisk into OTA update mode".warn;
	}
	setColor {
		|c = #[255, 0, 0 ,0]|
		var msg;
		msg = address ++ [0x05] ++ c ++ end;
		serial.putAll(msg);
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
		serial.putAll(msg);
	}
	gui{
		var window;
		var colorSliders;
		var windowName = if(id != nil, {"Jonisk " ++ id.asString}, {"Jonisk x"});
		window = Window(windowName).front.setInnerExtent(400, 400);
		window.view.palette_(QPalette.dark);
		window.view.decorator_(FlowLayout(window.view.bounds));
		window.view.decorator.left = 25;
		[
			["Test",{this.testLed()}],
			["OTA", {this.ota()}],
			["Battery",{this.requestBattery()}],
			["",{}]].do{|e|Button.new(window, 80@80).string_(e[0]).action_(e[1])};
		colorSliders = Array.fill(4, {
			|i|
			var slider = EZSlider.new(window, label:"RGBW".at(i), controlSpec: ControlSpec(0, 255, 'lin')).value_(0).action_({|e| var newColor = color.toJV; newColor[i] = e.value; color = Color.new255(newColor[0], newColor[1], newColor[2], newColor[3]); color.postln});
			slider.setColors(numNormalColor: Color.white);
		});
		window.view.decorator.left = 25;
		TextView.new(window, Rect(0,0, window.bounds.width - 70, 28)).string_(address.asString).editable_(false).hasVerticalScroller_(false);
	}
}
/*
JoniskGroup{
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
}*/