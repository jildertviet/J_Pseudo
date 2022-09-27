+ Array {
	elementsToString{
		var r = "";
		this.do{|e| r = r ++ e.asString};
		^r;
	}
}