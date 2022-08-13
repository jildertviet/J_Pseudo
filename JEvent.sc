JEvent {
	var <id, <>color, <>loc, <size, <direction, <>speed, <>bMove=nil, <layer, <>createArgs, <>mode, <>zoom=1;
	classvar <nonCamLayer = 4;
	classvar <defaultLayer = 2;
	var bCreateSend = false;
	var <>rotation = #[0, 0, 0];
	var guiWindow = nil;
	var <pointer = #[0,0];
	var <>bBundle = false;
	var <>bundle;
	var <>visualizerID = 0;
	// var <>type="JEvent";
	var <>modulators;
	*new{
		|layer=2, visualizerID=0|
		^super.new.init(layer, visualizerID)
	}
	uniqueInit {

	}
	createUnique{

	}
	create {
		if(bCreateSend == false && id != nil, {
			this.createUnique();
			bCreateSend = true;
		})
	}
	sendMsg {
		|msgAsArray|
		if(this.bBundle == true, {
			this.bundle.add(msgAsArray);
			// If bundle > 20, send current bundle w/ endbundle
		}, {
			// ("sendBundle: " ++ msgAsArray).postln;
			~v[visualizerID].netAddr.sendBundle(0.1, msgAsArray);
		});
	}
	init {
		|layer_, visualizerID_|
		layer = layer_;
		visualizerID = visualizerID_;
		id = ~v[visualizerID].getFreeAddress();

		createArgs = List.newFrom(["/make", "type", id]);
		if(layer == nil,
			{createArgs.add("nonCamFront")},
			{createArgs.add(layer)},
			);
		color = Color.white;

		// ("New event with id: " ++ id).postln;
		this.uniqueInit();
		bundle = List.new();
		modulators = List.new();
		// this.setType();
	}
	/*setType{
		type = "JEvent"
	}*/
	startBundle{
		this.bBundle = true;
	}
	endBundle{
		var msg = bundle;
		this.bBundle = false;
		("visualizerID: " ++ visualizerID).postln;
		switch(bundle.size,
			1, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0]);},
			2, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1]);},
			3, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2]);},
			4, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3]);},
			5, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4]);},
			6, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5]);},
			7, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6]);},
			8, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7]);},
			9, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8]);},
			10, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9]);},
			11, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10]);},
			12, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10], msg[11]);},
			13, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10], msg[11], msg[12]);},
			14, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10], msg[11], msg[12], msg[13]);},
			15, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10], msg[11], msg[12], msg[13], msg[14]);},
			16, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10], msg[11], msg[12], msg[13], msg[14], msg[15]);},
			17, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10], msg[11], msg[12], msg[13], msg[14], msg[15], msg[16]);},
			18, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10], msg[11], msg[12], msg[13], msg[14], msg[15], msg[16], msg[17]);},
			19, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10], msg[11], msg[12], msg[13], msg[14], msg[15], msg[16], msg[17], msg[18]);},
			20, {~v[visualizerID].netAddr.sendBundle(0.1, msg[0], msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10], msg[11], msg[12], msg[13], msg[14], msg[15], msg[16], msg[17], msg[18], msg[19]);},
		);
		bundle.clear();
	}
	killAll {
		this.sendMsg(["/killAll"]);
		~numEvents = 1;
	}
	sendMakeCmd {
		|type|
		createArgs[1] = type;
		this.sendMsg(createArgs.asArray);
	}
	setVal {
		|name, value|
		var msg = List.newUsing(["/setVal", id, name]);
		if(value.isArray  && (value.isString == false), {
			msg = msg ++ value;
			this.sendMsg(msg.asArray);
		}, {
			msg.add(value);
			// msg.postln;
			this.sendMsg(msg.asArray);
		});
	}
	setColor { // Moved first arg from [4] to Color, probably breakes some stuff :o
		|c="Color.white", colorIndex=0|
		if(c == "Color.white", {color = Color.white});
		if(c.isArray, {c = Color.new255(c[0], c[1], c[2], c[3])});
		color = c;
		c = c.toJV; // floats to unsigned char (0-255)
		if(colorIndex == 0, {
			// c = Color.asArray
			// ~visualUDP.sendMsg("/setVal", id, "color", c[0], c[1], c[2], c[3]);
			this.setVal("color", c);
		}, {
			this.setVal("color", c ++ colorIndex);
		});
		// And send an OSC msg
	}
	colorPicker{
		// ~v.reloadColorPicker;
		// ~v.colorPicker.front;
		// ~setColorFuncTemp = {this.postln; this.setColor(~colorFromPicker.asArray.asFloat * [1,1,1,255]);};
		// ~v.colorPicker.onClose_({this.postln; this.setColor(~colorFromPicker.asArray.asFloat * [1,1,1,255]);});

		// "open /Users/jildertviet/of_v20201026_osx_release/apps/visuals/colorPicker/bin/colorPickerDebug.app/".unixCmd; // Focuses when already open
		// ~colorUpdateListener.free; ~colorUpdateListener = OSCFunc({|msg, time, addr, recvPort| this.setColor([msg[1], msg[2], msg[3], msg[4]]); msg.postln}, '/colorUpdate', recvPort: 7676).oneShot;
		var colorPicker = ColorPicker.new(this.color, {|result| this.setColor(result)}); ~tempWindow  = Window.new.front; ~tempWindow.close;
		colorPicker.value_(this.color);
	}
	setAlpha {
		|a=1.0|
		color.alpha = a;
		this.setVal("alpha", a*255);
	}
	setLoc {
		|l=#[0,0]|
		loc = l;
		this.setVal("loc", loc);
	}
	setSize {
		|si=#[100,100], bSendUpdate = true|
		size = si;
		// si.size.postln;
		if(bSendUpdate == true, {
			this.setVal("size", size);
		})
	}
	setRotation {
		|rot=#[0, 0, 0]|
		rotation = rot;
		this.setVal("rotation", rot);
	}
	setMode {
		|m = 0|
		mode = m;
		this.setVal("mode", m);
	}
	setMove {
		|m|
		bMove = m;
		this.setVal("bMove", m);
	}
	setDirection {
		|d|
		direction = d;
		this.setVal("direction", d);
	}
	setSpeed {
		|sp|
		speed = sp;
		this.setVal("speed", speed);
	}
	setZoom {
		|zoomArg|
		zoom = zoomArg;
		this.setVal("zoom", zoom);
	}
	addEnv {
		|name = "brightness", times = #[500, 500, 500], values = #[0, 255, 255, 0], kill = false, bSave = false|
		var msg = ["/addEnv", id, name, times[0].asFloat, times[1].asFloat, times[2].asFloat, values[0].asFloat, values[1].asFloat, values[2].asFloat, values[3].asFloat, kill.asBoolean, bSave.asBoolean];
		this.sendMsg(msg);
	}
	killEnv{
		|release=1000|
		this.addEnv("brightness", [5, 5, release], [-1, -1, -1, 0], true);
	}
	addTo{
		|type="loc", value|
		var msg = ["/addTo", id, type, value];
		this.sendMsg(msg);
	}
	addLoc {
		|l=#[0,0,0]|
		var msg;
		if(loc == nil, {loc = [0, 0, 0]});  // Set a random loc
		if(l.size == 3, {loc = loc + l}, {loc = loc + [l[0], l[1], 0]}); // If 3 coords: [a,b,c] + [d,e,f], if 2 coords [a,b,c]+[d,e,0]
		msg = ["/addTo", id, "loc"] ++ l;
		this.sendMsg(msg);
	}
	setCustomArg {
		|index, value|
		this.setVal("customArg", [index.asInteger, value.asFloat]);
		// ~visualUDP.sendMsg("/setVal", id, "customArg", index.asInteger, value.asFloat);
	}
	doFunc {
		|functionId=0|
		var msg = ["/doFunc", id, functionId.asInteger];
		this.sendMsg(msg);
	}
	basicGui {
		var sliderDimensions = 380@20;
		guiWindow = Window(" GUI", Rect(0, 0, 400, 10)).front;
		guiWindow.view.decorator = FlowLayout(guiWindow.view.bounds, 10@10, 20@5);
		// Slider2D(guiWindow, size.x@size.x * 0.45).x_(0).y_(1.0).action_({|val| (1.0-val.y).postln}); Loc
		// Slider2D(guiWindow, size.x@size.x * 0.45).x_(0).y_(1.0).action_({|val| (1.0-val.y).postln}); Size
		guiWindow.setInnerExtent(guiWindow.bounds.width, guiWindow.bounds.height + (25*0) + 10);
		^sliderDimensions;
	}
	gui {
		this.basicGui();
	}
	testName {
		this.class.name.postln;
	}
	mod {
		|parameters="width", modulator="{SinOsc.kr(1)}", link=false|
		if(modulator != nil, {
			var paramIDs = [];
			if((parameters.isString), {
				parameters = [parameters];
			});
			paramIDs = parameters.collect({|e|~v[0].getParamId(this.class.name, e)});
// var paramId = ~v[0].getParamId(this.class.name, parameter); // "JEvent", "width" should lookup.
			if(~of != nil, {
				if(~of.serverRunning, {
					if(paramIDs.size != 0, {
						// Create Synth
						var bus = Bus.alloc(\control, ~of, paramIDs.size).set(0);
						var sender;
						modulator = modulator.play(~of, outbus: bus);
						sender = {SendReply.kr(Impulse.kr(~v[0].frameRate), "/mapVal",
							[id, paramIDs.size] ++ paramIDs ++ In.kr(bus, paramIDs.size))}.play(~of);
						if(link == true,
							modulator.onFree{
								("killEnv on " ++ this.asString).postln;
								this.killEnv();
								this.stopMod();
						});
						modulators.add([sender, modulator, bus]);
						// ("Creating modulator to event id " ++ id.asString ++ " to param ID: " ++ paramId.asString ++ " with modulator " ++ modulator).postln;
					}, {
						("Parameter " ++ parameters ++ " doesn't seem to be mappable").error;
					});
				}, {
					"Remote server not running".error;
				});
			});
		});
	}
	stopMod{
		modulators.do{
			|e|
			e.do{
				|f|
				f.free;
			}
		};
		modulators.clear;
	}
}

JEllipse : JEvent{
	createUnique {
		this.sendMakeCmd("JEllipse");
	}
}

JText : JEvent{
	createUnique {
		this.sendMakeCmd("JText");
	}
}

JMirror : JEvent{
	var <angle;
	createUnique {
		this.sendMakeCmd("JMirror");
		this.bMove = false;
		this.color = Color.white;
	}
	setAngle {
		|a|
		this.setVal("angle", ["JMirror", a]);
		// ~visualUDP.sendMsg("/setVal", id, "angle", "JMirror", a);
	}
}

JShaderTest : JEvent{
	createUnique {
		if(layer==nil, {layer=2});
		createArgs.add(layer);
		createArgs.add(size[0]);
		createArgs.add(size[1]);
		this.sendMakeCmd("JShaderTest");
	}
	setDivisions {
		|div=#[16,16]|
		this.setCustomArg(0, div[0]);
		this.setCustomArg(1, div[1]);
		this.doFunc(0);
	}
}

JNoise : JEvent {
	var <>numLines = 100;
	createUnique {
		this.sendMakeCmd("JNoise");
	}
	setNumLines{
		|numLines=100|
		this.setCustomArg(0, numLines.asInteger);
		this.doFunc(0);
	}
}

JLinesFalling : JEvent { // Quick Batobe, Maybe Tomorrow visuals ripoff
	createUnique {
		this.sendMakeCmd("JLinesFalling");
	}
}

+ SimpleNumber{
	midiToFloat{
		// ("Receiving this in function: " ++ val).postln;
		^(this/127.0);
	}
}

+ Color{
	toJV {
		^(this.asArray*255).asInteger
	}
}