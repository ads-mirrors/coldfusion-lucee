component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {

		describe( title="Test case LDEV-5621", body=function() {


			it(title="checking URLDecode() function via internal request %26", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5621/index.cfm" );
				systemOutput(uri,1,1);
				var result =_InternalRequest(
					template: uri
				);
				systemOutput(result,1,1);
				var mi = deserializeJSON(result.fileContent);

				//systemOutput(mi,1,1);
				expect( mi.groupId ).toBe( "org.apache.tika");
				expect( mi.artifactId ).toBe( "tika-core");
				expect( mi.version ).toBe( "1.28.5" );


			});


		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
