+ JVisuals{
	initMapValues {
		var t = Dictionary.new();
		t.put(\JRectangle, ["width", "height"]);
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