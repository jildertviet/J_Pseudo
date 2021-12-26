JDivisionGrid : JEvent{
	createUnique {
		|a|
		if(layer == nil, {createArgs.add(defaultLayer);}); // Add default layer, so extra arg is not the 3rd, but the 4th
		createArgs.add(true);
		this.sendMakeCmd("JDivisionGrid");
	}
	displayMesh { // Display wireframe
		~visualUDP.sendMsg("/doFunc", id, 0);
	}
	saveAllPoly { // Save one frame
		~visualUDP.sendMsg("/doFunc", id, 1);
	}
	setSave { // Sets the save flag
		|state=true|
		this.setCustomArg(0, state);
		~visualUDP.sendMsg("/doFunc", id, 2);
	}
}