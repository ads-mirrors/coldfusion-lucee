component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {
	
	function run( testResults , testBox ) {
		describe( "LDEV-5291 - csrfVerifyToken remove=true", function() {

			it( title='cfml session', body=function( currentSpec ) {
				var uri = createURI("LDEV5291");
				var cfmlSessionId = _InternalRequest(
					template : "#uri#/cfml_csrf_remove/test_csrf_remove.cfm"
				);
				expect( cfmlSessionId.fileContent ).toInclude( "all good" );
			});

			it( title='jee session', body=function( currentSpec ) {
				var uri = createURI("LDEV5291");
				var j2eeSessionId = _InternalRequest(
					template : "#uri#/jee_csrf_remove/test_csrf_remove.cfm"
				);
				expect( j2eeSessionId.fileContent ).toInclude( "all good" );
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}