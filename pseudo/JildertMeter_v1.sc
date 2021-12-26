JildertMeter_v1{
	var <>recTrigger = 0;
	// BufWr / BufRd

	*kr {|name="JildertMeter", in, window, pos = #[0,0], netAddr=nil, id=0, buffer=0, noteA=0, lagTime=0, frameRate=25, nextTo=nil|
		var out, osc, send;
		var meter, title, slopeMeter, button, thresholdMeter, thresholdSlopeMeter;
		var slope;
		var width, height;
		var openGuiWindow;
		var guiWindow, recordButton, noteTxtField, hideButton;
		var prevValue;
		var recBuf, recTrigger;
		var test;
		var note = noteA;

		prevValue = 0;
		width = 30;
		height = 160;

		"JildertMeter made".postln;
		// If nextTo is not nil, numChannels will be 1, so position will be relative
		// if(nextTo.numChannels==1, {pos = nextTo.get(\pos)});

		// Make window larger if xPos is too far
		if((pos[0]+width)>window.bounds.width, {
			var newWidth = pos[0] + (width*2);
			// "Resize window".postln;
			window.setInnerExtent((newWidth),window.bounds.height);
			}
		);

		if(lagTime!==0, {in = Lag.kr(in, lagTime);}); // Only apply lag if lagTime>0
		slope = Slope.kr(in);
		slope = slope.abs;

		// buffer = Buffer.new(Server.local, 4*Server.local.sampleRate, bufnum: id);

		out=in;

		// Display meter + name
		meter = Slider(window, Rect(pos[0], pos[1], width*(0.5*0.75), height))
		.thumbSize_(1).value_(0.5).knobColor_(Color.white).background_(Color.black).orientation_( \vertical).canFocus_(false);
//--
		slopeMeter = Slider(window, Rect(pos[0]+(width*(0.5)), pos[1], width*(0.5*0.75), height))
		.thumbSize_(1).value_(0.5).knobColor_(Color.white).background_(Color.black).orientation_( \vertical).canFocus_(false).visible_(false);
//--
		thresholdMeter = Slider(window, meter.bounds.moveBy(meter.bounds.width, 0).width_(width*(0.5*0.25)))
		.thumbSize_(1).value_(1).knobColor_(Color.red).background_(Color.black).orientation_( \vertical).canFocus_(false);
//--
		thresholdSlopeMeter = Slider(window, slopeMeter.bounds.moveBy(slopeMeter.bounds.width, 0).width_(width*(0.5*0.25)))
		.thumbSize_(1).value_(1).knobColor_(Color.red).background_(Color.black).orientation_( \vertical).canFocus_(false);
//--
		title = StaticText(window, meter.bounds.moveBy(0, meter.bounds.height+3).width_(height));
		title.string = name;
		title.align = \topLeft;
		title.bounds.postln;
//--

		// Own GUI window
		guiWindow = Window(name+" Gui", window.bounds-Rect(-20,20,0,0)).front.background_(Color.gray(1,0.9)).visible_(false);
		guiWindow.userCanClose_(false);

		hideButton = Button(guiWindow, Rect(guiWindow.bounds.width-22, 2, 20, 20)).states_([["X", Color.red]]).action_({guiWindow.visible_(false)});

		recordButton = Button(guiWindow, Rect(10,10,50,20)).states_([["Record", Color.black, Color.white], ["Record", Color.black, Color.red]]).action_({
			// recTrigger.fill(1);
			var wait;

			"Record...".postln;
			{ RecordBuf.kr(Lag.kr(In.kr(id), 0.1).round(0.125), buffer, loop: 0, doneAction: 2);
			}.play;
			wait = Routine({
				var array = Array.fill(9, {List.new;};);
				var prevVal;
				buffer.get(0, {|val|
					var arrayIndex = (val*8);
					prevVal = val;
					array[arrayIndex].add(0);
				}); // Set prevVal to the beginning of buffer
				0.1.yield;


				((buffer.numFrames*64)/Server.local.sampleRate).yield;
				"Recording finished".postln;
				recordButton.value = 0;
				buffer.plot;

				buffer.numFrames.do{|i|
					// array = array.add(1);
					buffer.get(i, {|val|
						// val.postln;
						if((prevVal!==val)&&(prevVal<val), {
							var arrayIndex = (val*8);
							// "Time : ".post; i.post; " ".post; "ArrayIndex: ".post; arrayIndex.postln;
							array[arrayIndex] = array[arrayIndex].add(i);
						});
						prevVal = val;
					});

				};

				2.yield;
				array.size.do({|i|
					if(array[i].size>1, {
					((array[i].size)-1).do({|j|
							var a, b;
							a = array[i][j+1];
							b = array[i][j];
							// a.post; " ".post; b.postln;
							array[i][j] = (array[i][j+1])-(array[i][j]);
					});
						array[i].removeAt((array[i].size)-1);
					});

				});
				2.yield;
				"array: ".post; array.postln;

				// Buffer
			});
			AppClock.play(wait);
			}); // Recordbutton

		noteTxtField = TextField(guiWindow, recordButton.bounds.moveBy(0,recordButton.bounds.height+5).width_(80));
		noteTxtField.string = note;
		noteTxtField.action = {arg field; note = field.value;};

		// recBuf = RecordBuf.kr(in, buffer, loop: 0, run: recTrigger);

		// test = SinOsc.ar(1);

		button = Button(window, meter.bounds.moveBy(0,meter.bounds.height+18).height_(18).width_(28)).states_([
            ["GUI", Color.black, Color.white],
		        ]).canFocus_(false).action_({guiWindow.visible_(true); guiWindow.front;});

		// Set value to meter
		send = SendTrig.kr(Impulse.kr(frameRate),[0,1]+pos[0],[in, slope]);
		osc = OSCFunc({ |msg|
			// msg.postln;
			{
			if(window.isClosed==false, {
			var value;
			value = msg[3];
				// value.postln;
				switch(msg[2], // Set meter
						(0+pos[0]), {
							if((value>thresholdMeter.value) && (prevValue<thresholdMeter.value) && (thresholdMeter.value<1), {
								if(netAddr!==nil, {
									netAddr.sendMsg("/JildertMeter", note.asInt);
									note.post; " send OSC msg".postln;
								});
							});
						prevValue = value;
						meter.value_(value);
					},
						(1+pos[0]), {slopeMeter.value_(value)}
				);

			});
			}.defer;
		},'/tr');

		^out
	}
}

JildertRhythmFinder{
	*new{
		^super.new.init;
	}
	init{

	}
}