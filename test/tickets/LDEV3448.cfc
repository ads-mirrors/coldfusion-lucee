component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV3448", function() {
			it( title='check cfml session cookie defaults, httponly, samesite=lax', body=function( currentSpec ) {
				var sessionReq = test(
					template : "/session-cfml/index.cfm"
				);
				//dumpResult( sessionReq );
				var str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				//dumpResult( str );
				expect( len( trim( str ) ) ).toBeGT( 0 );
				var sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( sct ).toHaveKey( "HTTPOnly" );
				expect( sct ).toHaveKey( "Samesite" );
				expect( sct.Samesite ).toBe( "lax" );

			});

			it( title='check overriding cfml session cookie defaults, httponly=false, samesite=""', body=function( currentSpec ) {
				var sessionReq = test(
					template : "/session-cfml/index.cfm",
					urlArgs: {
						samesite: "",
						httponly: false
					}
				);
				// dumpResult( sessionReq );
				var str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				// dumpResult( str );
				expect( len( trim( str ) ) ).toBeGT( 0 );
				var sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( sct ).notToHaveKey( "HTTPOnly" );
				expect( sct ).notToHaveKey( "Samesite" );

			});

			it( title='check overriding cfml session cookie defaults, httponly=false, samesite="strict"', body=function( currentSpec ) {
				var sessionReq = test(
					template : "/session-cfml/index.cfm",
					urlArgs: {
						samesite: "strict",
						httponly: false
					}
				);
				// dumpResult( sessionReq );
				var str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				// dumpResult( str );
				expect( len( trim( str ) ) ).toBeGT( 0 );
				var sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( sct ).notToHaveKey( "HTTPOnly" );
				expect( sct ).toHaveKey( "Samesite" );
				expect( sct.Samesite ).toBe( "strict" );
			});

			it( title='check cfml session, httponly=false', body=function( currentSpec ) {
				var sessionReq = test(
					template : "/session-cfml-no-httpOnly/index.cfm"
				);
				//dumpResult( sessionReq );

				var str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );
				var sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( sct ).notToHaveKey( "HTTPOnly" );
			});

			// TODO disabled, j2ee sessions aren't created by internal request (yet)
			xit( title='LDEV-5335 check jee session cookie defaults, httponly, samesite=lax', skip=isJsr232(), body=function( currentSpec ) {
				var sessionReq = test(
					template : "/session-jee/index.cfm"
				);
				dumpResult( sessionReq );

				var str = getCookieFromHeaders(sessionReq.headers, "jsessionid" );
				dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				var sct = toCookieStruct( str );
				dumpResult( sct );
				expect( sct ).toHaveKey( "HTTPOnly" );
				expect( sct ).toHaveKey( "Samesite" );
				expect( sct.Samesite ).toBe( "lax" );
			});

			xit( title='LDEV-5335 check overriding jee session cookie defaults, httponly=false, samesite="strict"', skip=isJsr232(), body=function( currentSpec ) {
				var sessionReq = test(
					template : "/session-jee/index.cfm",
					urlArgs: {
						samesite: "strict",
						httponly: false
					}
				);
				// dumpResult( sessionReq );
				var str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				// dumpResult( str );
				expect( len( trim( str ) ) ).toBeGT( 0 );
				var sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( sct ).toHaveKey( "Samesite" );
				expect( sct.Samesite ).toBe( "strict" );
				expect( sct ).notToHaveKey( "HTTPOnly" );
				
			});

		});
	}

	private string function getCookieFromHeaders( struct headers, string name ){
		var arr = arguments.headers[ 'Set-Cookie' ];
		var str = '';
		loop array=arr item="local.entry" {
			if( findNoCase( arguments.name & '=', entry ) eq 1 )
				str = entry;
		}
		return str;
	}

	private struct function toCookieStruct( string str ){
		var arr = listToArray( str,';' );
		var sct={};
		loop array=arr item="local.entry" {
			sct[ trim( listFirst( entry, '=' ) ) ] = listLen( entry, '=' ) == 1 ? "" : trim( listLast( entry, '=' ) );
		}
		return sct;
	}

	private function isJsr232(){
		return (cgi.request_url eq "http://localhost/index.cfm");
	}

	private function test(template, urlArgs={}){
		systemOutput(arguments, true);
		var jsr223 = isJsr232();
		if ( jsr223 ){ 
			var uri = createURI("LDEV3448");
			var result = internalRequest(
				template : uri & arguments.template,
				url: arguments.urlArgs
			);
			return result;
		} else {
			// running via a web browser, let's try http, which also supports jee sessions
			var hostIdx = find(cgi.script_name, cgi.request_url);
			if (hostIdx gt 0){
				var host = left(cgi.request_url, hostIdx-1);
				var webUrl = host & "/test/tickets/LDEV3448" & arguments.template;
				systemOutput("could do http! testing via [#webUrl#]", true);
			} else {
				throw "failed to extract host [#hostIdx#] from cgi [#cgi.script_name#], [#cgi.request_url#]";
			}
			var httpResult = "";
			http method="get" url="#webUrl#" result="httpResult" throwOnError="true" {
				structEach(arguments.urlArgs, function(k,v){
					httpparam name="#k#" value="#v#" type="url";
				});
			}

			//debug(httpResult,"cfhttp - #template#");

			// force cfhttp result to be like internalRequest result;
			httpResult.cookies = queryToStruct(httpResult.cookies, "name");
			httpResult.headers = httpResult.responseHeader;
			//debug(httpResult,"cfhttp - #template#");
			/*
			expect( structCount( httpResult.cookies ) ).toBe( structCount( result.cookies ),
				"cfhttp [#httpResult.cookies.toJson()#] differs from internalRequest [#result.cookies.toJson()#]" );
			*/
		}
		return httpResult;
	}


	private function dumpResult(r){
		// systemOutput("", true);
		// systemOutput("Cookies: " & serializeJson(r.cookies), true);
		// systemOutput("Headers: " & serializeJson(r.headers), true);
		// systemOutput("", true);
	}

	private string function createURI(string calledName, boolean contract=false){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}
}