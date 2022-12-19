JParticles : JEvent{
	var <>numParticles;
	var <>globalForce, <>forceMultiplier;
	var forceMultiplierScale = 1;
	var <>uniqueID = 1;
	createUnique {
		|a|
		// if(layer == nil, {createArgs.add(defaultLayer);}); // Add default layer, so extra arg is not the 3rd, but the 4th
		createArgs.add(true); // !? 2
		createArgs.add(numParticles);
		if(size == nil, {size=[1280, 800]});
		createArgs.add(size[0]);
		createArgs.add(size[1]);
		createArgs.add(uniqueID);

		globalForce = [0,0];
		forceMultiplier = [0,0];
		this.sendMakeCmd("JParticles");
	}
	setNumParticles {
		|n=1000000|
		numParticles = n;
	}
	linkVecField {
		|vecField|
		this.sendMsg("/linkVecField", id, vecField.id);
	}

	setGlobalForceX {
		|x = 0|
		globalForce[0] = x;
		this.setCustomArg(0, x);
		this.doFunc(0); // Update globalForce
	}
	setGlobalForceY {
		|y = 0|
		globalForce[1] = y * -1;
		this.setCustomArg(1, y);
		this.doFunc(0); // Update globalForce
	}
	setGlobalForce {
		|f=#[0,0]|
		globalForce = f;
		this.setCustomArg(0, f[0]);
		this.setCustomArg(1, f[1] * -1);
		this.doFunc(0); // Update globalForce
	}
	setForceMultiplierX {
		|x = 0|
		forceMultiplier[0] = x;
		this.setCustomArg(2, x);
		this.doFunc(1); // Update forceMultiplier
	}
	setForceMultiplierY {
		|y = 0|
		forceMultiplier[1] = y;
		this.setCustomArg(3, y);
		this.doFunc(1); // Update forceMultiplier
	}
	setForceMultiplier {
		|f=#[0,0]|
		forceMultiplier = f;
		this.setCustomArg(2, f[0]);
		this.setCustomArg(3, f[1]);
		this.doFunc(1); // Update forceMultiplier
	}
	setTraagheid {
		|t=0.9|
		this.setCustomArg(4, t);
		this.doFunc(2);
	}
	setAlpha {
		|a=0.2|
		this.setColor([color[0], color[1], color[2], (a*255).asInt]);
		this.doFunc(3);
	}
	setFadeTime{
		|fT=0.001|
		this.setCustomArg(0, fT);
		this.doFunc(4);
	}
	gui {
		var t;
		~psGui = Window.new("particleSystem", Rect(0, 400, 400, 400)).front;
		t = Slider2D(~psGui, Rect(20, 20, 80, 80))
		.x_(globalForce[0]+0.5) // initial location of x
		.y_(globalForce[1]+0.5)   // initial location of y
        .action_({|sl|
			var pos = [sl.x, sl.y];
			pos = pos - 0.5; // 0 <> 1 to -0.5 <> 0.5 range
			pos = pos * 2; // To -1 <> 1 range
			this.setGlobalForce(pos);
        });
		StaticText(~psGui, t.bounds + Rect(100, 0, 0, 0)).string_("globalForce");
		t = Slider2D(~psGui, Rect(20, 120, 80, 80))
        .x_(0) // initial location of x
        .y_(0)   // initial location of y
        .action_({|sl|
			var force = [sl.x, sl.y] * forceMultiplierScale.value; // 0-1 range to 0 - 10
			force = force + 1;
			this.setForceMultiplier(force);
        });
		StaticText(~psGui, t.bounds + Rect(100, 0, 20, 0)).string_("forceMultiplier");
		EZSlider.new(~psGui, Rect(80, 120, 200, 15), "mul", ControlSpec.new(1, 50)).action_({|value| forceMultiplierScale = value});
		EZSlider.new(~psGui, Rect(0, 220, 200, 15), "traagHeid", ControlSpec.new(0.0001, 0.99999), initVal: 0.5).action_({|slider|
			this.setTraagheid(slider.value);
		});
		EZSlider.new(~psGui, Rect(0, 240, 200, 15), "fadeTime", ControlSpec.new(0.0001, 0.1, step: 0.0001), initVal: 0.0001).action_({|slider|
			this.setFadeTime(slider.value);
		});
	}
}
