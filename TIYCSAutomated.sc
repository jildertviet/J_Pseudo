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
		automationWindow = Window("Automate", Rect(2000, 800, 300, 150)).front;
		cueCounter = NumberBox(automationWindow, Rect(m, m + h, 50, h)).step_(1).value_(-1);
		textBoxes[0] = StaticText(automationWindow, Rect(m + 50 + m, 0, 150, h)).stringColor_(Color.grey);
		textBoxes[1] = StaticText(automationWindow, Rect(m + 50 + m, h + m, 150, h));
		textBoxes[2] = StaticText(automationWindow, Rect(m + 50 + m, (h + m) * 2, 150, h)).stringColor_(Color.grey);
		textBoxes[3] = StaticText(automationWindow, Rect(m + 50 + m, (h + m) * 3, 150, h)).stringColor_(Color.grey);

		this.gui;

		cues = 0!100;
		cues[0] = [{
			this.valyueById(2, 255);
			this.clearRoutines();
			this.loadScene(\Intro);
			this.setScreenOrder(2, 1, 0);
		}, "Intro"];
		cues[1] = [{
			var button;
			var w;
			"Rotate".postln;
			w = this.getWindowByName("TIYCS - Intro");
			button = w.view.children[0];
			button.postln;
			button.valueAction_(1); // Rotate
		}, "Roteren: 'welkom'"];
		cues[2] = [ {
			this.clearRoutines();
			this.loadScene(\Instructions);
		}, "Instructies"];
		cues[3] = [ {counter.valueAction = 1}, "snacks"];
		cues[4] = [ {counter.valueAction = 2}, "bagage"];
		cues[5] = [ {counter.valueAction = 3}, "mobiel"];
		cues[6] = [ {counter.valueAction = 4}, "links"];
		cues[7] = [ {counter.valueAction = 5}, "rechts"];
		cues[8] = [ {counter.valueAction = 6}, "rechtop"];
		cues[9] = [ {counter.valueAction = 7}, "nooduitgang"];
		cues[10] = [ {counter.valueAction = 8}, "black"];
		cues[11] = [ {this.loadScene(\Countdown)}, "countdown: 15"];
		cues[12] = [ {counter.valueAction = 14 }, "14"];
		cues[13] = [ {counter.valueAction = 13 }, "13"];
		cues[14] = [ {counter.valueAction = 12 }, "12"];
		cues[15] = [ {counter.valueAction = 11 }, "11"];
		cues[16] = [ {counter.valueAction = 10 }, "10"];
		cues[17] = [ {counter.valueAction = 9 }, "9"];
		cues[18] = [ {counter.valueAction = 8 }, "8"];
		cues[19] = [ {counter.valueAction = 7 }, "7"];
		cues[20] = [ {counter.valueAction = 6 }, "6"];
		cues[21] = [ {counter.valueAction = 5 }, "5"];
		cues[22] = [ {counter.valueAction = 4 }, "4"];
		cues[23] = [ {counter.valueAction = 3 }, "3"];
		cues[24] = [ {counter.valueAction = 2 }, "2"];
		cues[25] = [ {counter.valueAction = 1 }, "1"];
		cues[26] = [ {counter.valueAction = 0 }, "black"];
		cues[27] = [ {
			this.clearRoutines();
			this.loadScene(\Stars)
		}, "Raam: fort"];
		cues[28] = [ {
			var h;
			var num = 20;
			"Lift off".postln;
			h = this.getWindowByName("TIYCS - Stars").view.children[0].children[1];
			this.makeRoutine(30 * num, {
				|i|
				var hMapped = i.linlin(0, 30*num, 0, 1);
				hMapped = hMapped.pow(2);
				hMapped = hMapped * 2;
				{h.valueAction_(hMapped)}.fork(AppClock);
			});
		}, "opstijgen"];
		cues[29] = [ {
			var m = this.getWindowByName("TIYCS - Stars").view.children[2].children[1];
			this.clearRoutines();
			"Overlay + start route".postln;
			m.valueAction_(1);
			this.getWindowByName("TIYCS - Stars").view.children[6].valueAction_(1);
		}, "route overlay"];
		cues[30] = [ {
			"Commercials!".postln;
			this.loadScene(\Commercials);
		}, "commercials"];
		cues[31] = [ {
			this.loadScene(\Stars2);
		}, "terug naar sterren"];
		cues[32] = [ {
			this.loadScene(\QandA);
		}, "Vraag en antwoord"];
		cues[33] = [ {
			~j.jonisks.do{|j| j.synth.set(\noiseMul, 0.0)};
			this.clearRoutines();
			this.loadScene(\Stars2);
			this.getWindowByName("TIYCS - Stars").view.children[4].children[1].valueAction_(255);
			this.getWindowByName("TIYCS - Stars").view.children[7].valueAction_(1); // Move
			this.getWindowByName("TIYCS - Stars").view.children[8].valueAction_(1); // Empty tank
		}, "terug naar vliegen"];
		cues[34] = [ {
			this.loadScene(\Benzine);
		}, "alarm: benzine"];
		cues[35] = [ {
			this.clearRoutines();
			this.setBus(5, 0); // Red fill OFF
			"Down".postln;
			this.makeRoutine(30 * 10, {
				|i|
				var h = i.linlin(0, 30*10, 1, 0);
				h = h.pow(2);
				h = h * 2;
				this.setBus(0, h); // Landing
			});
		}, "dalen"];
		cues[36] = [ {
			this.clearRoutines();
			"Captain incoming".postln;
			this.loadScene(\Captain);
		}, "Incoming call"];
		cues[37] = [ {
			"Start call".postln;
			counter.valueAction_(1);
			"TO DO: glitch".error;
		}, "Opnemen"];
		cues[38] = [ {
			this.loadScene(\Bingo);
		}, "Bingo"];
		cues[39] = [ {
			this.getWindowByName("TIYCS - Bingo").view.children[4].valueAction_(1);
			"Start turning bg".postln;
		}, "Roteren achtergrond"];
		cues[40] = [ {
			this.getWindowByName("TIYCS - Bingo").view.children[0].valueAction_(1);
			"Roll bingo wheel".postln;
		}, "Bingo wiel rollen"];
		cues[41] = [ {
			"First number".postln;
			this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
		}, "pick: 4"];
		cues[42] = [ {
			"2nd number".postln;
			this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
		}, "pick: 5"];
		cues[43] = [ {
			"3rd number".postln;
			this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
		}, "pick: 6"];
		cues[44] = [ {
			"4th number".postln;
			this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
		}, "pick: 9"];
		cues[45] = [ {
			"5th number".postln;
			this.getWindowByName("TIYCS - Bingo").view.children[1].valueAction_(1);
		}, "pick: 3"];
		cues[46] = [ {
			"Voer code in".postln;
			this.loadScene(\Code);
		}, "Voer code in"];
		cues[47] = [ {
			counter.valueAction_(1);
		}, "1e getal"];
		cues[48] = [{
			counter.valueAction_(2);
		}, "2e getal"];
		cues[49] = [{
			counter.valueAction_(3);
		}, "3e getal"];
		cues[50] = [ {
			counter.valueAction_(4);
		}, "4e getal"];
		cues[51] = [ {
			"Starting autopilot".postln;
			this.clearRoutines();
			this.loadScene(\Autopilot);
		}, "Gelukt! start automatische P"];
		cues[52] = [ {
			"Countdown 2".postln;
			this.loadScene(\Countdown2);
		}, "Countdown: 5"];
		cues[53] = [{
			counter.valueAction_(4);
		}, "4"];
		cues[54] = [{
			counter.valueAction_(3);
		}, "3"];
		cues[55] = [{
			counter.valueAction_(2);
		}, "2"];
		cues[56] = [{
			counter.valueAction_(1);
		}, "1"];
		cues[57] = [{
			counter.valueAction_(0);
		}, "black"];
		cues[58] = [{
			this.clearRoutines();
			this.setScreenOrder(0,1,2);
			this.loadScene(\Starsfinal);
		}, "Stars final"];

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
		}); // Action function
		textBoxes[2].string = cues[0][1];
		textBoxes[3].string = cues[1][1];
	} // Automate function (TIYCS::)
}

// s.options.numInputBusChannels_(0);
// t = TIYCS.new().automate
// t.getWindowByName("TIYCS - Bingo").view.children[4].valueAction_(1)