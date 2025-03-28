component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults, testBox ) {
		// this is still supported in 6, it's being removed in 7
		describe("LDEV-5403 remove support for loginStorage=cookie and sessionStorage=cookie", function() {

			it( title="check loginStorage=cookie works in 6",skip="isNotSupported", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#createURI("LDEV5403")#/loginStorageCookie/index.cfm"
				);
				expect( structKeyList( result.cookies ) ).toInclude( "CFAUTHORIZATION_" );
			});

			it( title="check sessionStorage=cookie works in 6",skip="isNotSupported",  body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#createURI("LDEV5403")#/sessionStorageCookie/index.cfm"
				);
				expect( structKeyList( result.cookies ) ).toInclude( "CF_SESSION_" );
			});
		});
	}


	private function isNotSupported() {
		return listFirst(server.lucee,version,'.')>6;
	}

	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}

}