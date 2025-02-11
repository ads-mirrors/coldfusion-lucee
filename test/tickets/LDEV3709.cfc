component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "test case for LDEV-3709", function() {
			
			it (title = "getTempFile should default to temp dir (positional)", body = function( currentSpec ) {
				var tmp = getTempFile( "", "lucee-getTempFile" );
				expect( listLast( tmp, "/\" ) ).toInclude( "lucee-getTempFile" );
				expect( listLast( listLast( tmp, "/\" ), ". ") ).toBe( "tmp" );
				expect( tmp ).toInclude( getTempDirectory() );

				var txt = getTempFile( "", "lucee-getTempFile-ext", ".txt" );
				expect( listLast( txt, "/\" ) ).toInclude( "lucee-getTempFile-ext" );
				expect( listLast( listLast( txt, "/\" ), ". ") ).toBe( "txt" );
				expect( txt ).toInclude( getTempDirectory() );
			
			});

			it (title = "getTempFile should default to temp dir (named)", body = function( currentSpec ) {
				var tmp = getTempFile( dir="", prefix="lucee-getTempFile" );
				expect( listLast( tmp, "/\" ) ).toInclude( "lucee-getTempFile" );
				expect( listLast( listLast( tmp, "/\" ), ". ") ).toBe( "tmp" );
				expect( tmp ).toInclude( getTempDirectory() );

				var txt = getTempFile( dir="", prefix="lucee-getTempFile-ext", ext=".txt" );
				expect( listLast( txt, "/\" ) ).toInclude( "lucee-getTempFile-ext" );
				expect( listLast( listLast( txt, "/\" ), ". ") ).toBe( "txt" );
				expect( txt ).toInclude( getTempDirectory() );
			});

			it (title = "getTempFile should default to temp dir (named without)", body = function( currentSpec ) {
				var tmp = getTempFile( prefix="lucee-getTempFile" );
				expect( listLast( tmp, "/\" ) ).toInclude( "lucee-getTempFile" );
				expect( listLast( listLast( tmp, "/\" ), ". ") ).toBe( "tmp" );
				expect( tmp ).toInclude( getTempDirectory() );

				var txt = getTempFile( prefix="lucee-getTempFile-ext", ext=".txt" );
				expect( listLast( txt, "/\" ) ).toInclude( "lucee-getTempFile-ext" );
				expect( listLast( listLast( txt, "/\" ), ". ") ).toBe( "txt" );
				expect( txt ).toInclude( getTempDirectory() );
			});
		});
	}
}