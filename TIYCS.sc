TIYCS{
	var <> scenes, <>img, <>size, <>frameDur, <>frameRate;
	var <>routines;
	var <>screens;
	var <>counter;
	var <>flyHeight;
	*new{
		^super.new.init();
	}
	init{
		screens = [
			NetAddr("127.0.0.1", 5000),
			NetAddr("127.0.0.2", 5000),
			NetAddr("127.0.0.3", 5000),
		];

		size = [1280, 800];
		this.setFramerate(30);
		scenes = List.new();
		routines = List.new();
		scenes = scenes ++ [
			["Intro", {
				var numFrames = (frameRate * 30); // 30 sec
				this.setScene(0, -1);
				routines.add(this.makeRoutine(numFrames, {
					|i|
					this.setBus(0, linlin(i, 0, numFrames, 0, 2)); // Movement-intensity
				}));
			}],
			["Instructions", {
				this.setScene(2, -1);
				this.setBus(0, 0);
				counter.value_(0);
				counter.action_({|e|this.setBus(0, e.value)}).focus;
			}],
			["Countdown",{
				this.setScene(3, -1);
				this.setBus(0, 10);
				counter.value_(10);
				counter.action_({|e|this.setBus(0, e.value)}).focus;
			}],
			["Stars", {
				var window = Window("TIYCS - Stars").front.setInnerExtent(400, 60);
				var speed, height;

				window.view.palette_(QPalette.dark);
				window.view.decorator_(FlowLayout(window.view.bounds));
				speed = EZSlider.new(window, label:"Speed", controlSpec: ControlSpec(0.2, 10, 'lin', 0.001)).value_(1);
				height =EZSlider.new(window, label:"Height", controlSpec: ControlSpec(0, 2, 'lin')).value_(2);
				speed.setColors(numNormalColor: Color.white);
				height.setColors(numNormalColor: Color.white);
				window.bounds_(window.bounds + [0,80,0,0]); // Move 80 px to top
				this.setScene(4, -1);
				this.setBus(0, 2); // Height
				this.setBus(1, 1); // TravelSpeed
			}],
			["Route"],
			["Commercial0"],
			["Commercial1"],
			["Commercial2"],
			["Benzine"],
			["ReturnToShip"],
			["Captain"],
			["Bingo"],
			["Code"],
			["Autopilot"],
			["None"],
		];
	}
	makeRoutine{
		|num=1, func|
		var routine = {
			num.do{
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
	setScene {
		|id, screen= -1|
		if(screen == -1, {
			screens.do{|g| g.sendMsg("/setScene", id)};
		}, {
			screens[screen].sendMsg("/setScene", id)
		});
	}
	setBus {
		|id, value,screen= -1|
		if(screen == -1, {
			screens.do{|g| g.sendMsg("/setBus", id, value)};
		}, {
			screens[screen].sendMsg("/setBus", id, value);
		});
	}
	gui{
		var numColumns = 4;
		var numRows = ceil(scenes.size / numColumns);
		var window = Window("TIYCS - Scenes").front;
		var a = { { Button(window) } ! numColumns } ! numRows;
		var buttonMatrix = View().layout_(VLayout(*a.collect { |x| HLayout(*x) }));
		// var flyHeightNumberBox = NumberBox().normalColor_(Color.white);
		// var controlSpec = ControlSpec(0, 2);
		// var flyHeight = Slider().orientation_(\horizontal).action_({|e|
		// var mapped = controlSpec.map(e.value);
		// flyHeightNumberBox.value_(mapped)
// });
		window.view.palette_(QPalette.dark);
		// buttonMatrix.background_(Color.red);
		counter = NumberBox().step_(1).normalColor_(Color.white);

		window.layout = VLayout(
			[buttonMatrix],
			[counter],
			// HLayout(
			// [flyHeight, stretch: 5],
			// [flyHeightNumberBox, stretch: 1],
		// )
		);
		// Assign scene-functions to buttons
		a = a.reshape(scenes.size, 1);
		a = a.collect({|e| e[0]});
		a.do{|e, i|
			e.string_(scenes[i][0]);
			e.action_({this.clearRoutines; scenes[i][1].value()});
		}
	}
}