JSong {
	var <>functions;
	var <>guiWindow;
	*new { |a|
		^super.new.init();
	}
	init{
		"JSong init".postln;
		functions = Array.newClear(16);
	}
	gui{
		var names = ["f12","f13","f14","f15","f8","f9","f10","f11","f4","f5","f6","f7","f0","f1","f2","f3"];
		guiWindow = Window.new("", 370).front;
		//change the gaps and margins to see how they work
		// guiWindow.view.decorator = FlowLayout( guiWindow.view.bounds, 10@10, 10@10 );
		16.do{ |i|
			Button.new(guiWindow, Rect((i%4) * 90, (3-(i/4).asInteger) * 90, 80, 80).moveBy(10,10))
			.states_([[i, Color.black, Color.white]])
			.action_({this.doFunc(i)});
		};
	}
	f0{}
	f1{}
	f2{}
	f3{}
	f4{}
	f5{}
	f6{}
	f7{}
	f8{}
	f9{}
	f10{}
	f11{}
	f12{}
	f13{}
	f14{}
	f15{}
	doFunc{
		|i=0|
		switch(i,
			0, {this.f0},
			1, {this.f1},
			2, {this.f2},
			3, {this.f3},
			4, {this.f4},
			5, {this.f5},
			6, {this.f6},
			7, {this.f7},
			8, {this.f8},
			9, {this.f9},
			10, {this.f10},
			11, {this.f11},
			12, {this.f12},
			13, {this.f13},
			14, {this.f14},
			15, {this.f15},
		);
	}
}


// 16.do{|i| (3-(i/4).asInteger).postln}