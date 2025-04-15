component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true labels="mssql" {

	function beforeAll(){
		variables.uri = createURI("LDEV2708");
	}

	function run( testResults, testBox ){
		describe("Test case for LDEV2708", function(){
			it( title = "INSERT timestamp object using cfsqltype='cf_sql_varchar'", skip=isNotSupported(), body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/ldev2708.cfm"
				);
				expect(trim(result.filecontent)).toBe(1);
			});
		});
	}

	private function isNotSupported() {
		return isEmpty(server.getDatasource("mssql"));
	}


	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}