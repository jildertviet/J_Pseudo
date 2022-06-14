+ TIYCS {
	loadScene{ |key|
		("Scene " ++ key.asString ++ " loaded").postln;
		scenesDict[key][1].value();
	}
	getWindowByName{ // Returns the first window
		|name|
		var w = List.new();
		Window.allWindows.collect({|x| if(x.name == name, {w.add(x)})});
		^w[0];
	}
	automate{
		var automationWindow = Window.new("Automate");
		var automationCounter;
		this.gui;
		this.valyueById(2, 255); // Brightness full
		automationWindow.front;
		automationCounter = NumberBox(automationWindow).step_(1).action_({
			|e|
			var value = e.value.asInteger;
			switch(value,
				0, {
					this.loadScene(\Intro);
					},
				1, {
					var button;
					var w;
					"Rotate".postln;
					w = this.getWindowByName("TIYCS - Intro");
					button = w.view.children[0];
					button.postln;
					button.valueAction_(1); // Rotate
				},
				2, {
					this.loadScene(\Instructions);
				},
				3, {counter.valueAction_(1)},
				4, {counter.valueAction_(2)},
				5, {counter.valueAction_(3)},
				6, {counter.valueAction_(4)},
				7, {counter.valueAction_(5)},
				8, {counter.valueAction_(6)},
				9, {counter.valueAction_(7)},
				10, {counter.valueAction_(8)},
				11, {
					this.loadScene(\Countdown);
				},
				27, {
					this.loadScene(\Stars);
				},
				28, {
					var h;
					"Lift off".postln;
					h = this.getWindowByName("TIYCS - Stars").view.children[0].children[1];
					this.makeRoutine(30 * 10, {
						|i|
						var hMapped = i.linlin(0, 30*10, 0, 1);
						hMapped = hMapped.pow(2);
						hMapped = hMapped * 2;
						{h.valueAction_(hMapped)}.fork(AppClock);
					});
				},
				29, {
					var m = this.getWindowByName("TIYCS - Stars").view.children[2].children[1];
					"Overlay + start route".postln;
					m.valueAction_(1);
					this.getWindowByName("TIYCS - Stars").view.children[6].valueAction_(1);
				},
				30, {
					"Commercials!".postln;
					this.loadScene(\Commercials);
				},
				31, {
					this.clearRoutines();
					this.loadScene(\Stars2);
					this.getWindowByName("TIYCS - Stars").view.children[4].children[1].valueAction_(255);
					this.getWindowByName("TIYCS - Stars").view.children[7].valueAction_(1); // Move
					this.getWindowByName("TIYCS - Stars").view.children[8].valueAction_(1); // Empty tank
				},
				32, {
					this.loadScene(\Benzine);
				},
				33, {
					"Down".postln;
					this.makeRoutine(30 * 10, {
						|i|
						var h = i.linlin(0, 30*10, 1, 0);
						h = h.pow(2);
						h = h * 2;
						this.setBus(0, h); // Landing
					});
				},
				34, {
					"Captain incoming".postln;
					this.loadScene(\Captain);
				},
				35, {
					"Start call".postln;
					counter.valueAction_(1);
					"TO DO: glitch".error;
				},
				36, {
					this.loadScene(\Bingo);
				},
				37, {
					this.getWindowByName("TIYCS - Bingo").view.children[4].valueAction_(1);
					"Start turning bg".postln;
				},
				38, {
					this.getWindowByName("TIYCS - Bingo").view.children[0].valueAction_(1);
					"Roll bingo wheel".postln;
				},
				39, {
					"First number".postln;
					this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
				},
				40, {
					"2nd number".postln;
					this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
				},
				41, {
					"3rd number".postln;
					this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
				},
				42, {
					"4th number".postln;
					this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
				},
				43, {
					"Voer code in".postln;
					this.loadScene(\Code);
				},
				44, {
					counter.valueAction_(1);
				},
				45,{
					counter.valueAction_(2);
				},
				46,{
					counter.valueAction_(3);
				},
				47, {
					counter.valueAction_(4);
				},
				48, {
					"Starting autopilot".postln;
					this.loadScene(\Autopilot);
				},
				49, {
					"Countdown 2".postln;
					this.loadScene(\Countdown2);
				},
				55, {
					this.clearRoutines();
					"Back to stars 2";
					this.loadScene(\Stars);
					this.loadScene(\Stars2);
				},
				56, {
					"Blackout".postln;
					this.loadScene(\BLACKOUT);
				},
				57, {
					"Eind".postln;
					this.loadScene(\Einde);
				}
					);
			if((value > 11).and(value < 27), { // 12 -
				var index = value - 12;
				var count = (0..14).reverse[index];
				counter.valueAction_(count);
			});
			if((value > 49).and(value < (49+6)), { // 49 - 54
				var index = value - 50;
				var count = (0..4).reverse[index];
				counter.valueAction_(count);
			});
			});
	}
}

// s.options.numInputBusChannels_(0);
t = TIYCS.new().automate
// t.getWindowByName("TIYCS - Bingo").view.children[4].valueAction_(1)