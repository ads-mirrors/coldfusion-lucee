<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"  {

	function run( testResults , testBox ) {
		describe( "test case for getTempDirectory()", function() {

			it (title = "getTempDirectory()", body = function( currentSpec ) {
				var tmp = getTempDirectory();
				var tmp2 = getTempDirectory();

				expect( tmp ).toBe( tmp2 );
				expect( DirectoryExists( tmp ) ).toBeTrue();
				expect( right( tmp, 1 ) ).toBe( server.separator.file );
			});

			it (title = "getTempDirectory(false)", body = function( currentSpec ) {
				var tmp = getTempDirectory( false );
				var tmp2 = getTempDirectory( "false" );

				expect( tmp ).toBe( tmp2 );
				expect( DirectoryExists( tmp ) ).toBeTrue();
			});

			it (title = "getTempDirectory(unique=true)", body = function( currentSpec ) {
				var tmp = getTempDirectory();
				var tmp2 = getTempDirectory(unique=true);
				var tmp3 = getTempDirectory(unique=true);

				expect( tmp ).notToBe( tmp2 );
				expect( tmp2 ).notToBe( tmp3 );

				expect( DirectoryExists( tmp2 ) ).toBeTrue();
				expect( DirectoryExists( tmp3 ) ).toBeTrue();
			});

			it (title = "getTempDirectory(true)", body = function( currentSpec ) {
				var tmp = getTempDirectory( false );
				var tmp2 = getTempDirectory( true );
				var tmp3 = getTempDirectory( "true" );

				expect( tmp ).notToBe( tmp2 );
				expect( tmp2 ).notToBe( tmp3 );
				expect( DirectoryExists( tmp2 ) ).toBeTrue();
				expect( DirectoryExists( tmp3 ) ).toBeTrue();
			});

			it (title = "getTempDirectory('ldev')", body = function( currentSpec ) {
				var prefix = "ldev";
				var tmp = getTempDirectory( false );
				var tmp2 = getTempDirectory( prefix );
				var tmp3 = getTempDirectory( prefix );

				expect( tmp ).notToBe( tmp2 );
				expect( tmp2 ).notToBe( tmp3 );

				expect( DirectoryExists( tmp2 ) ).toBeTrue();
				expect( DirectoryExists( tmp3 ) ).toBeTrue();

				expect( tmp2 ).toInclude( tmp & prefix );
				expect( tmp3 ).toInclude( tmp & prefix );

				expect( right( tmp2, 1 ) ).toBe( server.separator.file );
				expect( right( tmp3, 1 ) ).toBe( server.separator.file );
			});

		});
	}

}
</cfscript>