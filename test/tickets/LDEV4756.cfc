component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie" {

	/*
		This test case supports being run via a browser, when run via a browser, it will test both via internalRequest and via CFHTTP
	*/


	function run( testResults , testBox ) {
		describe( "Test suite for LDEV4756 Partitioned Cookies - tag cfcookie", function() {
			it( title='check cfcookie tag defaults, secure, partitioned, path', body=function( currentSpec ) {
				var sessionReq = test("/tag-defaults/index.cfm",{
					secure: true,
					partitioned: true,
					path: "/",
					tagDefaults: false
				});
				//dumpResult( sessionReq );
				// debug(sessionReq);
				local.str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				local.sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( sct.path ).toBe( "/" );
				expect( sct.VALUE ).toBe( "LDEV4756" );
				expect( structKeyExists( sct, "Partitioned" ) ).toBeTrue("Partitioned attribute should exist [#str#]");
				expect( structKeyExists( sct, "secure" ) ).toBeTrue();
			});

			it( title='check cfcookie tag secure, partitioned, path', body=function( currentSpec ) {
				var sessionReq = test("/tag-defaults/index.cfm",{
					secure: true,
					partitioned: true,
					path: "/",
					tagDefaults: true
				});
				//dumpResult( sessionReq );

				local.str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				local.sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( sct.path ).toBe( "/" );
				expect( sct.VALUE ).toBe( "LDEV4756" );
				expect( structKeyExists( sct, "Partitioned" ) ).toBeTrue("Partitioned attribute should exist [#str#]");
				expect( structKeyExists( sct, "secure" ) ).toBeTrue();
			});

			// currently not enforcing these client side business rules

			xit( title='check cfcookie tag Partitioned, no path', body=function( currentSpec ) {
				expect( function(){
					var sessionReq = test("/tag-defaults/index.cfm",{
						secure: true,
						partitioned: true,
						path: "",
						tagDefaults: false
					});
				}).toThrow(); // Partitioned requires path="/"
			});

			xit( title='check cfcookie tag Partitioned, no secure', body=function( currentSpec ) {
				expect( function(){
					var sessionReq = test("/tag-defaults/index.cfm",{
						secure: true,
						partitioned: true,
						path: "/",
						tagDefaults: false
					});
				}).toThrow(); // Partitioned requires secure="/"
			});

			xit( title='check cfcookie tag Partitioned, no secure, no path', body=function( currentSpec ) {
				expect( function(){
					var sessionReq = test("/tag-defaults/index.cfm",{
						secure: false,
						partitioned: true,
						path: "",
						tagDefaults: false
					});
				}).toThrow(); // Partitioned requires path="/" and secure
			});
		});

		describe( "Test suite for LDEV4756 Partitioned Session cookies ", function() {
			it( title='check cfml session cookie partitioned: true', body=function( currentSpec ) {
				local.sessionReq = test("/session-cookie/index.cfm",{
					partitioned: true
				});
				//dumpResult( sessionReq );
				local.str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				//dumpResult( str );
				expect( len( trim( str ) ) ).toBeGT( 0 );
				local.sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( structKeyExists( sct, "Partitioned" ) ).toBeTrue();
			});

			it( title='check cfml session cookie partitioned: false', body=function( currentSpec ) {
				local.sessionReq = test("/session-cookie/index.cfm", {
					partitioned: false
				});
				//dumpResult( sessionReq );
				local.str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				//dumpResult( str );
				expect( len( trim( str ) ) ).toBeGT( 0 );
				local.sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( structKeyExists( sct, "Partitioned" ) ).toBeFalse();
			});

			it( title='check cfml session cookie partitioned: unset', body=function( currentSpec ) {
				local.sessionReq = test("/session-cookie/index.cfm", {
					partitioned: ""
				});
				//dumpResult( sessionReq );
				local.str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				//dumpResult( str );
				expect( len( trim( str ) ) ).toBeGT( 0 );
				local.sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( structKeyExists( sct, "Partitioned" ) ).toBeFalse();

			});
		});

		describe( "Test suite for LDEV4756 Partitioned Session Cookies - getApplicationSettings() ", function() {
		
			it( title='checking sessionCookie keys & values on getApplicationSettings() partitioned: true', body=function( currentSpec ) {
				var sessionCookie = test("/session-cookie/index.cfm", {
					partitioned: true
				}).filecontent.trim();
				var result = deserializeJSON( sessionCookie );

				expect( result.SAMESITE ).toBe("none");
				expect( result.HTTPONLY ).toBeTrue();
				expect( result.DOMAIN ).toBe("www.lucee.org");
				expect( result.PATH ).toBe("/");
				expect( result.TIMEOUT ).toBe("1.0");
				expect( result.SECURE ).toBeTrue();
				expect( result.PARTITIONED ).toBeTrue();

			});

			 it( title='checking sessionCookie keys & values on getApplicationSettings(), partitioned: false', body=function( currentSpec ) {
				var sessionCookie = test("/session-cookie/index.cfm", {
					partitioned: false
				}).filecontent.trim();
				var result = deserializeJSON( sessionCookie );

				expect( result.SAMESITE ).toBe("none");
				expect( result.HTTPONLY ).toBeTrue();
				expect( result.DOMAIN ).toBe("www.lucee.org");
				expect( result.PATH ).toBe("/");
				expect( result.TIMEOUT ).toBe("1.0");
				expect( result.SECURE ).toBeTrue();
				expect( result.PARTITIONED ).toBeFalse();

			});

		});

	};

	private function test(template, args){
		var jsr223 = (cgi.request_url eq "http://localhost/index.cfm")
		var uri = createURI("LDEV4756", !jsr223);
		//systemOutput(arguments, true);
		var result = _InternalRequest(
			template : "#uri##template#",
			url: args
		);
		//debug(result, "internalRequest");
		//expect( structCount( result.cookies ) ).toBe( expectedCookieCount );
		if ( !jsr223 ){ // running via a web browser, let's try http, to compare to internalRequest
			var httpUri = createURI("LDEV4756", true);
			var hostIdx = find(cgi.script_name, cgi.request_url);
			if (hostIdx gt 0){
				var host = left(cgi.request_url, hostIdx-1);
				var webUrl = host & httpUri & template;
				systemOutput("could do http! testing via [#webUrl#]", true);
			} else {
				throw "failed to extract host [#hostIdx#] from cgi [#cgi.script_name#], [#cgi.request_url#]";
			}
			var httpResult = "";
			http method="get" url="#webUrl#" result="httpResult"{
				structEach(arguments.args, function(k,v){
					httpparam name="#k#" value="#v#" type="url";
				});
			}
			
			// force cfhttp result to be like internalRequest result;
			httpResult.cookies = queryToStruct(httpResult.cookies, "name");
			httpResult.headers = httpResult.responseHeader;
			debug(httpResult,"cfhttp");
			expect( structCount( httpResult.cookies ) ).toBe( structCount( result.cookies ),
				"cfhttp [#httpResult.cookies.toJson()#] differs from internalRequest [#result.cookies.toJson()#]" );
			compareSetCookies(result, httpResult);
		}
		debug(result,"internalRequest");
		return result;
	}

	private string function getCookieFromHeaders( struct headers, string name ){
		local.arr = arguments.headers[ 'Set-Cookie' ];
		local.str = '';
		if ( isSimpleValue( arr ) ) arr = [ arr ]; // single cookies don't end up in an array
		loop array=arr item="local.entry" {
			if( findNoCase( arguments.name & '=', entry ) eq 1 )
				str = entry;
		}
		return str;
	}

	private struct function toCookieStruct( string str ){
		local.arr = listToArray( str,';' );
		local.sct={};
		loop array=arr item="local.entry" {
			sct[ trim( listFirst( entry, '=' ) ) ] = listLen( entry, '=' ) == 1 ? "" : trim( listLast( entry, '=' ) );
		}
		return sct;
	}

	private function dumpResult(r){
		// systemOutput( "---", true );
		// systemOutput( r, true );
		// systemOutput( "---", true );
 	}

	private string function createURI(string calledName, boolean contract=false){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}

	private void function compareSetCookies (result, httpResult){
		if ( structKeyExists(result, "value" ) ){
			var res = getCookieFromHeaders(result.headers, "value" );
			var resHttp = getCookieFromHeaders(httpResult.headers, "value" );
			expect( res ).toBe( resHttp );
		}
	}
}