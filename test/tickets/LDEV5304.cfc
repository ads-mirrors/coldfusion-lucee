component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread,cookie,session" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-5304", function() {

			it( title='unknown session cookies should be rotated', skip=true, body=function( currentSpec ) {
				var uri = createURI("LDEV5304");
				var cfid = createGUID();
				local.result = _InternalRequest(
					template : "#uri#/cfml-session/testUnknownSessionCookies.cfm",
					cookies: {
						cftoken: 0,
						cfid: cfid 
					}
				);
				//systemOutput(result, true);
				var _cookies = _getCookies( result, "cfid" );
				expect( structCount(result.cookies ) ).toBeGT( 0 );
				expect( structKeyExists(result.cookies, "CFID" ) ).toBeTrue();
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
				expect( result.cookies.CFID ).notToBe( cfid );
			});

			it( title='unknown url session cookies should be rotated', body=function( currentSpec ) {
				var uri = createURI("LDEV5304");
				var cfid = createGUID();
				local.result = _InternalRequest(
					template : "#uri#/cfml-session/testUnknownSessionCookies.cfm",
					url: {
						cftoken: 0,
						cfid: cfid 
					}
				);
				//systemOutput(result, true);
				var _cookies = _getCookies( result, "cfid" );
				expect( structCount(result.cookies ) ).toBeGT( 0 );
				expect( structKeyExists(result.cookies, "CFID" ) ).toBeTrue();
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
				expect( result.cookies.CFID ).notToBe( cfid );
			});

		});
	}

	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}

	private function _getCookies( result, name ){
		var headers = result.headers[ "Set-Cookie" ] ?: [];
		var matches = [];
		for ( var header in headers ){
			if ( listFirst( header, "=" ) eq arguments.name )
				arrayAppend( matches, header );
		}
		return matches;
	}
}