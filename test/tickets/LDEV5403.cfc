component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults, testBox ) {
		describe("LDEV-5403 remove support for loginStorage=cookie", function() {

			it( title="check loginStorage=cookie throws", body=function( currentSpec ) {
				expect(function(){
					var result = _InternalRequest(
						template : "#createURI("LDEV5403")#/loginStorageCookie/index.cfm",
						url: {
							"loginStorage": "cookie"
						}
					);
				}).toThrow(); //  invalid loginStorage definition, cookie no longer supported
			});

			it( title="check loginStorage=session", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#createURI("LDEV5403")#/loginStorageCookie/index.cfm",
					url: {
						"loginStorage": "session"
					}
				);
			});

			it( title="check loginStorage default", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#createURI("LDEV5403")#/loginStorageCookie/index.cfm",
					url: {
						"loginStorage": ""
					}
				);
			});
		});

		describe("LDEV-5403 remove support sessionStorage=cookie", function() {

			it( title="check sessionStorage=cookie throws ", body=function( currentSpec ) {
				expect(function(){
					var result = _InternalRequest(
						template : "#createURI("LDEV5403")#/sessionStorageCookie/index.cfm"
					);
				}).toThrow(); //  sessionStorage cookie is no longer supported
			});
		});
	}

	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}

}