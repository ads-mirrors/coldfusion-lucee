 component extends="org.lucee.cfml.test.LuceeTestCase" {

	public function beforeAll() {		
		variables.SEP = Server.separator.file;
		variables.path = getTempDirectory() & "ldev1247";
		if ( directoryExists( path ) )
			directoryDelete( path, true );
		var path2 = path & "#SEP#a";
		directoryCreate( path2 );
		cffile( fixnewline=false, output="aaa", file="#path##SEP#b.txt", addnewline=true, action="write" );
		cffile( fixnewline=false, output="aaa", file="#path2##SEP#c.txt", addnewline=true, action="write" );
	}

	public function afterAll() {
		directoryDelete( path, true );
		structDelete( request, "ldev1427" );
	}

	function run( testResults , testBox ) {
		describe( "test case for DirectoryList() filter", function() {
			it(title = "check directoryList filter has arguments (path, type, ext)", body = function( currentSpec ) {
				var request.ldev1247 = {};
				function filter( path, type, ext ) {
					request.ldev1247[ arguments.path ] = duplicate( arguments );
					return true;
				}
				var dir = directoryList( path, true, "query", filter );
				expect( dir.recordcount ).toBe( 3 );
				expect( structCount( request.ldev1247 ) ).toBe( 3 );
				loop query="dir" {
					var p = dir.directory & sep & dir.name;
					expect ( request.ldev1247 ).toHaveKey( p );
					var pp = request.ldev1247[ p ];
					if ( pp.type eq "directory" ){
						expect( "dir" ).toBe( dir.type ); // note: type query has "dir" instead of "directory"
						expect( pp.ext ).toBe( "" );
					} else {
						expect( pp.type ).toBe( dir.type );
						expect( pp.ext ).toBe( listLast( dir.name, "." ) );
					}
				}
				structDelete( request, "ldev1427" );
			});

		});
	}

}