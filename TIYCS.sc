TIYCS{
	var <> scenes, <>img, <>size, <>frameDur, <>frameRate;
	var <>scenesDict, t;
	var <>routines;
	var <>screens;
	var <>counter;
	var <>flyHeight;
	var <>noteOn, <>noteOff, <>cc;
	var <>bingo;
	var <>onSwitch;
	var <>buffer, <>captainSample;
	var <> blackToggle = 0;
	var <> childWindows;
	var <> bShiftPressed = false;
	var <> mappingPoints;
	var <> automationWindow;
	var <> cues;
	var <> patterns;
	*new{
		|ip="127.0.0.1"|
		^super.new.init(ip);
	}
	init{
		|ip|
		if(ip.isString, {
			ip = ip!1;
		});
		ip = ip.asArray;
		screens = [
			NetAddr(ip.wrapAt(0), 5000),
			NetAddr(ip.wrapAt(1), 5001),
			NetAddr(ip.wrapAt(2), 5002),
		];

		size = [1280, 800];
		this.setFramerate(30);
		this.initBingo();
		noteOn = List.new();
		noteOff = List.new();
		cc = List.new();
		scenes = List.new();
		routines = List.new();
		this.initDefaultMidi();
		buffer = Buffer.alloc(Server.local, 400*3);
		captainSample = Buffer.readChannel(Server.local, "/Users/jildertviet/of_v0.11.2_osx_release/apps/TIYCS/capt weg kwijt v2.wav", channels: 1);
		this.initScenes();
		childWindows = List.new();
		mappingPoints = [[0,0],[1,0],[1,1],[0,1]]!3;
		patterns = 0!4;
		this.readMappingFile();
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
		routines.add(routine);
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
	valyueById{
		|id, value=0, screen= -1|
		if(screen == -1, {
			screens.do{|g| g.sendMsg("/setValueById", id, value)};
		}, {
			screens[screen].sendMsg("/setValueById", id, value);
		});
	}
	gui{
		var numColumns = 5;
		var numRows = ceil(scenes.size / numColumns);
		var window = Window("TIYCS - Scenes", Rect(950, 200, 400, 300)).front;
		var a = { { Button(window) } ! numColumns } ! numRows;
		var buttonMatrix = View().layout_(VLayout(*a.collect { |x| HLayout(*x) }));
		var brightnessSlider;
		window.view.palette_(QPalette.dark);
		counter = NumberBox().step_(1).normalColor_(Color.white);
		// brightnessSlider = Slider.new(window, label:"brightness", controlSpec: ControlSpec(0, 255, 'lin', 0.001)).action_(
		// {|e| this.valyueById(2, e.value * 2);}).orientation_(\horizontal).valueAction = 255;


		window.layout = VLayout(
			[buttonMatrix],
			[counter],
			// [brightnessSlider, 0.1]
		);
		// Assign scene-functions to buttons
		a = a.reshape(scenes.size, 1);
		a = a.collect({|e| e[0]});
		a.do{|e, i|
			e.string_(scenes[i][0]);
			e.action_({
				onSwitch.value();
				if(bShiftPressed == false, {
					this.closeChildWindows();
				});
				onSwitch = {}; // Clear
				this.clearRoutines;
				scenes[i][1].value()
			});
		};
		window.view.keyDownAction = {
			|doc, char, mod, unicode, keycode, key|
			// [doc, char, mod, unicode, keycode, key].postln;
			switch(key,
				70, { this.whiteFrame(3);}, // 'f'
				71, { this.blackFrame(3);}, // 'g'
				16777248, {bShiftPressed = true},
			);
		};
		window.view.keyUpAction = {
			|doc, char, mod, unicode, keycode, key|
			switch(key,
				16777248, {bShiftPressed = false},
			);
		};
	}
	clearMidiFunc{
		|num|
		[noteOn, noteOff].do{
			|list| // noteOn and noteOff
			var indexToRemove = -1;
			list.do{
				|e, i|
				if(e.msgNum == num, {
					indexToRemove = i;
				});
			};
			if(indexToRemove >= 0, {
				list.removeAt(indexToRemove);
			});
		}
	}
	initDefaultMidi{
		if(~j != nil, {~j.sequence = [0,1,2,3,4]});
		MIDIdef.cc(\brightnessVisuals, {|value, note| this.valyueById(2, value * 2); "Brightness".postln}, 8);
		// MIDIdef.cc(\brightness, {|value, note| this.valyueById(2, value * 2); "Brightness".postln}, 8);
		// MIDIdef.noteOn(\captainScene, {|value, note| scenes[9][1].value(); "Captain scene".postln}, 34, chan: 14);
		MIDIdef.noteOn(\arp, {
			|value, note|
			"ARP".postln;
			// ~j.jonisks.choose.trigger();
			~j.triggerNextInSeq();
		}, noteNum: 36 + (0..30), chan: 14);
		MIDIdef.noteOn(\blackout, { // Pad 1
			|val, num|
			scenesDict[\BLACKOUT][1].value();
		}, 0, chan: 0, srcID: -1625549786);
		MIDIdef.noteOn(\jonisksToWhite, { // Pad 8
			|val, num|
			"All Jonisks to white".postln;
			if(~j != nil, {
				~j.slidersDict[\rgbw].do{|e, i| e.valueAction = [0,0,0,255].at(i)}; // Slider at range 0-255
				~j.slidersDict[\brightness].valueAction = 0.35;
				~j.jonisks.do{|j|
					var c = [0,0,0,255];
					j.synth.set(\rgbw, c/255); // Scaled to 1
					j.setColor(c);
					j.setBrightness(0.35);
				};
			});
		}, 7, chan: 0, srcID: -1625549786);
	}
	whiteFrame{
		|dur=1|
		this.valyueById(16, dur);
	}
	blackFrame{
		|bOverride = false, value|
		if(bOverride == true, {
			blackToggle = value;
		}, {
			blackToggle = blackToggle + 1 % 2;
		});
		this.valyueById(17, blackToggle);
	}
	closeChildWindows{
		childWindows.do{|w|w.close};
	}
	fullscreen{
		|value=1|
		// screens.do{|s| s.sendMsg("/eventById", 5, value);};
		screens[0].sendMsg("/eventById", 5, value);
	}
	setTexCoord{
		|screenID=0, id=0, x=0, y=0|
		screens[screenID].sendMsg("/eventById", 4, id, x, y);
	}
	mappingGuiOld{
		|screenID=0, path="~"|
		var aspectRatio = 1280/800;
		var points = [[0,0],[1,0],[1,1],[0,1]];
		var size = [300*aspectRatio,300];
		var padding = 10;
		var w = Window("mapping screen " ++ screenID, Rect(100, 200, size[0], size[1])).front;
		var r= 15;
		var updateFunction;
		var lastMovedPoint = 0;
		points = mappingPoints[screenID];
		w.onClose_({mappingPoints[screenID] = points});
		w.drawFunc = { |v|
			Pen.fillColor = Color.blue;
			Pen.strokeColor = Color.red;
			points.do{
				|p, i|
				var a = Point(p[0] * (size[0] - (padding * 2)) + padding, p[1] * (size[1] - (padding * 2)) + padding);
				var b = Point(points[(i+1)%points.size][0] * (size[0] - (padding * 2)) + padding, points[(i+1)%points.size][1] * (size[1] - (padding * 2)) + padding);
				Pen.line(a, b);
			};
			Pen.fillStroke;
		};
		updateFunction = {
			|view, x, y|
			var smallestD = 10000;
			var index = 0;
			x = x - (padding) / (size[0] - (padding * 2));
			y = y - (padding) / (size[1] - (padding * 2));
			// [x,y].postln;
			x =	x.max(0).min(1);
			y = y.max(0).min(1);
			// Find closest point
			points.do{
				|p, i|
				var absX = (p[0] - x).abs;
				var absY = (p[1] - y).abs;
				var d = sqrt(absX.pow(2) + absY.pow(2));
				if(d < smallestD, {
					smallestD = d;
					index = i;
				});
			};
			lastMovedPoint = index;
			// Update points
			this.setTexCoord(screenID, index, (x * this.getWidth()), y * this.getHeight());
			points[index] = [x,y];
			w.refresh;
		};
		w.view.mouseDownAction_(updateFunction);
		w.view.mouseMoveAction_(updateFunction);
		w.view.keyDownAction_({
			|doc, char, mod, unicode, keycode, key|
			if(keycode == 123, { // L
				points[lastMovedPoint][0] = points[lastMovedPoint][0] - (1/this.getWidth());
			});
			if(keycode == 124, { // R
				points[lastMovedPoint][0] = points[lastMovedPoint][0] +  (1/this.getWidth());
			});
			if(keycode == 126, { // U
				points[lastMovedPoint][1] = points[lastMovedPoint][1] - (1/this.getHeight());
			});
			if(keycode == 125, { // D
				points[lastMovedPoint][1] = points[lastMovedPoint][1] + (1/this.getHeight());
			});
			if((keycode == 123).or(keycode==124).or(keycode==125).or(keycode==126), {
				points = points.max(0).min(1);
				w.refresh
			});
			if((unicode == 48).or(unicode == 49).or(unicode==50), {
				w.close;
				this.mappingGui(unicode-48);
			});
			if(unicode == 115, {
				var f;
				f = File((path ++ "/TIYCSMapping_"++screenID++".txt").standardizePath,"w");
				points.do{
					|p|
					// p.postln;
					f.write(p[0].asString);
					f.write(",");
					f.write(p[1].asString);
					f.write(",");
				};
				f.write("\n");
				f.close;
				"Save mapping".postln;
			});
		});
	}
	mappingGui{
		var vertexID = NumberBox().normalColor_(Color.white);
		var screenID = NumberBox().normalColor_(Color.white);
		var stepSize = NumberBox().normalColor_(Color.white).value_(1);
		var saveButton = Button().string_("Save").action_({
			screens[screenID.value.asInteger].sendMsg("/eventById", 8); // Save ofxPiMapper
		});
		var updateVertex = {
			|x=0, y=0|
			// [screenID.value, vertexID.value, x, y].postln;
			screens[screenID.value.asInteger].sendMsg("/eventById", 7, screenID.value.asInteger, vertexID.value.asInteger, x, y);
		};
		var w = Window.new("test", Rect(0, 900, 200, 50)).view.keyDownAction_({|doc, char, mod, unicode, keycode, key|
			switch(keycode,
				119, {updateVertex.(0, -1 * stepSize.value)}, // W: up
				97, {updateVertex.(-1 * stepSize.value, 0)}, // A: left
				115, {updateVertex.(0, 1 * stepSize.value)}, // S: down
				100, {updateVertex.(1 * stepSize.value, 0)} // D: right
		)}).front;
		w.palette_(QPalette.dark);
		w.layout = VLayout(
			HLayout(TextField().canFocus_(false).string_("Screen ID"), screenID),
			HLayout(TextField().canFocus_(false).string_("Vertex ID"), vertexID),
			HLayout(TextField().canFocus_(false).string_("Step size"), stepSize),
			saveButton
		);
		// Use WASD to move, use numberBox to set vertex index.
	}
	placementGui{
		var a;
		var dimensions = [10, 5];
		var w = Window("Placement", Rect(0, 600, 1000, 500)).front;
		w.view.palette_(QPalette.dark);
		a = { {
			var width = w.view.bounds.width / dimensions[0];
			var height = w.view.bounds.height / dimensions[1];
			var c = View.new(w, Rect(0, 0, width, 100));
			var b = NumberBox(c,Rect(0, height*0.5 - (17.5), width * 0.9, 25)).value_(-1).background_(Color.gray).maxDecimals_(0).align_(\center);
			c.background = Color(0, 0, 0, 0.1);
			c;
		} ! dimensions[0] } ! dimensions[1];
		w.layout = VLayout(*a.collect { |x| HLayout(*x) });
		a.flat.do({|e| e.children[0].action_(
			{
				|e|
				if(e.value >= 0, {e.background_(Color.white)}, {e.background_(Color.gray)});
				patterns[0] = a.collect({|row, i|
					var r = row.collect({|cell| cell.children[0].value.asInteger});
					if(i.odd, {r = r.reverse});
					r;
				}).flat.select({|e| e >= 0});
				patterns[0].postln; // Chaser per row, not scanning, continous line
				patterns[1] = a.collect({|row, i|
					var r = row.collect({|cell| cell.children[0].value.asInteger});
					r;
				}).flat.select({|e| e >= 0});
				patterns[1].postln; // Chaser per row, scanning
				patterns[2] = a.flop.collect({|row, i|
					var r = row.collect({|cell| cell.children[0].value.asInteger});
					r;
				}).flat.select({|e| e >= 0});
				patterns[2].postln; // Chaser per column, not scanning, continous line
				patterns[3] = a.flop.collect({|row, i|
					var r = row.collect({|cell| cell.children[0].value.asInteger});
					if(i.odd, {r = r.reverse});
					r;
				}).flat.select({|e| e >= 0});
				patterns[3].postln; // Chaser per column, scanning
		})});
	}
	readMappingFile{
		|folder="~"|
		if(File.exists(("~" ++ "/TIYCSMapping_0.txt").standardizePath) == false, {^false});
		"Read mapping file of 3 screens".postln;
		3.do{
			|i|
			var g = File((folder ++ "/TIYCSMapping_"++i++".txt").standardizePath,"r");
			var a = g.readAllString.split($,).asFloat;
			("Read: " ++ (folder ++ "/TIYCSMapping_"++i++".txt").standardizePath).postln;
			if(a.size() > 7, {
				var points = [[a[0], a[1]],[a[2], a[3]],[a[4], a[5]],[a[6], a[7]]];
				mappingPoints[i] = points;
				// points.postln;
			});
			mappingPoints[i].do{
				|p, index|
				this.setTexCoord(i, index, (p[0] * this.getWidth()), p[1] * this.getHeight());
			}
			// mappingPoints[0].postln;
			// mappingPoints[1].postln;
			// mappingPoints[2].postln;
		}
	}
	getWidth{
		^size[0]
	}
	getHeight{
		^size[1]
	}
	setScreenOrder{
		|first=0, second=1,third=2|
		screens[0].sendMsg("/eventById", 6,first, second, third);
	}
	welcomeTxt{
		StaticText(Window("").setInnerExtent(400, 100).front, Rect(0, 0, 400, 60)).string_(
			"1.) Beweeg muis @ Linux voor je begint (slaapstand).\n2.) Staat (Jonisk-)knop linksboven op STOP (en niet nog op Idle?)\n3.) Sluit dit scherm")
		.onClose_({automationWindow.visible = false; automationWindow.visible = true});
	}
}

