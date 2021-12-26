TIYCS{
	var <> scenes, <>img, <>size, <>frameDur, <>frameRate;
	var <>routines;
	*new{
		^super.new.init();
	}
	makeRoutine{
		|func|
		var routine = {
			inf.do{
				|i|
				func.value(i);
				// (				"Wait " ++ frameDur ). postln;
				frameDur.wait;
			}
		}.fork;
		^routine;
	}
	clearRoutines{
		routines.do{|routine| routine.stop;};
		routines.clear();
	}
	setFramerate {
		|rate|
		frameDur = (1/rate);
		frameRate = rate;
	}
	init{
		size = [1280, 800];
		this.setFramerate(30);
		scenes = List.new();
		img = List.new();
		routines = List.new();
		scenes.add(
			["Intro", {
				var originalLoc = [0,0];
				img = JImage.new();
				img.setPath("joniskLayer.png");
				img.create();
				img.setSize(this.size);
				img.setLoc(originalLoc);
				routines.add(this.makeRoutine({
					|i|
					var zoom = (sin(i/50) * (0.1 * 1) + 1);
					var height = pow(sin(i/50), 2) * 30 * i.linexp(0, (frameRate * 10), 1, 2);
					img.setZoom(zoom);
					img.setLoc(originalLoc + [0, height]);
				}));
				/*
				To do: make content for 3 screens
				*/
			}],
			["Instructions"],
			["CaptainPicto"],
			["Waveform"],
			["StarsVideo"],
			["Route"],
			["Commercial0"],
			["Commercial1"],
			["Commercial2"],
			["Benzine"],
			["MartianWindow"],
			["Countdown"],
			["JoniskBig"],
			["Bingo"],
			["Code"],
			["Autopilot"],
			["Return"],
			["BingoBall"],
			["None"],
			["WordtVervolgd"],
			["BingoBg"]
		);
	}
}

// t = TIYCS.new();
// t.scenes[0][1].value();
// t.img.setZoom(2);
// t.clearRoutines
// ~v.killAll
// t.img.killEnv(1)