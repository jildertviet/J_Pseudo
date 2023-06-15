/*
Some extensions to existing classes
*/
+ Buffer {
	exportForAnimation{
		|path="/home/", fps=30|
		var numSamples = this.duration * fps;
		var export = Array.newClear(numSamples);
		("Will output a file with " ++ numSamples ++ " samples").postln;
		{
			numSamples.do{
				|i|
				var frameNum = ((this.duration * this.sampleRate)/numSamples) * i;
				// ("frameNum: " ++ frameNum).postln;
				if(i<(numSamples.asInteger-1), {
					// frameNum.postln;
					this.get(frameNum, {|v| export[i] = v});
					0.005.wait;
				}, {
					"Last frame, save".postln;
					{this.get(frameNum, {
						|v|
						var file;
						// v.postln;
						export[i] = v;
						// export.postln;
						("Save file " ++ path).postln;
						file = File(path.standardizePath,"w");
						export.do{
							|value|
							file.write(value.asString ++ ",");
						};
						file.close;
					});
					}.defer(0.2)
				});
			};
		}.fork;
	}
}
+ Node {
	nodeID32{
		^(Float.from32Bits(this.nodeID));
	}
}