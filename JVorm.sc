JVorm : JEvent{
	var <numAngles, <>lineMax, <>state;
	createUnique {
		|a|
		if(layer == nil, {createArgs.add(defaultLayer);}); // Add default layer, so extra arg is not the 3rd, but the 4th
		createArgs.add(true);
		this.sendMakeCmd("JVorm");
	}
	setShape {
	|num = 4, sideDiv = 2, radius=100|
		this.setCustomArg(0, num); // numSides
		this.setCustomArg(1, sideDiv); // div
		this.setCustomArg(2, radius); // radius
		this.doFunc(0);
	}
	placeParticlesAtBorder {
		this.doFunc(1);
	}
	setState {
		|state=true|
		this.state = state;
		this.setVal("bForm", ["JVorm", state]);
	}
	randomSpeed {
		|min, max|
		this.setVal("maxSpeed", ["JVorm", min, max]);
	}
	// setMaxSpeed {
	// |maxSpeed=1|
	// ~visualUDP.sendMsg("/setVal", id, "maxSpeed", "JVorm", maxSpeed);
// }
	// addConnection {
		// |i|
		// ~visualUDP.sendMsg("/addConnection", id, i.id);
	// }
	// switchRadius {
		// |with, instant = false|
		// ~visualUDP.sendMsg("/switchRadius", id, with.id, instant.asBoolean);
	// }
	addNoise {
		this.doFunc(2);
	}
	// changeAngleOffset {
	// |degrees|
	// ~visualUDP.sendMsg("/changeAngleOffset", id, degrees);
// }
	oneFrame { // Boring?
		this.doFunc(3);
	}
	formInstant{
		this.doFunc(4);
	}
	setLineMax{
		|val=4000|
		lineMax = val;
		this.setVal("lijnMax", val);
	}
}
