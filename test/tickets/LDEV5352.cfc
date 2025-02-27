component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-5352", body=function() {
			it(title = "checking compiler issue", body = function( currentSpec ) {
				// we neeed to call the code failing via internal request, because testbox does not like compiler erros
				local.result = _InternalRequest(
					template : "#createURI("LDEV5352")#/index.cfm"
				);
			});
		});
	}

	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}
}
