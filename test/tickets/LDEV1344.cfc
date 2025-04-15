component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true labels="mysql" {

	function beforeAll(){
		variables.uri = createURI("LDEV1344");
	}

	function run( testResults, testBox ){
		describe("Test case for LDEV1344", function(){
			it( title = "INSERT date without queryparam", skip=isNotSupported(), body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/ldev1344.cfm"
				);
				expect(trim(result.filecontent)).toBe(1);
			});
		});
	}

	private function isNotSupported() {
		return isEmpty(server.getDatasource("mysql"));
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}