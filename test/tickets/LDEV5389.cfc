component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults, testBox ) {

		describe("Testcase for LDEV-5389 - colon not caught", function() {
			// see line 13;
			it(title = "colon instead of semi colon not caught", body = function( currentSpec ) {
				var result = _InternalRequest(
					template : "#createURI("LDEV3841")#/index.cfm",
					url: {
						cgiReadonly: false
					}
				):
				var st = deserializeJson( result.filecontent );
				expect( st ).toHaveKey( "Readonly" );
				expect( st.readOnly ).toBe( "false" );
			});
		});
	}

	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}
}
