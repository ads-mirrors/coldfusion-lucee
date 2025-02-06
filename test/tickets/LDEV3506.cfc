component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.SEP = Server.separator.file;
		variables.dir = getTempDirectory() & "ldev3506" & sep;
		if (directoryExists( dir ))
			directoryDelete( dir, true );
		directoryCreate( dir );
		variables.testPaths = [ "ralio", "lucee" ];
	}
	function afterAll(){
		directoryDelete( dir, true );
	}
	function run( testResults , testBox ) {

		describe( "LDEV-3506 test case for directoryCreate mode with createPath", function() {
			it(title = "Checking mode directoryCreate()", skip=isWindows(), body = function( currentSpec ) {
				var testDir = dir & "mode";
				directoryCreate( path=testDir, mode="644", createPath=true );

				test( testDir, "644", "644" );
			});

			it(title = "Checking default mode directoryCreate()", skip=isWindows(), body = function( currentSpec ) {
				var testDir = dir & "mode-default";
				directoryCreate( path=testDir, createPath=true );

				test( testDir, "777", "777" );
			});

			// no mode, so this passes on windows too
			it(title = "Checking preserve mode -1 directoryCreate()", body = function( currentSpec ) {
				var testDir = dir & "mode-1";
				directoryCreate( path=testDir, mode="-1", createPath=true );

				var parentDirMode = directoryInfo( testDir ).mode ?: "";
				test( testDir, -1, parentDirMode );
			});
		});

	}

	private function test( testDir, createMode, expectedMode="" ){
		var testPath = arguments.testDir & "/";
		ArrayEach( variables.testPaths, function( item ){
			testpath &= arguments.item & "/";
		});

		directoryCreate( path=testPath, createPath=true, mode=arguments.createMode );
		expect( directoryExists( testDir ) ).toBeTrue();
		expect( directoryInfo( testDir ).mode ?: "" ).toBe( arguments.expectedMode );
		testPath = arguments.testDir & "/";

		var _expectedMode = expectedMode;
		ArrayEach( variables.testPaths, function( item ){
			testpath &= arguments.item & "/";
			expect( directoryInfo( testPath ).mode ?: "" ).toBe( _expectedMode, testPath );
		});

	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}