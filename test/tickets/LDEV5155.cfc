component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV5155");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV5155", function(){
			it(title = "StructKeyExists should not create session when no session exists.",
					skip = true,
					body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/ldev5155_structKeyExists.cfm",
					url: {
						type: "structKeyexists"
					}
				);
				expect( trim ( local.result.filecontent ) ).toBe( false, "session was created" );
			});

			it(title = "elvis operator should not create session when no session exists.",
					skip = true,
					body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/ldev5155_elvis.cfm",
					url: {
						type: "elvis"
					}
				);
				expect( trim ( local.result.filecontent ) ).toBe( false, "session was created" );
			});

		});

		describe( "Test case for SessionExists - LDEV-5241", function(){
			it(title = "Test sessionExists after creating session",
					body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/ldev5155_sessionExists.cfm",
					url: {
						type: "sessionExists_with_session",
						createSession: true
					}
				);
				expect( trim ( local.result.filecontent ) ).toBe( "true:true", "session was created and exists" );
			});

			it(title = "Test sessionExists without a session",
					body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/ldev5155_sessionExists.cfm",
					url: {
						type: "sessionExists_without_session"
					}
				);
				expect( trim ( local.result.filecontent ) ).toBe( "false:false", "session wasn't created" );
			});

		});

	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
