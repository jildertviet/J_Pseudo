/*
Some extensions to existing classes
*/
+ Buffer {
	exportForAnimation{
		|path="/home/", fps=30, format="csv"|
		var numSamples = this.duration * fps;
		var export = Array.newClear(numSamples);
		numSamples.do{
			|i|
			if(i<(numSamples-1), {
				this.get(((this.duration * this.sampleRate)/numSamples) * i, {|v|v.postln; export[i] = v});
			}, {
				this.get(((this.duration * this.sampleRate)/numSamples) * i, {
					|v|
					v.postln;
					export[i] = v;
					export.postln;
					"Save file...".postln;
				});
			});
		}
	}
}