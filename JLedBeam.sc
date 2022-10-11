JLedBeam : Jonisk{
	setPixelsGrayScale{
		|id=0, pixels = 0, dur=100, delayTime=0, retry=0|
		var msgId = 0; // ??? This comes from JLCD ...
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 8];
		msg = msg ++ dur ++ pixels;
		msg = address ++ [1] ++ [msg.size+1] ++ msg ++ [address.size + msg.size + end.size + 3] ++ end;
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
	setPixels{
		|id=0, pixels = 0, dur=100,delayTime=0, retry=0|
		var msgId = 0; // ??? This comes from JLCD ...
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 9];
		msg = msg ++ dur ++ pixels;
		msg = address ++ [1] ++ [msg.size+1] ++ msg ++ [address.size + msg.size + end.size + 3] ++ end;
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
	testLed{ // Override
		var msg = (0xFF!6) ++ [0x15] ++ address ++ "end";
		msg.postln;
		this.send(msg);
	}
	setOTAServer{
		|ssid="", password="", url=""|
		var json = "{\"ssid\":\""++ ssid ++"\", \"password\":\""++password++"\", \"url\":\""++url++"\"}";
		var msg = 0xFF!6 ++ [0x11] ++ address ++ json ++ end;
		this.send(msg);
	}
}