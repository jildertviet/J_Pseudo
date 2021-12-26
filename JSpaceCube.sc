JSpaceCube : JEvent{
	var <numAngles;
	createUnique {
		|a|
		if(layer == nil, {createArgs.add(defaultLayer);}); // Add default layer, so extra arg is not the 3rd, but the 4th
		createArgs.add(true);
		this.sendMakeCmd("JSpaceCube");
	}
	uniqueInit {
		this.speed = (2.5 + ((1/4)*[1,2,3,4].choose));
		this.setSpeed(this.speed);
	}
	rotateRotationSpeed{
		var speedOptions = [2.5, 3.75, 5.0, 7.5];
		var index = speedOptions.detectIndex({|val| val==this.speed});
		if(index.isNil.not,{
			speed = speedOptions.wrapAt(index+1);
		},{
			speed = speedOptions.choose;
		});
		this.setSpeed(speed);
	}
	randomChooseRotXorRotY{
		this.doFunc(1);
	}
	blink{
		this.doFunc(2);
	}
	lightLine{
		this.doFunc(3);
	}
	resize{
		|newSize = #[400, 400], numPoints=5, gridSize=6|
		this.setCustomArg(0, newSize[0]);
		this.setCustomArg(1, newSize[1]);
		this.setCustomArg(2, numPoints);
		this.setCustomArg(3, gridSize);
		this.doFunc(4);
	}
	addOrRemoveVertices{
		|bAdd=nil, num=1|
		if(bAdd.isNil,{
			bAdd = [true, false].choose;
		});
		this.setCustomArg(0, bAdd.asInteger);
		this.setCustomArg(1, num);
		this.doFunc(0);
	}
}