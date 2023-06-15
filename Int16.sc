Int16{
	var <> value;
	*new{
		|v|
		^super.new.init(v);
	}
	init{
		|v|
		value = v
	}
	asBytes{
		^ [value % 256, floor(value/256).asInteger];
	}
}
