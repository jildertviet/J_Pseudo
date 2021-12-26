JPontLights {
	var x, <>serial;
	var order = #[7, 2, 4, 3, 6, 1, 0];
	*new{
		|l|
		^super.new.init(l)
	}
	init {
		|l|
		serial = l;
	}
	sendS{
		|id, r, g, b, w|
		id = order.at(id);
		serial.putAll([id, r.min(254), g.min(254), b.min(254), w.min(254), 0, 0, 255]); // Id, R, G, B, W, mode, lagTime, stopByte
	}
	sendSLag{
		|id, r, g, b, w, lagTime=100|
		id = order.at(id);
		serial.putAll([id, r.min(254), g.min(254), b.min(254), w.min(254), 1, lagTime, 255]); // Id, R, G, B, W, mode, lagTime, stopByte
	}
	sendOSC{
		|id, r, g, b, w|
	}
	sendOSCLag{
		|id, r, g, b, w, lagTime|
	}
}