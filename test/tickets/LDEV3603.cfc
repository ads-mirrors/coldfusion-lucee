component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "Test servlet attributes in cgi scope", function(){

			it( "check cgi read only", function(){
				var result = _InternalRequest(
					template : "#createURI("LDEV3603")#/readOnlyCGI/ldev3603_ro.cfm"
				);
				expect( result.fileContent ).toBe("ldev3603" );
			});

			it( "check cgi read / write", function(){
				var result = _InternalRequest(
					template : "#createURI("LDEV3603")#/readWriteCGI/ldev3603_rw.cfm"
				);
				expect( result.fileContent ).toBe("ldev3603" );
			});

		} );
	}

	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}

}
