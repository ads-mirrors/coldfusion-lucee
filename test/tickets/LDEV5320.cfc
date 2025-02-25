component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( title='test cases for getSession() in thread when request has no session' , body=function(){

			xit( title='thread getSession() no session', body=function() {
				var uri = createURI("LDEV5320");
				var result = _InternalRequest(
					template : "#uri#/cfml-session/threadSession.cfm",
					url: {
						createSession: ""
					}
				);
				var threadResult = DeserializeJson(result.fileContent);
				expect(threadResult.error.stacktrace?:"").toBe("");
			});

			xit( title='thread getSession() create session in thread', body=function() {
				var uri = createURI("LDEV5320");
				var result = _InternalRequest(
					template : "#uri#/cfml-session/threadSession.cfm",
					url: {
						createSession: "inThread"
					}
				);
				var threadResult = DeserializeJson(result.fileContent);
				expect(threadResult.error.stacktrace?:"").toBe("");
			});

			xit( title='thread getSession() create session in request before thread', body=function() {
				var uri = createURI("LDEV5320");
				var result = _InternalRequest(
					template : "#uri#/cfml-session/threadSession.cfm",
					url: {
						createSession: "before"
					}
				);
				var threadResult = DeserializeJson(result.fileContent);
				expect(threadResult.error.stacktrace?:"").toBe("");
			});

			it( title='thread no getSession()', body=function() {
				var uri = createURI("LDEV5320");
				var result = _InternalRequest(
					template : "#uri#/cfml-session/threadSession.cfm",
					url: {
						createSession: "noGetSession"
					}
				);
				var threadResult = DeserializeJson(result.fileContent);
				expect(threadResult.error.stacktrace?:"").toBe("");
			});

		});
	}

	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}
}