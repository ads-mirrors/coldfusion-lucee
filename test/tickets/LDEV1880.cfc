component extends="org.lucee.cfml.test.LuceeTestCase"{
	function isNotSupported() {
		var isWindows =find("Windows",server.os.name);
		if(isWindows > 0 ) return false;
		else return  true;
	}

	function beforeAll(){
		variables.base = getTempDirectory();
		variables.path = base&"LDEV1880\example.txt";
		if(!directoryExists(base&"LDEV1880")){
			directoryCreate(base&'LDEV1880');
		}
	}
	function afterAll(){
		if(directoryExists(base&"LDEV1880")){
			var files = directoryList(path=variables.path, type="file");
			arrayEach( files, function( path ) {
				fileSetAttribute( path, 'Normal' );
			});
			directoryDelete(base&"LDEV1880",true);
		}
	}
	function run( testResults , testBox ) {
		describe( title="test suite for fileSetAttribute()", skip=isNotSupported(),  body = function() {

			beforeEach( function( currentSpec ) {
				if(fileExists(path)){
					fileSetAttribute(path,'Normal');
					fileDelete(path);
				}
				fileWrite(path,"This is a sample file content");
			});

			it(title = "checking the file with Archive Attribute", body = function( currentSpec ) {
				fileSetAttribute(path,'Archive');
				expect( getFileInfo(path).isArchive ).toBe('true');
			});

			it(title = "checking the file with System Attribute", body = function( currentSpec ) {
				fileSetAttribute(path,'System');
				expect( getFileInfo(path).isSystem ).toBe('true');
			});

			it(title = "checking the file with readOnly Attribute", body = function( currentSpec ) {
				fileSetAttribute(path,'readOnly');
				var info = getFileInfo( path );
				expect( info.canRead ).toBe('true');
				expect( info.canWrite ).toBe('false');
			});

			it(title = "checking the file with Hidden Attribute", body = function( currentSpec ) {
				fileSetAttribute(path,'Hidden');
				expect( getFileInfo( path ).isHidden ).toBe('true');
			});

			it(title = "checking the file with Normal Attribute", body = function( currentSpec ) {
				fileSetAttribute(path,'Normal');
				var info = getFileInfo( path );
				expect( info.canRead ) .toBe('true');
				expect( info.canWrite ).toBe('true');
				expect( info.isHidden ).toBe('false');
				expect( info.isSystem ).toBe('false');
				expect( info.isArchive ).toBe('false');
			});
		});
	}
}