+ Jonisk {
	*getGuiWindow{
		|object, functions|
		// "getGuiTest".postln;
		var window;
		var id = if(object != nil, {object.id}, {"X"});
		var colorSliders;
		var windowName = if(id != nil, {"Jonisk " ++ id.asString}, {"Jonisk x"});
		window = Window(windowName).front.setInnerExtent(400, 600);
		window.view.palette_(QPalette.dark);
		window.view.decorator_(FlowLayout(window.view.bounds));
		window.view.decorator.left = 25;
		if(functions==nil, {
			functions = Dictionary.newFrom([
				\test, {object.testLed()},
				\ota, {object.ota()},
				\battery, {object.requestBattery()},
				\testEnv, {object.trigger()},
				\deepsleep, {object.deepSleep()},
				\setColor, {
					|e, i|
					var color = object.color;
					var newColor = [color.red, color.green, color.blue, color.alpha];
					newColor[i] = e.value / 255;
					object.color = color = Color(newColor[0], newColor[1], newColor[2], newColor[3]);
					object.setColor(color.toJV ++ (color.alpha * 255));
					object.synth.set(\rgbw, color.asArray);
				},
				\getColor, {
					|i|
					// Color.white.toJV[i];
					object.color.toJV[i];
				},
				\getEnv, {
				|i|
					[object.attack, object.sustain, object.release].at(i)
				},
				\setEnv, {
					|e, i|
					i.postln;
					switch(i,
						0, {object.setAttack(e.value)},
						1, {object.setSustain(e.value)},
						2, {object.setRelease(e.value)}
					);
				},
				\getBrightness, {
					object.brightness;
				},
				\setBrightness, {
					|e|
					object.setBrightness(e.value);
				},
				\getAddress, {
					object.addrToPrint.asString
				},
				\getBus, {
					object.bus;
				}
			]);
		});
		[
			["Test",functions[\test]],
			["OTA", functions[\ota]],
			["Battery",functions[\battery]],
			["TestEnv",functions[\testEnv]],
			["Sleep",functions[\deepsleep]],
		].do{
			|e|
			Button.new(window, 60@40).string_(e[0]).action_(e[1]);
		};
		colorSliders = Array.fill(4, {
			|i|
			var slider = EZSlider.new(window, label:"RGBW".at(i), controlSpec: ControlSpec(0, 255, 'lin')).value_(functions[\getColor].value(i)).action_({
				|e|
				functions[\setColor].value(e, i);
			}
			);
			slider.setColors(numNormalColor: Color.white);
		});
		Array.fill(3, {
			|i|
			// var functions = [this.setAttack, this.setSustain, this.setRelease];
			var slider = EZSlider.new(window, label:"ASR".at(i), controlSpec: ControlSpec(0, [4,10,10].at(i), 'lin')).value_(functions[\getEnv].value(i)).action_({
				|e|
				functions[\setEnv].value(e, i);
				// functions[i].value(e.value);
				// synth.set([\a,\s,\r].at(i), e.value);
			});
			slider.setColors(numNormalColor: Color.white);
		});
		EZSlider.new(window, label:"Brightness", controlSpec: ControlSpec(0, 1, 'lin', 0.01), labelWidth: 80).value_(functions[\getBrightness].value()).action_({
			|e|
			functions[\setBrightness].value(e);
		}).setColors(numNormalColor: Color.white);
		window.view.decorator.left = 25;
		window.view.decorator.top = window.view.decorator.top + 25;
		TextView.new(window, Rect(0,0, window.bounds.width - 70, 28)).string_(functions[\getAddress].value()).editable_(false).hasVerticalScroller_(false);
		window.view.decorator.left = 25;
		window.view.decorator.top = window.view.decorator.top + 30;
		Stethoscope.new(Server.default, functions[\getBus].value().numChannels, functions[\getBus].value().index, rate: 'control', view: window.view);
		^window;
	}
	gui{
		^Jonisk.getGuiWindow(this);
	}
}