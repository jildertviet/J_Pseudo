JDivisionGrid : JEvent{
	createUnique {
		|a|
		if(layer == nil, {createArgs.add(defaultLayer);}); // Add default layer, so extra arg is not the 3rd, but the 4th
		// createArgs.add(true);
		createArgs.add(size[0]);
		createArgs.add(size[1]);
		this.sendMakeCmd("JDivisionGrid");
	}
	displayMesh { // Display wireframe
		this.doFunc(0);
	}
	saveAllPoly { // Save one frame
		this.doFunc(1);
	}
	setSave { // Sets the save flag
		|state=true|
		this.setCustomArg(0, state); // ...
		this.doFunc(2);
	}
	splitAndSort {
		|num=1|
		this.setCustomArg(0, num);
		this.doFunc(3);
	}
}