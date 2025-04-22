/*
	this test can be run mapped in a browser, toggling between using internal request or cfhttp
	http://127.0.0.1:9888/test/tickets/ldev5530.cfc?internalRequest=false would use cfhttp
*/

component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {
	function run( testResults, testBox ) {

		describe( "test case with onCFCRequest http status responses with a missing.cfc", function() {

			it( "test missing cfc with onCFCRequest - no method", function() {
				var result = testCfc("missing.cfc", {});
				expect ( result.status ).toBe( 404, result.message );
			});

			it( "test missing cfc with onCFCRequest, with method", function() {
				var result = testCfc("missing.cfc", { method="test"});
				expect (result.status).toBe( 404, result.message ); // throw 200
			});

			it( "test missing cfc with onCFCRequest with invalid wddx argumentCollection", function() {
				var wddx = "";
				wddx action="cfml2wddx" input="#queryNew('id,name')#" output="WDDX";
				var result = testCfc("missing.cfc", {
					method:"test",
					argumentCollection: wddx
				});
				expect ( result.status ).toBe( 404, result.message ); // throws 500
			});

			it( "test missing cfc with onCFCRequest with json argumentCollection", function() {
				var jsonObj = {json:true };
				var result = testCfc("missing.cfc", {
					method:"test",
					argumentCollection: jsonObj.toJson()
				});
				expect ( result.status ).toBe( 404, result.message ); // throws 500
			});

			it( "test missing cfc with onCFCRequest - no method", function() {
				var result = testCfc("missing.cfc", {});
				expect ( result.status ).toBe( 404, result.message );
			});

		});

		describe( "test case with onCFCRequest http status responses with with an existing cfc", function() {

			it( "test cfc with onCFCRequest, no method", function() {
				var result = testCfc("testLDEV5530.cfc", {});
				expect( result.status ).toBe( 406, result.message );
			});

			it( "test cfc with onCFCRequest with non remote method", function() {
				var result = testCfc("testLDEV5530.cfc", {method:"test"});
				expect( result.status ).toBe( 406, result.message ); // method isn't remote
			});

			it( "test cfc with onCFCRequest with invalid wddx argumentCollection, existing non remote method", function() {
				var wddx = "";
				wddx action="cfml2wddx" input="#queryNew('id,name')#" output="WDDX";
				var result = testCfc("testLDEV5530.cfc", {
					method:"test",
					argumentCollection: wddx
				});
				expect( result.status ).notToBe( 406, result.message ); // method isn't remote, but error
			});

			it( "test cfc with onCFCRequest after request with argumentCollection", function() {
				var result = testCfc("testLDEV5530.cfc");
				expect( result.status ).notToBe( 406, result.message ); // method missing
			});
		} );

		describe( "test case with onCFCRequest http status responses with existing cfc - remote", function() {
			it( "test cfc with onCFCRequest with invalid wddx argumentCollection, remote method", function() {
				var wddx = "";
				wddx action="cfml2wddx" input="#queryNew('id,name')#" output="WDDX";
				var result = testCfc("testLDEV5530.cfc", {
					method: "testRemote",
					argumentCollection: wddx
				});
				expect( result.status ).toBe( 500, result.message ); // method is remote, but error
			});

			it( "test cfc with onCFCRequest with valid json argumentCollection, non remote method", function() {
				var jsonObj = {json:true };
				var result = testCfc("testLDEV5530.cfc", {
					method:"test",
					argumentCollection: jsonObj.toJson()
				});
				expect ( result.status ).toBe( 406, result.message ); // throws 500
			});

			it( "test cfc with onCFCRequest with valid json argumentCollection, remote method", function() {
				var jsonObj = {json:true };
				var result = testCfc("testLDEV5530.cfc", {
					method:"testRemote",
					argumentCollection: jsonObj.toJson()
				});
				expect ( result.status ).toBe( 200, result.message ); // throws 500
			});

			it( "test cfc with onCFCRequest with invalid wddx argumentCollection, non existing method", function() {
				var wddx = "";
				wddx action="cfml2wddx" input="#queryNew('id,name')#" output="WDDX";
				var result = testCfc("testLDEV5530.cfc", {
					method: "missing",
					argumentCollection: wddx
				});
				expect( result.status ).toBe( 406, result.message ); // method isn't remote, but error
			});

		});
	}

	private function testCFC(template, struct formArgs={}){
		var uri = createURI("/LDEV5530");
		var result = call(
			template:"#uri#/#arguments.template#",
			method: "post",
			formArgs: formArgs
		);
		
		if (!structKeyExists(result, "message")){
			result.message = "no exception thrown";
		}
		
		debug(result);
		return result;
	}

	private function call(template, method="post", formArgs={}){
		var jsr223 = isJsr232();
		if ( jsr223 ){ 
			try {
				var result = internalRequest(
					template : arguments.template,
					method: arguments.method,
					form: arguments.formArgs 
				);
			} catch( e ){
				result = e;
				if (e.message contains "not found")
					result.status=404;
				else// if ( e.message contains error )
					result.status=500;
			}
			return result;
		} else {
			// running via a web browser, let's try http, which also supports jee sessions
			var hostIdx = find(cgi.script_name, cgi.request_url);
			if (hostIdx gt 0){
				var host = left(cgi.request_url, hostIdx-1);
				var webUrl = host & arguments.template;
				//systemOutput("could do http! testing via [#webUrl#]", true);
			} else {
				throw "failed to extract host [#hostIdx#] from cgi [#cgi.script_name#], [#cgi.request_url#]";
			}
			var httpResult = "";
			http method="#arguments.method#" url="#webUrl#" result="httpResult" {
				structEach(arguments.formArgs, function(k,v){
					httpparam name="#k#" value="#v#" type="form";
				});
			}

			httpResult.message = httpResult.fileContent;
			httpResult.status = httpResult.statusCode;
			structDelete(httpResult, "fileContent");

		}
		return httpResult;
	}

	private function isJsr232(){
		if ( structKeyExists( url, "internalRequest") )
			return url.internalRequest;
		return (cgi.request_url eq "http://localhost/index.cfm");
	}

	private string function createURI(string calledName, boolean contract=false){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}
}
