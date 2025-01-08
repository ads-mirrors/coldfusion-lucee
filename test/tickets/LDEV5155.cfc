component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll(){
		variables.uri = createURI("LDEV5155");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV5155", function(){
			it(title = "StructKeyExists should not create session when no session exists.",
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
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
