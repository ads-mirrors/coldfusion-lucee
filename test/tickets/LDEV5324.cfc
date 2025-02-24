component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	/* this only works via a browser, internalRequest doesn't support rest mappings */

	function beforeAll(){
		dumpRestMappings();
		request.serverAdminPassword = "yourAdminPassword"; // CHANGE TO YOUR LOCAL ADMIN PASSWORD!!!
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV5324/"
		RestInitApplication( dir, '/ldev5324', false, request.serverAdminPassword );
		dumpRestMappings();
	}

	function afterAll(){
		dumpRestMappings();
		RestDeleteApplication( dir, request.serverAdminPassword );
		dumpRestMappings();
	}

	private function dumpRestMappings(){
		return;
		systemOutput("---getRestMappings---", true);
		var mappings=getPageContext().getConfig().getRestMappings();
		loop array=mappings item="m" {
			systemOutput( m, true );
		}
	}

	function run( testResults, testBox ){
		describe( "LDEV-5324 invalid combinations", function(){

			it( "check rest component has the correct application scope", function(){
				var result = test(path="/rest/ldev5324/ldev5324/getApplicationName", args={}, method="GET");
				expect( trim( result.fileContent ) ).toBe( '"applicationName:lucee"' ) // .toBe( '"applicationName:ldev5324"' );  see LDEV5323
			});

			it( "check rest method with wrong httpmethod (GET)", function(){
				var result = test(path="/rest/ldev5324/ldev5324/wrongMethod/", args={}, method="GET");
				expect( trim( result.fileContent ) ).toBe( '"WrongMethod"' );
			});

			xit( "check rest method with wrong httpmethod (POST instead of GET)", function(){
				var result = test(path="/rest/ldev5324/ldev5324/wrongMethod/", args={}, method="POST");
				expect( trim( result.fileContent ) ).toBe( 'REST endpoint [/ldev5324/wrongMethod/] only supports [GET]' );
			});

			it( "check rest method which doesn't exist", function(){
				var result = test(path="/rest/ldev5324/ldev5324/missingMethod/", args={}, method="GET");
				expect( trim( result.fileContent ) ).toBe( 'no rest service for [/ldev5324/missingMethod/] found' );
			});
		});

		xdescribe( "LDEV-5324 endpoint which shouldn't return Subresource locator error.", function(){

			// as this endpoint has no http method or component return type, it should not attempt sub resource type
			it( "check rest method with no httpmethod or return type", function(){
				var result = test(path="/rest/ldev5324/ldev5324nosrl/noMethod/", args={}, method="GET");
				expect( trim( result.fileContent ) ).toBe( 'REST endpoint [/ldev5324nosrl/noMethod/] only supports []' );
			});

			it( "check rest method with no httpmethod", function(){
				var result = test(path="/rest/ldev5324/ldev5324/noMethod/", args={}, method="GET");
				expect( trim( result.fileContent ) ).toBe( 'REST endpoint [/ldev5324/noMethod/] only supports []' );
			});

			
		});

		describe( "LDEV-5324 subresource", function(){
			it( "check sub resource locator GET", function(){
				var result = test(path="/rest/ldev5324/ldev5324/Name-1/", args={}, method="GET");
				expect( trim( result.fileContent ) ).toBe( '{"Name":"Name","AGE":"1"}' );
			});

			it( "check sub resource locator PUT", function(){
				var result = test(path="/rest/ldev5324/ldev5324/Name-1/", args={}, method="PUT");
				expect( trim( result.fileContent ) ).toBe( "" );
			});

			it( "check sub resource locator DELETE", function(){
				var result = test(path="/rest/ldev5324/ldev5324/Name-1/", args={}, method="DELETE");
				expect( trim( result.fileContent ) ).toBe( true );
			});

			it( "check sub resource locator GET invalid rest path", function(){
				var result = test(path="/rest/ldev5324/ldev5324/Name/", args={}, method="GET");
				expect( trim( result.fileContent ) ).toBe( 'no rest service for [/ldev5324/Name/] found' );
			});

		});

		describe( "LDEV-5324 multiple methods", function(){

			it( "check rest method supporting multiple methods (GET)", function(){
				var result = test(path="/rest/ldev5324/ldev5324/multipleMethods/", args={}, method="GET");
				expect( trim( result.fileContent ) ).toBe( '"GET"' );
			});

			it( "check rest method supporting multiple methods (POST)", function(){
				var result = test(path="/rest/ldev5324/ldev5324/multipleMethods/", args={}, method="POST");
				expect( trim( result.fileContent ) ).toBe( '"POST"' );
			});

			xit( "check rest method supporting multiple methods (DELETE)", function(){
				var result = test(path="/rest/ldev5324/ldev5324/multipleMethods/", args={}, method="DELETE");
				expect( trim( result.fileContent ) ).toBe( 'REST endpoint [/ldev5324/multipleMethods/] only supports [GET,POST]' );
			});

		});

		describe( "LDEV-5324 multiple methods - single rest method", function(){

			it( "check rest method supporting multiple methods (DELETE)", function(){
				var result = test(path="/rest/ldev5324/ldev5324multiple/multipleMethods/", args={}, method="DELETE");
				expect( trim( result.fileContent ) ).toBe( '"DELETE"' );
			});

			it( "check rest method supporting multiple methods (POST)", function(){
				var result = test(path="/rest/ldev5324/ldev5324multiple/multipleMethods/", args={}, method="POST");
				expect( trim( result.fileContent ) ).toBe( '"POST"' );
			});

			xit( "check rest method supporting multiple methods, unsupported method (GET)", function(){
				var result = test(path="/rest/ldev5324/ldev5324multiple/multipleMethods/", args={}, method="GET");
				expect( trim( result.fileContent ) ).toBe( 'REST endpoint [/ldev5324multiple/multipleMethods/] only supports [DELETE,POST]' );
			});

		});

		describe( "LDEV-5324 multiple methods - no matching rest cfc", function(){

			it( "check non existant rest services", function(){
				var result = test(path="/rest/ldev5324/ldev5324missing/notExists/", args={}, method="GET");
				expect( trim( result.fileContent ) ).toBe( 'no REST service for [/ldev5324missing/notExists/] found' );
			});

		});

	}

	private function test(path, method, args={}){
		var jsr223 = (cgi.request_url eq "http://localhost/index.cfm")
		/*
		var uri = createURI("LDEV5324", !jsr223);
		//systemOutput(arguments, true);
		var result = _InternalRequest(
			template : "#uri##template#",
			url: args
		);
		*/
		//debug(result, "internalRequest");
		//expect( structCount( result.cookies ) ).toBe( expectedCookieCount );
		if ( jsr223 ){ // running via a web browser, let's try http, to compare to internalRequest
			throw "internalRequest doesn't support REST urls";
		} else {
			var hostIdx = find(cgi.script_name, cgi.request_url);
			if (hostIdx gt 0){
				var host = left(cgi.request_url, hostIdx-1);
				var webUrl = host & arguments.path;
				systemOutput("could do http! testing via [#webUrl#]", true);
			} else {
				throw "failed to extract host [#hostIdx#] from cgi [#cgi.script_name#], [#cgi.request_url#]";
			}
			
			var httpResult = "";
			http method="#arguments.method#" url="#webUrl#" result="httpResult"{
				structEach(arguments.args, function(k,v){
					httpparam name="#k#" value="#v#" type="url";
				});
			}

			// force cfhttp result to be like internalRequest result;
			httpResult.cookies = queryToStruct(httpResult.cookies, "name");
			httpResult.headers = httpResult.responseHeader;
			debug(httpResult,"cfhttp");
			/*
			expect( structCount( httpResult.cookies ) ).toBe( structCount( result.cookies ),
				"cfhttp [#httpResult.cookies.toJson()#] differs from internalRequest [#result.cookies.toJson()#]" );
			*/
		}
		systemOutput("REST endpoint [#method#] [#path#] returned [#httpResult.statuscode#] [#httpResult.filecontent#]", true)
		systemOutput("", true);
		return httpResult;
	}

	private string function createURI(string calledName, boolean contract=false){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}
}