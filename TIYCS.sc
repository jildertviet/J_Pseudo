TIYCS{
	var <> scenes, <>img, <>size, <>frameDur, <>frameRate;
	var <>routines;
	var <>screens;
	var <>counter;
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
		img = List.new();
		routines = List.new();
		scenes = scenes ++ [
			["Intro", {
				this.setScene(0, -1);
				routines.add(this.makeRoutine({
					|i|
					this.setBus(0, linlin(i, 0, 900, 0, 2)); // move intensity
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
			["Stars"],
			["Route"],
			["Commercial0"],
			["Commercial1"],
			["Commercial2"],
			["Benzine"],
			["ReturnToShip"],
			["CaptainPicto"],
			["Waveform"],
			["Bingo"],
			["Code"],
			["Autopilot"],
			["None"],
		];
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
		buttonMatrix.background_(Color.red);
		numColumns.postln;
		numRows.postln;
		counter = NumberBox().step_(1);
		window.layout = VLayout(
			[buttonMatrix],
			[counter, stretch: 10]
		);
		a = a.reshape(scenes.size, 1);
		a = a.collect({|e| e[0]});
		a.do{|e, i|
			e.string_(scenes[i][0]);
			e.action_({this.clearRoutines; scenes[i][1].value()});
		}
	}
}