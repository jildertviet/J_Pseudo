+ JVisuals{
	initMapValues {
		var t = Dictionary.new();
		var default = ["width","height","depth","loc.x","loc.y","loc.z","alpha","r","g","b","speed","direction.x","direction.y","direction.z","zoom"]; // 0 - 14
		t.put(\JRectangle, default);
		mapValues = t;
	}
	getParamId{
		|type, name|
		mapValues.at(type.asSymbol).do{
			|p, i|
			if(p == name, {
				^i
			});
		}
		^nil;
	}
}