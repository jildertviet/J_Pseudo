+ Float{
	asBytes {
		^Int8Array.newFrom([this.low32Bits].asRawOSC.at((8..11).reverse) ++ [this.high32Bits].asRawOSC.at((8..11)).reverse);
	}
}