JDrumSamples {
	var <>buffers;
  classvar <>dir = "/home/jildert/Music/JildertMusic/Samples/Drums/Drum Samples/Drum Machines/";
	*new { | a |
        ^super.new.init(a);
    }
	init{

	}
	load {

	}
	loadByFolderName{
		|fName=""|
		var path = dir;
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
    var d = dir;
		var folder = PathName(d).entries.choose;
		this.buffers = List.new();
		if(folder.isFolder, {
			folder.entries.do{
				|e|
				var bTemp = Buffer.readChannel(Server.local, e.fullPath, channels: 1);
				e.postln;
				buffers.add(bTemp);
			}
		});
  }
  /*
  If folder get files inside, if another folder: also check
  USE .deepFiles instead!!!
  */
  dirToSamplePaths {
    |p|
    var r = List.newClear(0);
    var hasFolders = true;
    PathName(p).entries.do{
      |e|
      // e.postln;
      r.add(e.fullPath);
    };
    while({hasFolders == true}, {
      hasFolders = false;
      r.do{|e,i|
        // e.postln;
        if(e.isFolder, {
          hasFolders = true;
          r.removeAt(i);
          PathName(e).entries.do{|f|
            r.add(f.fullPath);
          }
        // Delete entrie, add children. 
        });
      }
    })
    ^r;
  }
  * loadRandomSamples {
    |num|
    var paths = PathName(dir).deepFiles; 
    // var r = Array.fill(num, {paths.choose});
    var r = Array.fill(num, {Buffer.read(Server.local,paths.choose.fullPath);});
    ^r
  }
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

