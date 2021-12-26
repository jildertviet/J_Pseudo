JPhysarum : JEvent{
	var <>sensorAngle, sensorDistance, turnAngle, speedMul, decay, deposit, balance;
	createUnique {
		if(layer == nil, {createArgs.add(defaultLayer);}); // Add default layer, so extra arg is not the 3rd, but the 4th
		if(size.isNil, {size = [1280, 800]});
		createArgs.add(this.size[0]);
		createArgs.add(this.size[1]);
		createArgs.postln;
		this.sendMakeCmd("JPhysarum");
	}
	setSensorAngle{
		|angle = 45|
		sensorAngle = angle;
		this.setVal("sensorAngle", sensorAngle);
	}
	setSensorDistance{
		|distance=20|
		sensorDistance = distance;
		this.setVal("sensorDistance", sensorDistance);
	}
	setTurnAngle{
		|angle=45|
		turnAngle = angle;
		this.setVal("turnAngle", turnAngle);
	}
	setSpeedMul{
		|mul=1|
		speedMul = mul;
		this.setVal("speed", speedMul); // Use an already existing name
	}
	setDecay{
		|decay=0.5|
		decay = decay;
		this.setVal("decay", decay);
	}
	setDeposit{
		|deposit=40|
		deposit = deposit;
		this.setVal("deposit", deposit);
	}
	setBalance{
		|balance=0.5|
		balance = balance;
		this.setVal("balance", balance);
	}
	gui{
		var sliderDimensions = this.basicGui();
		EZSlider(guiWindow, sliderDimensions, "sensorAngle", ControlSpec(1, 360), {|e|this.setSensorAngle(e.value)}, 45, labelWidth: 100);
		EZSlider(guiWindow, sliderDimensions, "sensorDistance", ControlSpec(0.1, 500), {|e|this.setSensorDistance(e.value)}, 20, labelWidth: 100);
		EZSlider(guiWindow, sliderDimensions, "turnAngle", ControlSpec(1, 360), {|e|this.setTurnAngle(e.value)}, 45, labelWidth: 100);
		EZSlider(guiWindow, sliderDimensions, "speed", ControlSpec(0.01, 20), {|e|this.setSpeedMul(e.value)}, 1, labelWidth: 100);
		EZSlider(guiWindow, sliderDimensions, "decay", ControlSpec(0, 1), {|e|this.setDecay(e.value)}, 0.5, labelWidth: 100);
		EZSlider(guiWindow, sliderDimensions, "deposit", ControlSpec(0, 255), {|e|this.setDeposit(e.value)}, 40, labelWidth: 100);
		EZSlider(guiWindow, sliderDimensions, "balance", ControlSpec(0, 1), {|e|this.setBalance(e.value)}, 0.5, labelWidth: 100);
		guiWindow.setInnerExtent(guiWindow.bounds.width, guiWindow.bounds.height + (25*7));
	}
}