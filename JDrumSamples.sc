JDrumSamples {
	var <>buffers;
	*new { | a |
        ^super.new.init(a);
    }
	init{

	}
	load {

	}
	loadByFolderName{
		|fName=""|
		var path = "/home/jildert/Music/JildertMusic/Samples/Drums/Drum Samples/Drum Machines/";
		var folder;
		this.buffers = List.new();
		path = path ++ fName ++ "/";
		folder = PathName.new(path);
		if(folder.isFolder, {
			folder.entries.do{
				|e|
				var bTemp = Buffer.readChannel(Server.local, e.fullPath, channels: 1);
				e.postln;
				buffers.add(bTemp);
			}
		});
	}
	loadRandom {
		var i = 0;
		var d = PathName.new("/home/jildert/Music/JildertMusic/Samples/Drums/Drum Samples/Drum Machines/");
		var folder = d.entries.choose;
		this.buffers = List.new();
		if(folder.isFolder, {
			folder.entries.do{
				|e|
				var bTemp = Buffer.readChannel(Server.local, e.fullPath, channels: 1);
				e.postln;
				buffers.add(bTemp);
			}
		});
/*		d.entries.do{
			|e|
			if(e.isFolder, {
				var subDir = PathName.new(e.fullPath);
				subDir.entries.do{
					|f|
					if(f.fileName.contains("Hat") || f.fileName.contains("hat"), {
						// Add the file
						var bTemp = Buffer.readChannel(s, f.fullPath, channels: 1);
						buffers.add(bTemp);
					})
					// f.fileName.postln;
				}
				// e.fullPath.postln;
			});
		};*/
	}
}