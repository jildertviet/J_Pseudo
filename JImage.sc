JImage : JEvent{
	var <lineLength, <complexity, <density, <>path;
	createUnique {
		if(path != nil, {
			createArgs.add(path);
		});
		this.sendMakeCmd("JImage");
	}
	setPath {
		|p|
		this.path = p;
	}
}