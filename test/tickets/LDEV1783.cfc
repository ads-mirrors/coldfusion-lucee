component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "test case for LDEV-1783", function() {
			it(title = "Checking exception includes template", body = function( currentSpec ) {
				try {
					var result = _InternalRequest(
						template:"#createURI("LDEV1783")#/LDEV1783.cfm"
					).filecontent;
				}
				catch(any e) {
					var result = e.stacktrace;
				}
				expect(trim(result)).toInclude("LDEV1783.cfm");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}