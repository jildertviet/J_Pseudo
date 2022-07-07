+ TIYCS {
	loadScene{ |key|
		onSwitch.value();
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
		var m = 10;
		var h = 30;
		var automationCounter;
		var cueCounter;
		var textBoxes = 0!4;
		automationWindow = Window("Automate ↑", Rect(2000, 800, 300, 150)).front;
		cueCounter = NumberBox(automationWindow, Rect(m, m + h, 50, h)).step_(1).value_(-1);
		textBoxes[0] = StaticText(automationWindow, Rect(m + 50 + m, 0, 150, h)).stringColor_(Color.grey);
		textBoxes[1] = StaticText(automationWindow, Rect(m + 50 + m, h + m, 150, h));
		textBoxes[2] = StaticText(automationWindow, Rect(m + 50 + m, (h + m) * 2, 150, h)).stringColor_(Color.grey);
		textBoxes[3] = StaticText(automationWindow, Rect(m + 50 + m, (h + m) * 3, 150, h)).stringColor_(Color.grey);
		/*Button(automationWindow, Rect(300 - 50 - m, m, 50, 50)).states_([
            ["↑", Color.grey, Color.white],
        ]).action_({
			cueCounter.decrement(1);
		});
		Button(automationWindow, Rect(300 - 50 - m, m + 50 + m, 50, 50)).string_("↓").action_({
			cueCounter.increment(1);
		});*/

		this.gui;

		cues = 0!100;
		cues[0] = [{
			this.loadScene(\None);
			if(~j != nil, {
				~j.slidersDict[\rgbw].do{|e, i| e.valueAction = [0,0,0,255].at(i)}; // Slider at range 0-255
				~j.slidersDict[\asr].do{|e, i| e.valueAction = [0.01, 0.5, 3.0].at(i)};
				~j.slidersDict[\brightness].valueAction = 0.15;
				~j.slidersDict[\brightnessAdd].valueAction = 1.0;
			});
		}, "Inloop"];
		cues[1] = [{
			this.valyueById(2, 255);
			this.clearRoutines();
			this.loadScene(\Intro);
			// this.setScreenOrder(2, 1, 0);
		}, "Intro"];
		cues[2] = [{
			var button;
			var w;
			"Rotate".postln;
			w = this.getWindowByName("TIYCS - Intro");
			button = w.view.children[0];
			button.postln;
			button.valueAction_(1); // Rotate
		}, "Roteren: 'welkom'"];
		cues[3] = [ {
			this.clearRoutines();
			this.loadScene(\Instructions);
		}, "Instructies"];
		cues[4] = [ {counter.valueAction = 1}, "Instr: snacks"];
		cues[5] = [ {counter.valueAction = 2}, "Instr: bagage"];
		cues[6] = [ {counter.valueAction = 3}, "Instr: mobiel"];
		cues[7] = [ {counter.valueAction = 4}, "Instr: links"];
		cues[8] = [ {counter.valueAction = 5}, "Instr: rechts"];
		cues[9] = [ {counter.valueAction = 6}, "Instr: rechtop"];
		cues[10] = [ {counter.valueAction = 7}, "Instr: nooduitgang"];
		cues[11] = [ {counter.valueAction = 8}, "black"];
		cues[12] = [ {this.loadScene(\Countdown)}, "countdown: 15"];
		cues[13] = [ {counter.valueAction = 14 }, "14"];
		cues[14] = [ {counter.valueAction = 13 }, "13"];
		cues[15] = [ {counter.valueAction = 12 }, "12"];
		cues[16] = [ {counter.valueAction = 11 }, "11"];
		cues[17] = [ {counter.valueAction = 10 }, "10"];
		cues[18] = [ {counter.valueAction = 9 }, "9"];
		cues[19] = [ {counter.valueAction = 8 }, "8"];
		cues[20] = [ {counter.valueAction = 7 }, "7"];
		cues[21] = [ {counter.valueAction = 6 }, "6"];
		cues[22] = [ {counter.valueAction = 5 }, "5"];
		cues[23] = [ {counter.valueAction = 4 }, "4"];
		cues[24] = [ {counter.valueAction = 3 }, "3"];
		cues[25] = [ {counter.valueAction = 2 }, "2"];
		cues[26] = [ {counter.valueAction = 1 }, "1"];
		cues[27] = [ {counter.valueAction = 0 }, "black"];
		cues[28] = [ {
			this.clearRoutines();
			this.loadScene(\Stars)
		}, "Raam: fort"];
		cues[29] = [ {
			var h, speedSlider;
			var num = 20;
			"Lift off".postln;
			h = this.getWindowByName("TIYCS - Stars").view.children[0].children[1];
			speedSlider = this.getWindowByName("TIYCS - Stars").view.children[1].children[1];
			this.makeRoutine(30 * num, {
				|i|
				var hMapped = i.linlin(0, 30*num, 0, 1);
				hMapped = hMapped.pow(2);
				hMapped = hMapped * 2;
				{
					h.valueAction_(hMapped);
				}.fork(AppClock);
			});
			{
				10.wait;
				this.makeRoutine(30 * num, {
					|i|
					var speedMapped = i.linlin(0, 30*num, 1.0, 0.3);
					{speedSlider.valueAction_(speedMapped);}.fork(AppClock);
				});
			}.fork;
		}, "opstijgen"];
		cues[30] = [ {
			var m = this.getWindowByName("TIYCS - Stars").view.children[2].children[1];
			this.clearRoutines();
			"Overlay + start route".postln;
			m.valueAction_(1);
			this.getWindowByName("TIYCS - Stars").view.children[6].valueAction_(1);
			{
				20.wait;
				if(~j != nil, {
					var num = 30 * 10;
					this.makeRoutine(num, {
						|i|
						{
							~j.slidersDict[\brightness].valueAction = i.linlin(0, num, 0.15, 0.25);
							~j.slidersDict[\brightnessAdd].valueAction = i.linlin(0, num, 1.0, 0.0);
						}.fork(AppClock);
					});
				});
			}.fork;
		}, "route overlay"];
		cues[31] = [ {
			"Commercials!".postln;
			this.loadScene(\Commercials);
		}, "commercials"];
		cues[32] = [ {
			this.loadScene(\Stars2);
		}, "terug naar sterren"];
		cues[33] = [ {
			this.loadScene(\QandA);
		}, "Vraag en antwoord"];
		cues[34] = [ {
			if(~j != nil, {
				~j.jonisks.do{|j| j.synth.set(\noiseMul, 0.0)};
				// ~j.setBrightnessAdd(0.35);
				~j.slidersDict[\brightnessAdd].valueAction = 0.0;
				~j.slidersDict[\brightness].valueAction = 0.4;
				// ~j.setBrightness(0.6);
			});
			this.clearRoutines();
			this.loadScene(\Stars2);
			this.getWindowByName("TIYCS - Stars").view.children[4].children[1].valueAction_(255);
			this.getWindowByName("TIYCS - Stars").view.children[7].valueAction_(1); // Move
			this.getWindowByName("TIYCS - Stars").view.children[8].valueAction_(1); // Empty tank
		}, "terug naar vliegen"];
		cues[35] = [ {
			this.loadScene(\Benzine);
		}, "alarm: benzine"];
		cues[36] = [ {
			var num = 30 * 20;
			this.clearRoutines();
			this.setBus(5, 0); // Red fill OFF
			"Down".postln;
			this.makeRoutine(num, {
				|i|
				var h = i.linlin(0, num, 1, 0);
				h = h.pow(2);
				h = h * 2;
				this.setBus(0, h); // Landing
			});
		}, "dalen"];
		cues[37] = [ {
			this.clearRoutines();
			"Captain incoming".postln;
			this.loadScene(\Captain);
		}, "Incoming call"];
		cues[38] = [ {
			"Start call".postln;
			counter.valueAction_(1);
			"TO DO: glitch".error;
		}, "Opnemen"];
		cues[39] = [ {
			this.loadScene(\Bingo);
		}, "Bingo"];
		cues[40] = [ {
			this.getWindowByName("TIYCS - Bingo").view.children[4].valueAction_(1);
			"Start turning bg".postln;
		}, "Roteren achtergrond"];
		cues[41] = [ {
			this.getWindowByName("TIYCS - Bingo").view.children[0].valueAction_(1);
			"Roll bingo wheel".postln;
		}, "Bingo wiel rollen"];
		cues[42] = [ {
			"First number".postln;
			this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
		}, "pick: 4"];
		cues[43] = [ {
			"2nd number".postln;
			this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
		}, "pick: 5"];
		cues[44] = [ {
			"3rd number".postln;
			this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
		}, "pick: 6"];
		cues[45] = [ {
			"4th number".postln;
			this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
		}, "pick: 9"];
		cues[46] = [ {
			"5th number".postln;
			this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
		}, "pick: 3"];
		cues[47] = [ {
			"Voer code in".postln;
			this.loadScene(\Code);
		}, "Voer code in"];
		cues[48] = [ {
			counter.valueAction_(1);
		}, "1e getal"];
		cues[49] = [{
			counter.valueAction_(2);
		}, "2e getal"];
		cues[50] = [{
			counter.valueAction_(3);
		}, "3e getal"];
		cues[51] = [ {
			counter.valueAction_(4);
		}, "4e getal"];
		cues[52] = [ {
			"Starting autopilot".postln;
			this.clearRoutines();
			this.loadScene(\Autopilot);
		}, "Gelukt! start automatische P"];
		cues[53] = [ {
			"Countdown 2".postln;
			this.loadScene(\Countdown2);
		}, "Countdown: 5"];
		cues[54] = [{
			counter.valueAction_(4);
		}, "4"];
		cues[55] = [{
			counter.valueAction_(3);
		}, "3"];
		cues[56] = [{
			counter.valueAction_(2);
		}, "2"];
		cues[57] = [{
			counter.valueAction_(1);
		}, "1"];
		cues[58] = [{
			counter.valueAction_(0);
		}, "black"];
		cues[59] = [{
			this.clearRoutines();
			// this.setScreenOrder(0,1,2);
			this.loadScene(\Starsfinal);
		}, "Stars final"];
		cues[60] = [{
			this.loadScene(\Party);
		}, "Party lights"];

		cues[61] = [{}, ""];
		cues[62] = [{}, ""];
		cues[63] = [{}, ""];

		cueCounter.action_({
			|e|
			var value = e.value.asInteger;
			cues[value][0].value();

			if(value > 0, {
				textBoxes[0].string =  cues[value-1][1];
			});
			textBoxes[1].string = cues[value][1];
			textBoxes[2].string = cues[value+1][1];
			textBoxes[3].string = cues[value+2][1];
			automationWindow.visible = false;
			automationWindow.visible = true;
		}); // Action function
		textBoxes[2].string = cues[0][1];
		textBoxes[3].string = cues[1][1];
	} // Automate function (TIYCS::)
}

// s.options.numInputBusChannels_(0);
// t = TIYCS.new().automate
// t.getWindowByName("TIYCS - Bingo").view.children[4].valueAction_(1)