component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie" skip=true {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV5288", function() {
			it( title='internalRequest should return jsessionId cookie', body=function( currentSpec ) {
				uri = createURI("LDEV5288");
				local.result = _InternalRequest(
					template : "#uri#/ldev5288-createJsession.cfm"
				);
				_dumpResult( local.result );
				//expect( structCount( result.cookies ) ).toBe( 1 );
				var _cookies = _getCookies( result, "jsessionId" );
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
			});
		});
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

	private function _dumpResult( result ){
		systemOutput( result.headers[ "Set-Cookie" ] ?: "[]", true );
		systemOutput( result.cookies, true );
		systemOutput( result, true );
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}