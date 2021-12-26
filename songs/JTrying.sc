JTrying : JSong {
	var <>spacecubes, <>colors;
	init {
		"JTrying init".postln;
		spacecubes = List.new();
		colors = [Color.white.alpha_(0.4), Color.blue.alpha_(0.4)];
	}
	f0 { // Make 4x object
		4.do{ this.createSpacecube(size:200, lifeTime:7000, numVertices:5, gridDiv:2, colorIndex:0, locMul:1.5, zRange: -2000);};
	}
	f1 { // Make big
		|colorIndex=0|
		var size = 500;
		10.do{
			var lifeTime = 5000.rrand(9000);
			this.createSpacecube(size:size, lifeTime:lifeTime, numVertices:4, gridDiv:2, colorIndex:colorIndex, locMul:1, zRange: -2300);
		}
	}
	f2 { // Env @ rotationspeed
		spacecubes.do{|sc| sc.addEnv("speed", [100.rrand(500), 10, 1000.rrand(5000)], [1,8,8,1] * sc.speed)};
	}
	f3{ // Make big blue
		this.f1(1);
	}
	f4{ // Add vertices
		spacecubes.do{|sc| sc.addOrRemoveVertices(true)};
	}
	f5{ // Change rotationspeed
		spacecubes.do{|sc| sc.rotateRotationSpeed()};
	}
	f6{ // Smallest cloud
		20.do{
			var size = 30.rrand(130);
			var lifeTime = 9000;
			var sc = this.createSpacecube(size:size, lifeTime:lifeTime, numVertices:5, gridDiv:2, colorIndex:0, locMul:1.3, zRange: -2400);
			sc.rotateRotationSpeed();
		}
	}
	f7{ // Make mirrors
		12.do{
			var m= JMirror.new();
			var width = 100;
			m.create();
			m.setSize([width, ~v.h]);
			m.setLoc([(~v.w-width).rand, 0]);
			m.addEnv(times: [10,10,700], kill: true);
		}
	}
	f8{ // Bridge figures
		3.do{
			var size = 200;
			var lifeTime = 10000;
			var sc = this.createSpacecube(size:size, lifeTime:lifeTime, numVertices:3, gridDiv:2, colorIndex:0, locMul:1.5, zRange: -2000);
			if(2.rand >= 1, sc.rotateRotationSpeed());
			sc.addOrRemoveVertices(true, 3);
		}
	}
	f9{ // Toggle RGBPP in ofxPostProcessing...
		"Not implemented yet".postln;
	}
	f10{

	}
	f11{

	}
	f12{

	}
	f13{

	}
	f14{

	}
	f15{

	}
	createSpacecube{
		|size=200, lifeTime=7000, numVertices=5, gridDiv=2, colorIndex=0, locMul=1.5, zRange = -2000|
			var loc;
			var sc = JSpaceCube.new();
			sc.create();
			sc.resize([size, size], numVertices, gridDiv);
			sc.setColor(colors[colorIndex]);
			loc = [
				~v.w.rand - (~v.w/2),
				(~v.h).rand - 400,
				50.rand * -1
			] * locMul;
			sc.setLoc(loc);
			sc.addEnv("z", [lifeTime, 10, 100], [0, 1,1,1] * zRange, false);
			sc.addEnv("brightness", [100, 10, lifeTime], [0,-1,-1,0], true);
			spacecubes.add(sc);
		^sc;
	}
}