component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	function run( testResults , testBox ) {

		describe( title="Test case LDEV-5730", body=function() {
			// error only repos when running on a servlet, not via unit tests
			it(title="checking sessionRotate with jsession", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5730" );
				var result =_InternalRequest(
					template: "#uri#/ldev5730.cfm"
				);
			});

		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
