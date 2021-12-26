JVecField : JEvent{
	var <lineLength, <complexity, <density, <mode, <lineWidth;
	var <>srcMode;
	createUnique {
		this.sendMakeCmd("JVecField");
	}
	setLineLength {
		|l|
		lineLength = l;
		~visualUDP.sendMsg("/setVal", id, "lineLength", "JVecField", l);
	}
	setLineWidth {
		|l|
		lineWidth = l;
		~visualUDP.sendMsg("/setVal", id, "lineWidth", l);
	}
	setComplexity {
		|c|
		complexity  = c;
		~visualUDP.sendMsg("/setVal", id, "complexity", "JVecField", c);
	}
	setDensity {
		|d = #[128, 80]|
		density = d;
		~visualUDP.sendMsg("/setVal", id, "density", "JVecField", d[0], d[1]);
	}
	setDrawMode {
		|m = "lines"|
		mode = m;
		if(m == "lines", {m = 2}); // Like the enum in JVecField.hpp
		if(m == "circles", {m = 3});
		if(m == "hide", {m = 4});
		if(m == "texture", {m = 7});
		~visualUDP.sendMsg("/setVal", id, "drawMode", m);
	}
	setSourceMode {
		|m="underlaying"|
		srcMode = m;
		if(m == "perlin", {m = 0});
		if(m == "test", {m = 1});
		if(m == "underlaying", {m = 6});
		m.postln;
		~visualUDP.sendMsg("/setVal", id, "mode", m);
	}
	setContrast {
		|c=1.0|
		this.setCustomArg(0, c);
		~visualUDP.sendMsg("/doFunc", id, 0);
	}
}