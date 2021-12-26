/*
TODO
f12
*/
JBendStraws : JSong {
	var <>divisions, <>radia, <>shape, <>maxShapes = 9, <>shapes, prev=nil, <>defaultLineMax=50;
	init {
		"JBendStraws init".postln;
		shape = [6].choose;
		divisions =  [2, 4, 6, 7].wrapExtend(9);
		maxShapes = divisions.size;
		radia = [50, 140, 230, 320, 80, 170, 260, 350, 110] * [1].choose;
		shapes = List.new();
	}
	f0 {
		var v;
		if(shapes.size >= maxShapes, {^false});

		v = JVorm.new;
		v.create();
		v.addEnv("brightness", [400, 10,100], [0] ++ (255!3), false);
		v.setShape(shape, divisions[shapes.size], radia[shapes.size]);

		v.randomSpeed(1.0*0.8, 1.5*0.5);
		v.placeParticlesAtBorder();
		v.setState(true);
		v.setLineMax(pow(defaultLineMax, 2));
		v.setVal("lineWidth", 3);
		if(prev.isNil.not,{
			v.addConnection(prev);
		});
		shapes.add(v);
		prev = v;
	}
	f1 { // Switch radius
		var a,b;
		var scrambled;
		if(shapes.size <= 1, {^false});
		scrambled = shapes;
		scrambled = scrambled.scramble;
		a = scrambled[0];
		b = scrambled[1];
		a.switchRadius(b);
	}
	f2 {
		var a,b;
		var scrambled;
		if(shapes.size <= 1, {^false});
		scrambled = shapes;
		scrambled = scrambled.scramble;
		a = scrambled[0];
		b = scrambled[1];
		a.switchRadius(b, true);
	}
	f3{ // Add noise
		this.f2.value();
		this.f7.value();
	}
	f4{  // Rotate vorm
		shapes.choose.changeAngleOffset(15 * [-1,1].choose);
	}
	f5{ // In formation
		shapes.do{|v| v.formInstant()};
	}
	f6{ // oneFrame
		shapes.do{|v| v.oneFrame()};
	}
	f7{
		shapes.do{|v| v.addNoise()};
	}
	f8{ // LineMax to default
		shapes.do{|v| v.setVal("lijnMax", (pow(defaultLineMax, 2.0)))};
	}
	f9{ // Random Line Max||
		shapes.do{|v| v.setVal("lijnMax", pow(40, 2.0).rrand(pow(110, 2.0)))};
	}
	f10{ // White gradient

	}
	f11{ // Free/Form Vorm
		shapes.do{|v| v.setState(v.state.not)};
	}
	f12{ // TRANSFORM
		shapes.do{|v| v.killEnv(10)};
		shapes.clear();
		5.do{
			|i|
			var v = JVorm.new;
			v.create();
			v.setShape(4, 4 + i, 200+(50*i));
			v.formInstant();
			v.randomSpeed(25, 26);
			v.setState(true);
			v.setLineMax(pow(defaultLineMax, 2));
			v.setVal("lineWidth", 3);
			v.setLineMax(pow(100, 2));
			shapes.add(v);
		};
		shapes.do{
			|v, i|
			shapes.do{
				|w, j|
				if(j >= i, {
					if(v!=w, {
						v.addConnection(w);
					});
				});
			}
		}
	}
	f13{ // Env @ linemax
		shapes.do{|v| v.addEnv("lijnMax", [10, 10, 800], v.lineMax * [1,2,2,1])};
	}
	f14{
	}
	f15{
	}
}