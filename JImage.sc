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
		path = p;
	}
	load{
		FileDialog({ |paths|
			path = paths[0].asString;
			path.postln;
			this.setVal("path", path.asString);
		}, {
			postln("Dialog was cancelled. Try again.");
	});
	}
}