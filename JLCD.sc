JLCD{
	var <numLCDs = 26;
	var <macAddresses;
	var <netAddr;
	var <serialPorts;
	var <msgId = 0;
	var end = #[101,110,100]; // Bytes to identify the ending of a msg: 'e' 'n' 'd'
	var <macWithSerial;
	var <oscReceiver;
	var <oscSender;
	var <visualizerBacklightValues;
	var <msgSpacingTime = 0.006;
	var updateFromVisualizer;
	var <>bUseNetAddr;
	var addr = #[0,0,0,0,0,0]; // Placeholder
	var <>baud = 115200;
    *new { |baud=115200|
        ^super.new.init(baud);
    }
    init { |baud|
		this.baud = baud;
		serialPorts = Array.newClear(2);
		netAddr = NetAddr("localHost", 5555);
		macAddresses = Array.newClear(numLCDs);
		visualizerBacklightValues = Array.fill(macAddresses.size, {[0,0]});
		macAddresses[0] = [0x24,0x6F,0x28,0xDD,0x47,0xF1]; // 0
		macAddresses[1] = [0x24,0x6F,0x28,0xDD,0x47,0xD9]; // 1
		macAddresses[2] = [0x24,0x6F,0x28,0xF2,0x83,0x41]; // 2
		macAddresses[3] = [0x24,0x6F,0x28,0xDD,0x47,0xDD]; // 3
		macAddresses[4] = [0x24,0x6F,0x28,0xDD,0x48,0x61]; // 4
		macAddresses[5] = [0x24,0x6F,0x28,0xDD,0x48,0xB1]; // 5
		macAddresses[6] = [0x24,0x6F,0x28,0xDD,0x48,0xCD]; // 6
		macAddresses[7] =  [0x24,0x6F,0x28,0xDD,0x48,0x51]; // 7
		macAddresses[8] =  [0x24,0x6F,0x28,0xDC,0xC0,0x61]; // 8
		macAddresses[9] = [0x24,0x6F,0x28,0xDD,0x47,0xFD]; // 9
		macAddresses[10] = [0x24,0x6F,0x28,0xDD,0x48,0x15]; // 10
		macAddresses[11] = [0x24,0x6F,0x28,0xDD,0x48,0x71]; // 11
		macAddresses[12] = [0x24,0x6F,0x28,0xDD,0x48,0x8D]; // 12
		macAddresses[13] = [0x24,0x6F,0x28,0xF2,0x83,0x21]; // 13
		macAddresses[14] = [0x24,0x6F,0x28,0xDD,0x01,0xB1]; // 14
		macAddresses[15] = [0x24,0x6F,0x28,0xDC,0xBF,0xDD]; // 15
		macAddresses[16] = [0x24,0x6F,0x28,0xF1,0x9D,0x49]; // 16
		macAddresses[17] = [0x24,0x6F,0x28,0xF1,0x9A,0xC1]; // 17
		macAddresses[18] = [0x24,0x6F,0x28,0xDC,0xC0,0x11]; // 20
		macAddresses[19] = [0x24,0x6F,0x28,0xDD,0x46,0xB5]; // 23
		macAddresses[20] = [0x24,0x6F,0x28,0xDD,0x48,0x5D]; // Was 25 // 24
		macAddresses[21] = [0x24,0x6F,0x28,0xDD,0x47,0xF5]; // 26
		macAddresses[22] = [0x24,0x6F,0x28,0xDD,0x48,0xBD]; // 27
		macAddresses[23] =	[0x24,0x6F,0x28,0xDD,0x48,0x11]; // 28
		macAddresses[24] = [0x24,0x6F,0x28,0xDD,0x48,0xA1]; // 29
		macAddresses[25] = [0x24,0x6F,0x28,0xDD,0x48,0x25];
		macWithSerial = Array.fill(macAddresses.size, {|i| [macAddresses[i], nil]});
		this.openDefaultSerial();
		oscReceiver = OSCdef(\receiveFromVisualizer, {|msg, time, addr, recvPort|
			msg.removeAt(0);
			msg.do{
				|v, i|
				visualizerBacklightValues[i/2][i%2] = v;
			};
			// visualizerBacklightValues.postln;
		}, '/fromVisualizer', recvPort: 3456); // def style
		oscSender = NetAddr("localhost", 5555);
    }
	linkSerialWithMac {
		|mac, serial|
		macWithSerial.do{
			|e, i|
			if(e[0] == mac, {
				macWithSerial[i][1] = serial;
				("Linked ["++i.asString++"]: ").post; macWithSerial[i][0].postln;
				^false;
			});
		}
	}
	openDefaultSerial {
		var portsToOpen = List.new();
		SerialPort.devices.do{
			|e|
			if(e.find("SLAB") != nil, {
				if(e.find("tty") != nil, {
					e.postln;
					portsToOpen.add(e);
			})});
			if(e.find("cu") != nil, {
				if(e.find("usbserial") != nil, {
					e.postln;
					portsToOpen.add(e);
			})});
		};
		portsToOpen.do{
			|e, i|
			(e ++ " opened").postln;
			serialPorts[i] = SerialPort.new(e, baud, crtscts: true);
			this.startListeningOnPort(serialPorts[i]);  // Also start listening for slaves!
		};
	}
	setSerialPort {
		|serialPortTemp, id=0|
		serialPorts[0] = serialPortTemp;
	}
	startListeningOnPort{
		|p|
		// Request slaves w/ 000000111xend
		"listening for slaves...".postln;
		~readSlaves = Routine({
			var byte, str, res;
			99999.do{|i|
				if(p.read==10, {
					str = "";
					while({byte = p.read; byte !=13 }, {
						str= str++byte.asAscii;
					});
					("String: "+str).postln;
					if(str.find("slaves:") != nil,{
						str = str.replace("slaves:", "");
						"Here".postln;
						str.split($:).do{
							|e|
							if(e.size >= 6,{
								e = e.split($,).asInteger;
								e.removeAt(6);
							});
							this.linkSerialWithMac(e.asArray, p);
						};
					});
				});
			};
		}).play;
		this.requestSlaves(p); // Only works when send twice...
		this.requestSlaves(p);
	}
	requestSlaves {
		|p|
		p.putAll([0,0,0,0,0,0,1,1,1,255,101,110,100]);
	}
	msgIncrease{
		msgId = msgId + 1;
		msgId = msgId % 255;
		if(msgId == 0, {
			msgId = 1
		});
	}

	// The functions for each LCD
	testMsg{ // Deprecated syntax...
		|id=0, value=127, delayTime=0, retry=0|
		if(retry > 0, {
			retry.do{
				// Re-send the msg with new delayTime
				// delayTime.shortToCharArray[0], delayTime.shortToCharArray[1]
			};
		}, {
			if(macWithSerial[id][1]!=nil, {
				var numMsgs = 1;
				var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 0, value]; // 1: msg type, 2: both leds
				var addr = macAddresses.at(id);
				msg = [numMsgs] ++ [msg.size] ++ msg; // num, size, msg <-- Can be '2, 2, a, b, 3, a, b, c', will be parsed as two msgs
				macWithSerial[id][1].putAll(addr ++ msg ++ [addr.size + msg.size + end.size + 1] ++ end);
			});
			netAddr.sendMsg("/", 0, 0, msgId, id, 0, value);
		});
		this.msgIncrease();
	}
	send {
		|msg, delayTime, retry, id=0|
		if(retry>0,{
			this.delaySending(retry, delayTime, msg);
		},{
			this.sendNow(msg, id);
		});
	}
	sendNow {
		|msg, id=0|
		if(id == -1,{
			id = (0..24);
		});
		if(id.isArray,{
			{
				id.do{
					|i|
					var array = Int8Array(msg.size); // For OSC
					msg.overWrite(macAddresses[i].at((0..5)));
					array.addAll(msg);
					netAddr.sendMsg(\test, i, array);
					if(macWithSerial[i][1]!=nil, {
						// "Send: ".post; msg.postln;
						macWithSerial[i][1].putAll(msg);
					}, {
						// "No SerialPort opened".error;
					});
					msgSpacingTime.wait; // Wait
				};
			}.fork;
		}, {
			var array = Int8Array(msg.size); // For OSC
			msg.overWrite(macAddresses[id].at((0..5)));
			array.addAll(msg);
			netAddr.sendMsg(\test, id, array);
			if(macWithSerial[id][1]!=nil, {
				// "Send: ".post; msg.postln;
				macWithSerial[id][1].putAll(msg);
			}, {
				// "No SerialPort opened".error;
			});

		});
	}
	delaySending {
		|retry, delayTime, msgArray|
		// Re-send the msg with new delayTime. If delayTime = 1000, retry = 2, send [now, delayTime: 1000], [0.5.wait. delayTime: 500];
		retry.do{
			|i|
			var newDelayTime = delayTime - ((delayTime / retry)*i);
			newDelayTime = newDelayTime - 100.rand;
			// newDelayTime.postln;
			if(i > 0, {
				// "Send".postln;
				var msgNew = msgArray;
				msgNew[2] = newDelayTime.shortToCharArray[0];
				msgNew[3] = newDelayTime.shortToCharArray[1];
				{(newDelayTime/1000).wait; this.sendNow(msgNew)}.fork;
			}, {
				this.sendNow(msgArray);
			});
		};
	}
	setVal{
		|id=0, value=127, ledId=2, delayTime=0, retry=0|
		var numMsgs = 1;
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 1, ledId, value]; // 1: msg type, 2: both leds
		msg = addr ++ [numMsgs] ++ [msg.size] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end; // num, size, msg <-- Can be '2, 2, a, b, 3, a, b, c', will be parsed as two msgs
		msg.postln;
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
	setValLag{
		|id=0, value=127, lagTime=1000, ledId=2, delayTime=0, retry=0| // lagTime as short, two bytes
		// netAddr.sendMsg("/", id, 1, value, lagTime.shortToCharArray[0], lagTime.shortToCharArray[1]);
		var numMsgs = 1;
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 2, ledId, value, lagTime.shortToCharArray[0], lagTime.shortToCharArray[1]]; // 2: msg type, 2: both leds
		msg = addr ++ [numMsgs] ++ [msg.size] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end; // num, size, msg <-- Can be '2, 2, a, b, 3, a, b, c', will be parsed as two msgs
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
	doEnv{
		|id=0, value, times=#[1000, 1000, 1000], ledId=2, delayTime=0, retry=0, comment="mode 0: BL 0, mode 1: BL 1, mode 2: both"|
		// netAddr.sendMsg("/", id, 2, values[0], values[1], values[2], values[3], times[0], times[1], times[2]);
				var numMsgs = 1;
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 3,
			times = times.asInteger;
			ledId,
			times[0].shortToCharArray[0],
			times[0].shortToCharArray[1],
			times[1].shortToCharArray[0],
			times[1].shortToCharArray[1],
			times[2].shortToCharArray[0],
			times[2].shortToCharArray[1],
			value
		]; // 1: msg type, 2: both leds
		msg = addr ++ [numMsgs] ++ [msg.size] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end; // num, size, msg <-- Can be '2, 2, a, b, 3, a, b, c', will be parsed as two msgs
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
/*	fillRect{ // MSG_LCD_FILLRECT_SHOW
		|id=0, x=0, y=0, w=255, h=255, r=255, g=255, b=255, bClear = true, delayTime=0, retry=0|
		var type = 7;
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, type, x.asInt, y.asInt, w.asInt, h.asInt, r, g, b];
		if(bClear, {
			var clearMsg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 4, 0, 0, 255, 255, 0, 0, 0]; // Black rect w/out show
			msg = addr ++ [2] ++ [clearMsg.size+1] ++ clearMsg ++ [msg.size+1] ++ msg ++  [addr.size + msg.size + clearMsg.size + end.size + 4] ++ end;
		}, {
			msg = addr ++ [1] ++ [msg.size+1] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end;
		});

		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}*/
/*	fillRectClear{ // Use MSG_LCD_FILLRECT_SHOW w/ bClear arg
		|id=0, x=0, y=0, w=0, h=0, r=255, g=255, b=255, delayTime=0, retry=0, dontusethis=0|
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 7, x.asInt, y.asInt, w.asInt, h.asInt, r, g, b];
		var clearMsg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, 4, 0, 0, 255, 255, 0, 0, 0];
		msg = addr ++ [2] ++ [clearMsg.size+1] ++ clearMsg ++ [msg.size+1] ++ msg ++  [addr.size + msg.size + clearMsg.size + end.size + 4] ++ end;
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}*/
	fillRect{ // 	MSG_LCD_FILLRECT_ABS
		|id=0, x=0, y=0, w=0, h=0, r=255, g=255, b=255, bClear = true, bShow = true, bRedraw = false, delayTime=0, retry=0|
		var type = 10;
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, type,
			x.shortToCharArray[0],
			x.shortToCharArray[1],
			y.shortToCharArray[0],
			y.shortToCharArray.[1],
			w.shortToCharArray[0],
			w.	shortToCharArray[1],
			h.shortToCharArray[0],
			h.shortToCharArray[1],
			r, g, b, bClear.asInteger, bShow.asInteger, bRedraw.asInteger];

		msg = addr ++ [1] ++ [msg.size+1] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end;

		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
	setTranslation{ // MSG_SET_TRANSLATION
		|id=0, x=0, y=0, delayTime=0, retry=0|
		var type = 11;
		var msg = [delayTime.shortToCharArray[0], delayTime.shortToCharArray[1], msgId, type,
			x.shortToCharArray[0],
			x.shortToCharArray[1],
			y.shortToCharArray[0],
			y.shortToCharArray.[1]
		];
		msg = addr ++ [1] ++ [msg.size+1] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end;
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}

	setOTA{
		|id=0|
		if(id.isArray == false, { // You don't want all LCD's to go into OTA-mode as once...
			var msg = [0, 0, msgId, 5];
			msg = addr ++ [1] ++ [msg.size] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end;
			this.send(msg, 0, 0, id);
			this.msgIncrease();
		});
	}
	setOTAServer{
		|id=0|
		if(id.isArray == false, { // You don't want all LCD's to go into OTA-mode as once...
			var msg = [0, 0, msgId, 6];
			msg = addr ++ [1] ++ [msg.size] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end;
			this.send(msg, 0, 0, id);
			this.msgIncrease();
		});
	}
	setVisualizerMode {
		|m=0|
		oscSender.sendMsg("/setMode", m);
	}
	updateValuesFromVisualizer{
		|lagTime=200|
		{
		visualizerBacklightValues.do{
			|v, i|
			this.setValLag(i, v[0], lagTime);
				if(macWithSerial[i][1] != nil, {
					0.006.wait;
				})
				// this.setVal(i, v[1], 1);
		};
		}.fork;
	}
	eventRect{
		|id=0, x=0, y=0, w=255, h=255, r=255, g=255, b=255, lifeTime=1000, speedX=1, speedY=1, delayTime=0, retry=0|
		var msg = [
			delayTime.shortToCharArray[0], delayTime.shortToCharArray[1],
			msgId, 8,
			x.asInteger, y.asInteger,
			w.asInteger, h.asInteger,
			r, g, b,
			lifeTime.shortToCharArray[0], lifeTime.shortToCharArray[1],
			speedX.shortToCharArray[0], speedX.shortToCharArray[1],
			speedY.shortToCharArray[0], speedY.shortToCharArray[1],
		];
		msg = addr ++ [1] ++ [msg.size+1] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end;
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
	eventExpRand{
		|id=0, x=0, y=0, w=255, h=255, r=255, g=255, b=255, lifeTime=1000, delayTimeEvent=100, sizeRange=50, delayTime=0, retry=0|
		var type = 9;
		var msg = [
			delayTime.shortToCharArray[0], delayTime.shortToCharArray[1],
			msgId, type,
			x.asInteger, y.asInteger,
			w.asInteger, h.asInteger,
			r, g, b,
			lifeTime.shortToCharArray[0], lifeTime.shortToCharArray[1],
			delayTimeEvent.shortToCharArray[0], delayTimeEvent.shortToCharArray[1],
			sizeRange
		];
		msg = addr ++ [1] ++ [msg.size+1] ++ msg ++ [addr.size + msg.size + end.size + 3] ++ end;
		this.send(msg, delayTime, retry, id);
		this.msgIncrease();
	}
	stream {
		|delayTime=180|
		if(updateFromVisualizer != nil, {
			this.stopStream();
		});
		updateFromVisualizer = {
			inf.do{
				this.updateValuesFromVisualizer(delayTime);
				(delayTime/1000).wait;
			}
		}.fork;
	}
	stopStream{
		updateFromVisualizer.stop;
	}
}

LCDSong{
	var <>name;
	var <>startFunc;
	var <>stopFunc;
	var <>guiWindow;
	var <>duration = 60;
	var <>waitTimeAfter = 0;
	var <>guiItems;
	*new { |name, start, stop, guiWindow, duration, waitTimeAfter, guiItems|
        ^super.new.init(name, start, stop, guiWindow, duration, waitTimeAfter, guiItems);
    }
	init {
		|name, start, stop, guiWindow, duration, waitTimeAfter, guiItems|
		this.name = name;
		this.startFunc = start;
		this.stopFunc = stop;
		this.guiWindow = guiWindow;
		this.duration = duration;
		this.waitTimeAfter = waitTimeAfter;
		this.guiItems = guiItems;
		guiWindow.view.keyDownAction = {
			arg view, char, modifiers, unicode, keycode;
			// [char, keycode].postln;
			if(keycode == 1, {
				var file;
				file = File.open("/Users/jildertviet/Desktop/GNP/SC/settings/" ++ name ++ ".txt", "w");
				guiItems.do{ |e|
					file.write(e.value.asString ++ "\n");
				};
				file.close();
			});
		};
	}
	loadSettings{
		var file, lines;
		file = File.open("/Users/jildertviet/Desktop/GNP/SC/settings/" ++ name ++ ".txt", "r");
		if(file.isOpen == false, {("No file: " ++ this.name).postln;^false});
		"Reading the file: ".postln;
		lines = file.readAllString.split($\n);
		// lines.removeAt(lines.size-1);
		guiItems.size.do{
			|i|
			guiItems[i].valueAction = lines[i].asFloat;
		};
	}
	start{
		startFunc.value();
	}
	stop{
		stopFunc.value();
	}
	showGui{
		guiWindow.front();
	}
	hideGui{
		guiWindow.visible = false;
	}
}

+ SimpleNumber{
	shortToCharArray{
		^[this%256, (this/256).asInteger];
	}
}