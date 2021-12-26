JVideoPlayer : JEvent{
	var <>path;
	createUnique {
		|a|
		if(layer == nil, {createArgs.add(defaultLayer);}); // Add default layer, so extra arg is not the 3rd, but the 4th
		createArgs.add(true);
		this.sendMakeCmd("JVideoPlayer");
	}
	load {
		|p="/path/to/movie.mp4"|
		this.setVal("path", p.asString);
		this.doFunc(0); // Load
	}
	setPos {
		|percent=0.0|
		this.setCustomArg(0, percent);
		this.doFunc(1); // Go to pos
	}
}
