JShaderS{
  var <> child;
  *new{
    ^super.new.init();
  }	
  *kr { |
		id=0,
		loc=#[0,0,0],
		size=#[100, 100, 0],
		direction=#[1,0,0],
		rotation=#[0,0,0],
		speed=1,
		lineWidth=1,
		bMove=0,
		layer=2,
		rgba=#[255,255,255,1.0],
		bFilled=1,
		mode=0,
    numBusses=10,
		busses=#[
      0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,0,0,0,0,0,0
    ]
		|
		if(busses.size < numBusses, {
      busses = (busses ++ [0]).clipExtend(numBusses)
    });
		^JEventDynamicS.kr(
		id,
		loc,
		size,
		direction,
		rotation,
		speed,
		lineWidth,
		bMove,
		layer,
		rgba,
		bFilled,
		mode,
    numBusses: numBusses,
    busses:	([8] ++ busses.at((1..(numBusses-1)))),
		);
  } 
  init{
    
  }
}
