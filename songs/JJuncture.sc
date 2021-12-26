JJuncture : JSong {
	var rectangles, mirrors;
	init {
		"JJuncture init".postln;
		functions = Array.newClear(16);
		rectangles = List.newClear(0);
		mirrors = List.newClear(0);
	}
	f0 {
		this.makeSquare(0);
	}
	f1 {
		this.makeSquare(1);
	}
	f2 {
		this.makeSquare(2);
	}
	f3{
		this.makeMirror();
	}
	f4{ // Do w/ color
		mirrors.do{|e| e.postln;  (e.color.alpha).postln; e.setAlpha(1.0 - (e.color.alpha)); (e.color.alpha).postln};
	}
	f5{ // Mirror angle
		mirrors.do{|e| e.setAngle(90.rand-45);};
	}
	f6{
		mirrors.do{|e| e.setMove((1-e.bMove.asInteger).asBoolean)};
	}
	f7{ // Jump All
		rectangles.do{
			|e|
			e.addLoc([100.rand-50, 0]); // Add -50, 50 to loc
		}
	}
	f8{ // Kill all rects
		rectangles.do{
			|e|
			e.killEnv(1000);
		}
	}
	f9{ // Double time
		rectangles.do{ |e| e.setSpeed(e.speed * 2);
		};
	}
	f10{ // Half time
		rectangles.do{ |e| e.setSpeed(e.speed * 0.5)};
	}
	makeMirror{
		var x = JMirror.new;
		var size = [200, 200];
		x.create();
		x.setLoc([(~w-size[0]).rand, (~h-size[1]).rand]); // TODO
		x.setSize(size);
		x.setColor(Color.white);
		mirrors.add(x); // Keep a reference
	}
	makeSquare{
		|type|
		var x = JRectangle.new;
		var c;
		var size;
		x.create();
		switch (type,
			0, {
				var w = 100;
				var h = 50;
				c = [255, 255, 255, 100 + 155.rand];
				x.setLoc([~w - w, ((~h-h).rand), 200.rand]);
				size = [w,h,2];
				x.setSpeed(0.8.rand + 0.5);
				x.setDirection([-1, 0]);
			},
			1, {
				var w = 5;
				var h = 400;
				c = [255, 255, 255, 100 + 155.rand];
				x.setLoc([~w-w, 200, 200.rand]);
				size = [w,h];
				x.setSpeed(1.5 + 0.1.rand);
				x.setDirection([-1, 0]);
			},
			2, {
				var w = 20;
				var h = 200;
				var loc = [~w - w, ~h - h];
				size = [w,h];
				c = [255, 255, 255, 155.rand+100];
				x.setLoc(loc);
				x.setDirection([-1, 0]);
				x.setSpeed(0.5.rand + 2.5);
			}
		);
		x.setSize(size);
		x.setColor(c);
		x.setMove(true);
		if(type==2, {	x.killEnv(5000);}); // Needs to happen after setColor :/
		rectangles.add(x);
	}
}