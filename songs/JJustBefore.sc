JJustBefore : JSong {
	var <>spacecubes;
	init {
		"JJustBefore init".postln;
		spacecubes = List.new;
	}
	f0 { // Switch Rotation
		spacecubes.do{|sc| sc.rotateRotationSpeed()};
	}
	f1 { // Add or remove vertices
		spacecubes.choose.addOrRemoveVertices();
	}
	f2 { // drawOneLine
		spacecubes.choose.lightLine();
	}
	f3{// slow
		spacecubes.do{|sc| sc.setSpeed(0.5)};
	}
	f4{ // randomAngle
		spacecubes.do{|sc| sc.randomChooseRotXorRotY()};
	}
	f5{ // Env @ speed
		spacecubes.do{|sc| sc.addEnv("speed", [10,10,400], [1,4,4,1] * sc.speed)};
	}
	f6{ // fadeOut ...
		spacecubes.do{|sc| sc.killEnv(3000)};
	}
	f7{ // Blink
		spacecubes.choose.blink();
	}
	f8{ // 40x + and - vertices
		40.do{this.f1()};
	}
	f9{// Lines vs Triangles
		spacecubes.do{|sc| sc.setMode(sc.mode.not)};
	}
	f10{// transform
		spacecubes.do{|sc|
			sc.setMode(0);
			sc.setColor(Color.white.alpha_(0.4));
			sc.addOrRemoveVertices(true, 9);
		}
	}
	f11{// doubleTime
		spacecubes.do{|sc| sc.setSpeed(sc.speed * 2)};
	}
	f12{// halfTime
		spacecubes.do{|sc| sc.setSpeed(sc.speed * 0.5)};
	}
	f13{ //All to lines
		spacecubes.do{|sc| sc.setMode(0)};
	}
	f14{ // Make object
		var sc = JSpaceCube.new;
		sc.create();
		sc.setSize(~v.h * 0.9 * [1,1,-1]);
		sc.resize(sc.size, 5.rrand(7), 6);

		sc.setMode(1);
		sc.setColor(Color.white.alpha_(0.1));
		sc.setVal("radius", 500 + ((~v.w*0.5) - 200).rand);
		sc.randomChooseRotXorRotY();
		spacecubes.add(sc);
	}
	f15{

	}
}