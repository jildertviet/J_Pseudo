TIYCS{
	var <> scenes, <>img, <>size, frameDur;
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
	}
	init{
		size = [1280, 800];
		this.setFramerate(30);
		scenes = List.new();
		img = List.new();
		routines = List.new();
		scenes.add(
			["Intro", {
				img = JImage.new();
				img.setPath("joniskLayer.png");
				img.create();
				img.setSize(this.size);
				routines.add(this.makeRoutine({|i|
					// pow(sin(ofGetFrameNum()/50.), 2.) * 30 * welcomIntensity // Height
					var zoom = (sin(i/50) * (0.1 * 1) + 1);
					img.setZoom(zoom);
				}));
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