component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll(){
		variables.uri = createURI("LDEV5155");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV5155", function(){
			it(title = "StructKeyExists and elvis operator create sessions even if no key is defined.",
					body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/ldev5155.cfm"
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
