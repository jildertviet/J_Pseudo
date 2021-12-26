JMultiMesh : JEvent{
	var <>num, <>radius;
	createUnique {
		|a|
		if(layer == nil, {createArgs.add(defaultLayer);}); // Add default layer, so extra arg is not the 3rd, but the 4th
		createArgs.add(true);
		this.sendMakeCmd("JMultiMesh");
		bMove = false;
	}
	generateMeshes {
	|type=0, numMeshes=12, radius=350| // type 0: generateSymmetricMeshes, type 1: generateRandomMeshes
		this.num = numMeshes;
		this.radius = radius;
		this.setCustomArg(0, type); // Set arguments for custom function
		this.setCustomArg(1, numMeshes);
		this.setCustomArg(2, radius);
		~visualUDP.sendMsg("/doFunc", id, 0);
	}
/*	setBpm{ // Overruled by setFrequency?
		|bpm|
		this.setCustomArg(0, bpm);
		~visualUDP.sendMsg("/doFunc", id, 1); // Set bpm
	}*/
	setFreq{
	|freq, multipliers|
		multipliers = multipliers ++ [1,1,1];
		this.setCustomArg(0, freq);
		this.setCustomArg(1, multipliers[0]);
		this.setCustomArg(2, multipliers[1]);
		this.setCustomArg(3, multipliers[2]);
		this.doFunc(2); // Set freq
	}
	addRandomMesh{
		|r=100, color|
		var c255;
		if(color.isArray,{
			c255 = color;
		},{
			c255 = color.asArray * 255;
		});
		this.setCustomArg(0, r);
		this.setCustomArg(1, c255[0]);
		this.setCustomArg(2, c255[1]);
		this.setCustomArg(3, c255[2]);
		this.setCustomArg(4, c255[3]);
		this.doFunc(3);
	}
	growRadius{
		|val=true|
		val = val.asInteger.asFloat;
		this.setCustomArg(0, val);
		this.doFunc(4);
	}
	doMorph{
		|angle=45, time=1000|
		this.setCustomArg(0, angle);
		this.setCustomArg(1, time);
		this.doFunc(1);
	}
	addRadius{
		|radius=100|
		this.addTo("radius", radius);
	}
}