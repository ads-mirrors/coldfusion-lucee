component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.SEP = Server.separator.file;
		variables.dir = getTempDirectory() & "directoryCreate" & sep;
		if (directoryExists(dir))
			directoryDelete(dir,true);
		directoryCreate(dir);
	}
	function afterAll(){
		directorydelete(dir,true);
	}	
	function run( testResults , testBox ) {
		describe( "test case for directoryCreate", function() {
			it(title = "Checking with directoryCreate()", body = function( currentSpec ) {
				cflock(name="testdirectoryCreate" timeout="1" throwontimeout="no" type="exclusive"){
					_dir=dir&createUUID();
					assertEquals("#false#", "#DirectoryExists(_dir)#");
					directoryCreate(_dir);
					assertEquals("#true#", "#DirectoryExists(_dir)#");

					try{
						directoryCreate(_dir);
						fail("must throw:The specified directory ... could not be created.");
					}catch(Any e){}
					directorydelete(_dir);

					dir2=_dir&"/a/b/c/";
					assertEquals("#false#", "#DirectoryExists(dir2)#");
					directoryCreate(dir2);
					assertEquals("#true#", "#DirectoryExists(dir2)#");
					directorydelete(_dir,true);
				}

			<!--- end old test code --->
			});
		});

		describe( "test case for directoryCreate mode", function() {
			it(title = "Checking mode directoryCreate()", skip=isWindows(), body = function( currentSpec ) {
				var testDir = dir & "mode";
				directoryCreate(path=testDir, mode="775");

				expect( directoryExists( testDir ) ).toBeTrue();
				expect( directoryInfo( testDir ).mode ).toBe("775");
			});

			it(title = "Checking default mode directoryCreate()", skip=isWindows(), body = function( currentSpec ) {
				var testDir = dir & "mode-default";
				directoryCreate(path=testDir);
				
				expect( directoryExists( testDir ) ).toBeTrue();
				expect( directoryInfo( testDir ).mode ).toBe("777");
			});

			it(title = "Checking preserve mode -1 directoryCreate()", skip=isWindows(), body = function( currentSpec ) {
				var testDir = dir & "mode-1";
				directoryCreate(path=testDir, mode="-1");
				
				expect( directoryExists( testDir ) ).toBeTrue();
				var parentDirMode = directoryInfo( getTempDirectory() ).mode;
				expect( directoryInfo( testDir ).mode ).toBe( parentDirMode );
			});
		});

	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}