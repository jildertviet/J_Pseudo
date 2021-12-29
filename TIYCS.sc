TIYCS{
	var <> scenes, <>img, <>size, <>frameDur, <>frameRate;
	var <>routines;
	var <>screens;
	var <>counter;
	var <>flyHeight;
	var <>noteOn, <>noteOff, <>cc;
	var <>bingo;
	*new{
		^super.new.init();
	}
	init{
		screens = [
			NetAddr("127.0.0.2", 5000),
			NetAddr("127.0.0.1", 5000),
			NetAddr("127.0.0.3", 5000),
		];

		size = [1280, 800];
		this.setFramerate(30);
		this.initBingo();
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
				height =EZSlider.new(window, label:"Height", controlSpec: ControlSpec(0, 2, 'lin')).value_(2).action_({|e|this.setBus(0, e.value)});
				speed = EZSlider.new(window, label:"Speed", controlSpec: ControlSpec(0.2, 10, 'lin', 0.001)).value_(1).action_({|e|this.setBus(1, e.value)});
				speed.setColors(numNormalColor: Color.white);
				height.setColors(numNormalColor: Color.white);
				window.bounds_(window.bounds + [0,80,0,0]); // Move 80 px to top
				this.setScene(4, -1);
				this.setBus(0, 2); // Height
				this.setBus(1, 1); // TravelSpeed
			}],
			["Route"],
			["Commercials", {
				var window = Window("TIYCS - Commercials").front.setInnerExtent(400, 30);
				window.bounds_(window.bounds + [0,80,0,0]); // Move 80 px to top
				window.view.palette_(QPalette.dark);
				window.layout = HLayout(
					[Button().string_("One").action_({this.setScene(6, -1); this.eventById(1, 0)})],
					[Button().string_("Two").action_({this.setScene(7, -1); this.eventById(1, 1)})],
					[Button().string_("Three").action_({this.setScene(8, -1); this.eventById(1, 2)})],
					[Button().string_("Stop").action_({this.setScene(16, -1); this.eventById(1, -1)})],
				);
			}
			],
			["Benzine", {
				this.setScene(9, 1);
				this.setScene(16, [0,2]); // Outer two to black
				this.setBus(0, 0);
			}],
			["ReturnToShip", {
				this.setScene(10);
				this.counter.value_(0); // Define a MIDI function
				this.noteOn = {
					|note, vel|
					this.setBus(0, note); // Increase the seq
				};
			}],
			["Captain", {
				this.setScene(11, 1);
				this.setScene(12, [0,2]);
			}],
			["Bingo", {
				/*
				BACKGROUND,
				WHEEL,
				NUMBER,
				BACKGROUND_AND_WHEEL
				*/
				var window = Window("TIYCS - Bingo").front.setInnerExtent(400, 30);
				this.setScene(13, -1);
				this.setBus(0, 3, 1); // Bingo mode
				this.setBus(0, 0, [0,2]); // Bingo mode
				this.setBus(1, 1, 1); // Gravity full
				window.bounds_(window.bounds + [0,80,0,0]); // Move 80 px to top
				window.view.palette_(QPalette.dark);
				window.layout = HLayout(
					[Button().states_([["Roll"],["Stop"]]).action_({|e| if(e.value == 1, {
						var routine = this.makeRoutine(inf, {|i| if( i % (frameRate*0.5) == 0, {this.setBus(1, [0, 1, 0.5].choose)})}); // Roll
						routines.add(routine);
					}, {
						this.setBus(1, 1); // Why 1? ;
						this.clearRoutines(); // Stop
					})})],
					[Button().string_("Pick number").action_({
						this.setBus(0, 0, [0,2]); // Set outter screens to certain bingo-drawMode
						if(bingo != nil, {
							this.eventById(2, bingo.pop);
						});
					})],
					[Button().string_("Reset").action_({this.initBingo(); this.eventById(3)})],
				);
			}],
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
			if(screen.isArray, {
				screens.at(screen).do{|g| g.sendMsg("/setScene", id)};
			},{
				screens[screen].sendMsg("/setScene", id)
			});
		});
	}
	initBingo{
		bingo = [4, 5, 6, 9, 3, -1].reverse;
	}
	setBus {
		|id, value,screen= -1|
		if(screen == -1, {
			screens.do{|g| g.sendMsg("/setBus", id, value)};
		}, {
			if(screen.isArray, {
				screens.at(screen).do{|g| g.sendMsg("/setBus", id, value)};
			},{
				screens[screen].sendMsg("/setBus", id, value);
			});
		});
	}
	eventById {
		|id, value=0, screen= -1|
		if(screen == -1, {
			screens.do{|g| g.sendMsg("/eventById", id, value)};
		}, {
			screens[screen].sendMsg("/eventById", id, value);
		});
	}
	gui{
		var numColumns = 4;
		var numRows = ceil(scenes.size / numColumns);
		var window = Window("TIYCS - Scenes").front;
		var a = { { Button(window) } ! numColumns } ! numRows;
		var buttonMatrix = View().layout_(VLayout(*a.collect { |x| HLayout(*x) }));
		window.view.palette_(QPalette.dark);
		counter = NumberBox().step_(1).normalColor_(Color.white);

		window.layout = VLayout(
			[buttonMatrix],
			[counter],
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