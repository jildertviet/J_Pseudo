+ Array {
	elementsToString{
		var r = "";
		this.do{|e| r = r ++ e.asString};
		^r;
	}
  asBytes32F{
    var r = this.asFloat;
    ^r.collect({|e| e.asBytes32()}).reshape(this.size * 4);
  }
}
