JLedBeam : JLCD{
	setPixelsGrayScale{
		|id=0, pixels = 0, dur=100, delayTime=0, retry=0|
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 8];
		msg = msg ++ dur ++ pixels;
		msg = addr ++ [1] ++ [msg.size+1] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end;
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
	setPixels{
		|id=0, pixels = 0, dur=100,delayTime=0, retry=0|
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 9];
		msg = msg ++ dur ++ pixels;
		msg = addr ++ [1] ++ [msg.size+1] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end;
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
}