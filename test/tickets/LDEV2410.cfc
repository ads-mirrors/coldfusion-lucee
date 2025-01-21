component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.testfile = getTempFile( getTempDirectory(), "LDEV-2410", ".txt" );
	}

	function run( testResults, testBox ){
		describe( "test case for LDEV-2410", function() {

			it(title = "checking the file with READONLY Attribute", body = function( currentSpec ) {
				if (!isWindows()) return;
				FileWrite( testfile, "I am a writeable  file" );

				FileSetAttribute( testfile, 'readonly' );
				var info = getFileInfo( testfile );
				expect( info.canRead ).toBe( true );
				expect( info.canWrite ).toBe( false );
				expect (function(){
					FileWrite( testfile, "I am in readonly file" );
				}).toThrow();
			});

			it(title = "checking the file with NORMAL Attribute", body = function( currentSpec ) {
				if (!isWindows()) return;
				FileSetAttribute( testfile, 'normal' );
				var info = getFileInfo( testfile );
				expect( info.canWrite ).toBe( true );
				FileWrite( testfile, "I am in normal (writable) file" );
			});

			it(title = "checking settting a file to readOnly on linux", body = function( currentSpec ) {
				if (isWindows()) return;
				
				var info = getFileInfo( testfile );
				expect( info.canWrite ).toBe( true );
				FileWrite( testfile, "I am in normal (writable) file" );

				FileSetAccessMode( testfile, "444" ); // i.e. readonly
				info = getFileInfo( testfile );
				expect( info.canWrite ).toBe( false);
				expect (function(){
					FileWrite( testfile, "I am in readonly file" );
				}).toThrow();

				FileSetAccessMode( testfile, "644" );
				info = getFileInfo( testfile );
				expect( info.canWrite ).toBe( true );
				expect (function(){
					FileWrite( testfile, "I am in normal (writable) file" );
				}).notToThrow();

			});
		});
	}

	function afterAll(){
		if ( FileExists( variables.testfile ) ) {
			FileDelete( variables.testfile );
		}
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}

}