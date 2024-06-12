+ String{
  dirToBuffers{
    |numChannels = 1|
    // var path = "/home/jildert/Music/JildertMusic/Samples/Drums/Drum Samples/Drum Machines/";
		var folder;
		var buffers = List.new();
		// this = this ++ fName ++ "/";
		folder = PathName.new(this);
		if(folder.isFolder, {
			folder.entries.do{
				|e|
				var bTemp = Buffer.readChannel(Server.local, e.fullPath, channels: numChannels);
				// e.postln;
				buffers.add(bTemp);
			}
		});
    ^buffers;
  }
}
