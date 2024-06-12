+ Float{
	asBytes {
    "Use .asBytes64 (Float)".warning;
		^Int8Array.newFrom([this.low32Bits].asRawOSC.at((8..11).reverse) ++ [this.high32Bits].asRawOSC.at((8..11)).reverse);
	}
  asBytes64{
    ^this.asBytes;
  }
  asBytes32{
    var r = 0!4; 
    this.as32Bits.asBinaryDigits(32).do{|e, i| r[(i/8).asInteger] = r[(i/8).asInteger] + ((2.pow((7-(i%8))) * e).asInteger)}; 
    r = r.reverse;
    ^r;
  }
}
+ Integer {
  asBytes32{
    var r = 0!4; 
    this.asBinaryDigits(32).do{|e, i| r[(i/8).asInteger] = r[(i/8).asInteger] + ((2.pow((7-(i%8))) * e).asInteger)}; 
    r = r.reverse;
    ^r;
  }
}
