component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.SEP = Server.separator.file;
		variables.parent = getTempDirectory() & "directoryRename" & sep;
		if (directoryExists(parent))
			directoryDelete(parent,true);
		directoryCreate(parent);
	}

	function afterAll(){
		directorydelete(parent,true);
	}

	function run( testResults , testBox ) {
		describe( "test case for directoryRename", function() {
			it(title = "Checking with directoryRename", body = function( currentSpec ) {
				cflock(name="testdirectoryDelete" timeout="1" throwontimeout="no" type="exclusive"){
					var dir=parent&createUUID();
					var nn="DirectoryRename-"&createUUID();
					var dir2=parent&nn;
					directoryCreate(dir);
					var newPath = directoryRename(dir,dir2);
					assertEquals(nn, listLast(newpath, "/\"));
					assertEquals(replaceNoCase(dir2,"\", "/","all"), replaceNoCase(newPath,"\", "/","all"));
					assertEquals(true, directoryExists(newPath));
					assertEquals(false, isEmpty(newPath));
					assertEquals("#false#", "#directoryExists(dir)#");
					assertEquals("#true#", "#directoryExists(dir2)#");

					directorydelete(dir2,true);
					directoryCreate(dir);
					directoryRename(dir,nn);
					assertEquals("#false#", "#directoryExists(dir)#");
					assertEquals("#true#", "#directoryExists(dir2)#");
				}
			});
		});
	}
}