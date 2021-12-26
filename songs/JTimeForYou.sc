JTimeForYou : JSong {
	var <>multimeshes, <>colors;
	init {
		"JTimeForYou init".postln;
		functions = Array.newClear(16);
		multimeshes = List.newClear(0);
		colors = [Color(1,1,1,1), Color(0.6, 0.6, 0.6, 1),Color(0.97254901960784,0.7921568627451,0,1),Color(0.90980392156863,0.74901960784314,0.33725490196078, 1)];
		4.do{|i| colors[i].alpha = 0.5.rand + 0.5};
	}

	f0 { // Start
		this.deleteMeshes();
		4.do{ // Create 4 meshes
			|i|
			var m = JMultiMesh.new();
			m.create();
			m.setColor(colors[i]);
			m.generateMeshes(1, 1); // Generate 1 random mesh
			m.setFreq(85/60);
			m.addEnv("brightness", [12500, 100, 100], [0] ++ ((m.color.alpha*255)!3), false);
			multimeshes.add(m);
		}
	}
	f1 { // Add mesh to MultiMesh
		var m = multimeshes.choose;
		m.addRandomMesh(400, colors.choose);
	}
	f2 { // Infinite grow (solo)
		this.f3();
		multimeshes.do{|m| m.setVal("evolve", true)};
	}
	f3{ // Chorus
		var r = 350;
		var alpha = 150;
		multimeshes.do{
			|m|
			m.growRadius(true);
			m.killEnv(1000);
		};
		multimeshes.clear();

		[24,25,16,17].do{|i,j|
			var m = JMultiMesh.new();
			var multipliers = 1/[2,4,8];
			m.create();
			m.setColor(this.colors[j]);
			m.generateMeshes(0, i, r);
			m.addEnv(times: [300, 10, 10], values: [0] ++ (alpha!3));
			m.setFreq(85/60, multipliers);
			multimeshes.add(m);
    }
	}
	f4{ // Move with pulse
		multimeshes.do{|m| m.setMove(m.bMove.not)};
	}
	f5{ // Delete chorus meshes
		multimeshes.do{
			|m|
			m.growRadius(true);
			m.killEnv(1000);
		};
		multimeshes.clear();
	}
	f6{ // Empty

	}
	f7{ // Add radius ||
		multimeshes.do{|m| m.addRadius(100)};
	}
	f8{ // Verse
		var alpha = 150;
		this.deleteMeshes();
		[4,5,6,7].do{
			|i,j|
			var m = JMultiMesh.new();
			m.create();
			m.setColor(this.colors[j]);
			m.generateMeshes(1, i);
			m.addEnv(times: [300, 10, 10], values: [0] ++ (alpha!3));
			multimeshes.add(m);
		}
	}
	f9{ // Change one MultiMesh
		var index = multimeshes.size.rand;
		var m = multimeshes[index];
		if(m.isNil.not,{
			var num = m.num;
			var radius = m.radius;
			var color = m.color;
			var newM;
			// Delete this one, replace with same num, radius and color
			m.killEnv(1);
			newM = JMultiMesh.new();
			newM.create();
			newM.generateMeshes(0, num, radius);
			newM.setColor(color);
			newM.addEnv(times: [300, 10, 10], values: [0] ++ ((color.alpha*255)!3));
			newM.setFreq(85/60, 1/[2,4,8]);
			multimeshes[index] = newM;
		});
	}
	f10{ // Rotate vertices
		var angle = 360.rand/3000.rrand(5000);
		var time = 500.rrand(3000);
		multimeshes.do{|m| m.doMorph(angle, time)};
	}
	f11{ // Double Time
		multimeshes.do{|m| m.setSpeed(2)};
	}
	f12{ // Half time
		multimeshes.do{|m| m.setSpeed(0.5)};
	}
	f13{ // Change Mode
		multimeshes.do{|m| m.setMove(m.bMove.not)}; // Needs to call a function :/
	}
	deleteMeshes{
		this.multimeshes.do{
			|m|
			m.growRadius(true);
			m.killEnv(1000);
		};
		this.multimeshes.clear();
	}
}