component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	/* this only works via a browser, internalRequest doesn't support rest mappings */

	function beforeAll(){
		dumpRestMappings();
		//request.serverAdminPassword = "yourLocalAdminpasssword";
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV5323/"
		RestInitApplication( dir,            '/ldev5323root', false, request.serverAdminPassword );
		RestInitApplication( dir & "subdir", '/ldev5323sub',  false, request.serverAdminPassword );
		dumpRestMappings();
	}

	function afterAll(){
		return;
		dumpRestMappings();
		RestDeleteApplication( dir, request.serverAdminPassword );
		RestDeleteApplication( dir & "subdir", request.serverAdminPassword );
		dumpRestMappings();
	}

	private function dumpRestMappings(){
		return;
		systemOutput("---getRestMappings---", true);
		var mappings=getPageContext().getConfig().getRestMappings();
		loop array=mappings item="m" {
			systemOutput( m, true );
		}

		// this only returns system mappings
		/*
		var mappings = [];
		action="getRestMappings"
			type="server"
			password="#request.serverAdminPassword#"
			returnVariable="mappings";
		
		if ( isNull( local.mappings ) ) {
			systemOutput( "none", true ); 
			return;
		}
		loop array=mappings item="m" {
			systemOutput( m, true);
		}
		*/
	}

	function run( testResults, testBox ){
		describe( "LDEV-5323", function(){

			it( "check rest component has the correct application scope", function(){
				var result = test(path="/rest/ldev5323root/ldev5323root/getApplicationName");
				expect( trim( result.filecontent ) ).toBe( '"applicationName:ldev5323"' );
			});

			it( "check rest component has the correct application scope, sub dir", function(){
				var result = test(path="/rest/ldev5323sub/ldev5323sub/getApplicationName");
				expect( trim( result.filecontent ) ).toBe( '"applicationName:ldev5323"' );
			});

		} );
	}

	private function test(path, args={}){
		var jsr223 = (cgi.request_url eq "http://localhost/index.cfm")
		/*
		var uri = createURI("LDEV5323", !jsr223);
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
			http method="get" url="#webUrl#" result="httpResult"{
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
		return httpResult;
	}

	private string function createURI(string calledName, boolean contract=false){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}
}