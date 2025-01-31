component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread,cookie,session" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-5292", function() {

			it( title='thread / should not create second cfml session', body=function( currentSpec ) {
				uri = createURI("LDEV5292");
				local.result = _InternalRequest(
					template : "#uri#/cfml-session/testThreadCookies.cfm",
					url: {
						testSession: true,
						testThread: true
					}
				);
				expect( structCount(result.cookies ) ).toBeGT( 0 );
				expect( structKeyExists(result.cookies, "CFID" ) ).toBeTrue();
				var _cookies = _getCookies( result, "cfid" );
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
			});

			it( title='thread / should not create second cfml session', body=function( currentSpec ) {
				uri = createURI("LDEV5292");
				local.result = _InternalRequest(
					template : "#uri#/cfml-session/testThreadCookies.cfm",
					url: {
						testSession: false,
						testThread: true
					}
				);
				expect( structCount(result.cookies ) ).toBeGT( 0 );
				expect( structKeyExists(result.cookies, "CFID" ) ).toBeTrue();
				var _cookies = _getCookies( result, "cfid" );
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
			});

			it( title='getPageContext().hasCFSession() should not create session', body=function( currentSpec ) {
				uri = createURI("LDEV5292");
				local.result = _InternalRequest(
					template : "#uri#/cfml-session/testThreadCookies.cfm",
					url: {
						testSession: false,
						testThread: false
					}
				);
				expect( structCount(result.cookies ) ).toBe( 0 );
			});

		});
	}


	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function _getCookies( result, name ){
		var headers = result.headers[ "Set-Cookie" ] ?: [];
		systemOutput(headers, true);
		var matches = [];
		for ( var header in headers ){
			if ( listFirst( header, "=" ) eq arguments.name )
				arrayAppend( matches, header );
		}
		return matches;
	}
}