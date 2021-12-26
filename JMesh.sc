JMesh : JEvent{
	var <numAngles;
	createUnique {
		|a|
		if(layer == nil, {createArgs.add(defaultLayer);}); // Add default layer, so extra arg is not the 3rd, but the 4th
		createArgs.add(true);
		this.sendMakeCmd("JMesh");
	}
	doSave {
		~visualUDP.sendMsg("/doFunc", id, 0);
	}
	save{
		~visualUDP.sendMsg("/doFunc", id, 1);
	}
	makeHollowRect{
		|dimensions=#[100, 100, 100], size=20|
		// Send params first!
		dimensions.do{
			|x, i|
			this.setCustomArg(i, x);
		};
		this.setCustomArg(3, size);
		~visualUDP.sendMsg("/doFunc", id, 2);
	}
}
