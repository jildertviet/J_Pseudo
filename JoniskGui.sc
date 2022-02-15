+ Jonisk {
	gui{
		var window;
		var colorSliders;
		var windowName = if(id != nil, {"Jonisk " ++ id.asString}, {"Jonisk x"});
		window = Window(windowName).front.setInnerExtent(400, 600);
		window.view.palette_(QPalette.dark);
		window.view.decorator_(FlowLayout(window.view.bounds));
		window.view.decorator.left = 25;
		[
			["Test",{this.testLed()}],
			["OTA", {this.ota()}],
			["Battery",{this.requestBattery()}],
			["TestEnv",{this.trigger()}]
		].do{
			|e|
			Button.new(window, 80@40).string_(e[0]).action_(e[1]);
		};
		colorSliders = Array.fill(4, {
			|i|
			var slider = EZSlider.new(window, label:"RGBW".at(i), controlSpec: ControlSpec(0, 255, 'lin')).value_(color.toJV[i]).action_({
				|e|
				var newColor = [color.red, color.green, color.blue, color.alpha];
				newColor[i] = e.value / 255;
				color = Color(newColor[0], newColor[1], newColor[2], newColor[3]);
				color.postln;
				this.setColor(color.toJV ++ (color.alpha * 255));
				// [color.red, color.green, color.blue, color.alpha].postln;
				synth.set(\rgbw, color.asArray);
			});
			slider.setColors(numNormalColor: Color.white);
		});
		Array.fill(3, {
			|i|
			// var functions = [this.setAttack, this.setSustain, this.setRelease];
			var slider = EZSlider.new(window, label:"ASR".at(i), controlSpec: ControlSpec(0, [4,10,10].at(i), 'lin')).value_([attack, sustain, release].at(i)).action_({
				|e|
				switch(i, 0, this.setAttack(e.value), 1, this.setSustain(e.value), 2, this.setRelease(e.value));
				// functions[i].value(e.value);
				// synth.set([\a,\s,\r].at(i), e.value);
			});
			slider.setColors(numNormalColor: Color.white);
		});
		EZSlider.new(window, label:"Brightness", controlSpec: ControlSpec(0, 1, 'lin', 0.01), labelWidth: 80).value_(brightness).action_({
			|e|
			this.setBrightness(e.value);
		}).setColors(numNormalColor: Color.white);
		window.view.decorator.left = 25;
		window.view.decorator.top = window.view.decorator.top + 25;
		TextView.new(window, Rect(0,0, window.bounds.width - 70, 28)).string_(addrToPrint.asString).editable_(false).hasVerticalScroller_(false);
		window.view.decorator.left = 25;
		window.view.decorator.top = window.view.decorator.top + 30;
		Stethoscope.new(Server.default, bus.numChannels, bus.index, rate: 'control', view: window.view);
		^window;
	}
}