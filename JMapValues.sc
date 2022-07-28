+ JVisuals{
	initMapValues {
		// paramIDs 0 - 14
		var default = ["width","height","depth","loc.x","loc.y","loc.z","alpha","r","g","b","speed","direction.x","direction.y","direction.z","zoom"];
		var subClasses = JEvent.subclasses;
		mapValues = Dictionary.new();
		subClasses.do{
			|e|
			mapValues.put(e.name.asSymbol, default);
		}
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