component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread,cookie,session" {

	/*
		This test case supports being run via a browser, when run via a browser, it will test both via internalRequest and via CFHTTP
	*/

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-5292", function() {

			it( title='thread / should create one cfml session', body=function( currentSpec ) {
				var args = {
					createSessionInThread: true,
					testThread: true,
					checkSession: true
				};
				test(args=args, currentSpec=currentSpec, expectedCookieCount=1);
			});

			it( title='thread / should not create cfml session', body=function( currentSpec ) {
				var args = {
					createSessionInThread: false,
					testThread: true,
					checkSession: true
				};
				test(args=args, currentSpec=currentSpec, expectedCookieCount=0);
			});

			it( title='getPageContext().hasCFSession() should not create session', body=function( currentSpec ) {
				var args = {
					createSessionInThread: false,
					testThread: false,
					checkSession: true
				};
				test(args=args, currentSpec=currentSpec, expectedCookieCount=0);
			});

			it( title='should not create session (with thread)', body=function( currentSpec ) {
				var args = {
					createSessionInThread: false,
					testThread: true,
					checkSession: false
				};
				test(args=args, currentSpec=currentSpec, expectedCookieCount=0);
			});

			it( title='should not create session', body=function( currentSpec ) {
				var args = {
					createSessionInThread: false,
					testThread: false,
					checkSession: false
				};
				test(args=args, currentSpec=currentSpec, expectedCookieCount=0);
			});

		});
	}

	private function test(args, currentSpec, expectedCookieCount){
		//systemOutput("---------------------", true);
		//debug(arguments);
		var jsr223 = (cgi.request_url eq "http://localhost/index.cfm")
		var uri = createURI("LDEV5292", !jsr223);
		var template = "/cfml-session/testThreadCookies.cfm";
		//systemOutput(arguments, true);
		var result = _InternalRequest(
			template : "#uri##template#",
			url: args
		);
		//debug(result, "internalRequest");
		//expect( structCount( result.cookies ) ).toBe( expectedCookieCount );
		var _cookies = _getCookies( result, "cfid" );
		if (expectedCookieCount gt 0 ) {
			expect( structKeyExists(result.cookies, "CFID" ) ).toBeTrue();
			expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
		} else {
			expect( ArrayLen( _cookies ) ).toBe( expectedCookieCount, "internalRequest cookies returned [#_cookies.toJson()#]" );
		}
		if ( !jsr223 ){ // running via a web browser, let's try http, to compare to internalRequest
			var httpUri = createURI("LDEV5292", true);
			var hostIdx = find(cgi.script_name, cgi.request_url);
			if (hostIdx gt 0){
				var host = left(cgi.request_url, hostIdx-1);
				var webUrl = host & httpUri & template;
				//systemOutput("could do http! testing via [#webUrl#]", true);
			} else {
				throw "failed to extract host [#hostIdx#] from cgi [#cgi.script_name#], [#cgi.request_url#]";
			}
			var webResult = "";
			http method="get" url="#webUrl#" result="webResult"{
				structEach(arguments.args, function(k,v){
					httpparam name="#k#" value="#v#" type="url";
				});
			}
			
			// force cfhttp result to be like internalRequest result;
			webResult.cookies = queryToStruct(webResult.cookies, "name");
			webResult.headers = webResult.responseHeader;
			//debug(webResult, "cfhttp");
			var _cookies = _getCookies( webResult, "cfid" );
			if (expectedCookieCount gt 0 ) {
				expect( structKeyExists(webResult.cookies, "CFID" ) ).toBeTrue();				
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
			} else {
				expect( ArrayLen( _cookies ) ).toBe( expectedCookieCount, "cfhttp cookies returned [#_cookies.toJson()#]" );
			}
		}
	}

	private string function createURI(string calledName, boolean contract=false){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
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