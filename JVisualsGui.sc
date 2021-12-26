+ JVisuals {
	gui{
		if(guiWindow.isNil, {
			var size = 380@20;
			var small = 300@20;
			var half = ((380/2)-10)@20;
			var colorBlockSize = 40@20;
			guiWindow = Window(" Visuals GUI", Rect(0, 0, 400, 400)).front; // OR USE THIS!?
			guiWindow.view.decorator = FlowLayout(guiWindow.view.bounds, 10@10, 20@5);

			Button.new(guiWindow, small).states_([["Select bgColor", Color.black, Color.white]]).action_({
				|e| this.setBackground()});
			Button.new(guiWindow, colorBlockSize).states_([["", Color.blue, bgColor]]).enabled_(false);
			brightnessSlider = EZSlider.new(guiWindow, size, "brightness", ControlSpec(0, 255), {|e| this.setBrightness(e.value)}, initVal: 255, labelWidth: 100);
			alphaSlider = EZSlider.new(guiWindow, size, "alpha [TODO]", ControlSpec(0, 255), {|e| this.setAlpha(e.value)}, initVal: 255, labelWidth: 100);
			maskBrightnessSlider = EZSlider.new(guiWindow, size, "mask brightness", ControlSpec(0, 255), {|e| this.setMaskBrightness(e.value)}, initVal: 0, labelWidth: 100);
			Button.new(guiWindow, half).states_([["Circular mask", Color.black, Color.red],["Circular mask", Color.black, Color.green]]).action_({
				|e|
				~visualUDP.sendMsg("/setCircularMaskAlpha", e.value); // expects bool
			}).value_(0);
			Button.new(guiWindow, half).states_([["Cam", Color.black, Color.red],["Cam", Color.black, Color.green]]).action_({
				|e|
				this.setCamEnabled(e.value);
			}).value_(1);
			Button.new(guiWindow, half).states_([["Test rect", Color.black, Color.white]]).action_({
				var r = JRectangle.new(); r.test(); // Creates it at [0,0] w/ size [100,100]x
			});
			Button.new(guiWindow, half).states_([["Kill all", Color.black, Color.white]]).action_({this.killAll()});
			EZListView.new(guiWindow,
				size * [1,1+connectedDeviceNames.size],
				"Connected MIDI devices:",
				connectedDeviceNames.asArray,
				// globalAction: { |a| ("this is a global action of "++a.asString ).postln },
				initVal: 0,
				initAction: false,
				labelWidth: 120,
				labelHeight: 16,
				layout: \vert,
				gap: 2@2
			);
		}, {
			if(guiWindow.isClosed,{guiWindow = nil; this.gui()},{
				guiWindow.front();
			});
		});
	}
}