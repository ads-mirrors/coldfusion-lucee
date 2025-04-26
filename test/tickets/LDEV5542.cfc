component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" skip=true {

	function beforeAll(){
		
		//variables.testUrl = "http://127.0.0.1:7888/redirect2.cfm?method=#method#&version=#server.lucee.version#" // redirects and echos back json
	}

	function run( testResults, testBox ){

		describe( "LDEV-5542 cfhttp redirect='lax' support", function(){

			// Original test cases
			it( "test http redirects - STANDARD - return filecontent from GET only", function(){
				var methods = [ "GET", "POST", "PUT", "PATCH", "DELETE" ];

				var results = methods.map(
					( method ) => {

						http
							result = "local.httpResponse"
							method = method
							url = variables.testUrl
							redirect = "yes"
						;
						return httpResponse.fileContent;
					}
				);

				var ok = { 1 : true }; // GET

				arrayEach( results, function( el, idx){
					expect( isJson( el ) ).toBe( structKeyExists( ok, idx ), el );
				});

			});

			it( "test http redirects - LAX - return filecontent from GET, POST and DELTE", function(){
				var methods = [ "GET", "POST", "PUT", "PATCH", "DELETE" ];

				var results = methods.map(
					( method ) => {

						http
							result = "local.httpResponse"
							method = method
							url = variables.testUrl
							redirect = "lax";
						return httpResponse.fileContent;
					}
				);

				debug(results)

				var ok = { 1 : true, 2: true, 5: true }; // GET, POST, DELETE

				arrayEach( results, function( el, idx){
					expect( isJson( el ) ).toBe( structKeyExists( ok, idx ), el );
				});

			});

			
			it( "test http redirects - invalid value for redirect should throw", function(){
				expect(function(){
					http
						result = "local.httpResponse"
						method = "GET"
						url = variables.testUrl
						redirect = "laxer";
				}).toThrow()
			});

		});
	}
}