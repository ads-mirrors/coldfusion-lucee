component extends="org.lucee.cfml.test.LuceeTestCase" {

	function afterAll(){
		structDelete( request, "ldev5187");
		structDelete( server, "ldev5187_applicationName");
	};

	function run( testResults , testBox ) {
		describe( title="Testcase for LDEV-5187", body = function() {

			it(title = "check query listener before exception", body = function( currentSpec ) {
				var q = queryNew("a");
					expect(function(){
					query name="local.qry" dbtype="query" 
						listener={
							before=function (caller,args) {
								//systemOutput("before listener throw", true);
								throw "throw in before listener";
								return arguments;
							}
						} {
						echo("select a from q");
					};
				}).toThrow("application","","throw in before listener");
			});

			it(title = "check query listener after exception", body = function( currentSpec ) {
				var q = queryNew("a");
				expect(function(){
					query name="local.qry" dbtype="query" 
						listener={
							after=function (caller,args) {
								//systemOutput("after listener throw", true);
								throw "throw in after listener";
								return arguments;
							}
						} {
							echo("select a from q");
					};
				}).toThrow("application","","throw in after listener");
			});

			it(title = "check query listener after exception, with error listener", body = function( currentSpec ) {
				var q = queryNew("a");
				expect(function(){
					query name="local.qry" dbtype="query" 
						listener={
							after=function (caller,args) {
								//systemOutput("before listener throw", true);
								throw "throw in after listener";
								return arguments;
							},
							error=function (caller,args) {
								//systemOutput("in error listener", true);
								throw "throw in error listener";
								return arguments;
							}
						} {
							echo("select a from q");
					};
				}).toThrow("application","","throw in error listener");
			});

			it(title = "check query error listener", body = function( currentSpec ) {
				var q = queryNew("a");
				expect(function(){
					query name="local.qry" dbtype="query" 
						listener={
							before=function (caller,args) {
								args.sql = "select I am going to error";
								return arguments;
							},
							error=function (caller,args) {
								//systemOutput("in error listener", true);
								throw "throw in error listener";
								return arguments;
							}
						} {
							echo("select a from q");
					};
				}).toThrow("application","","throw in error listener");
			});

			it(title = "check query listener scopes", body = function( currentSpec ) {
				var q = queryNew("a");
				application name="ldev5187";
				request.ldev5187 = true;
				application.ldev5187 = true;
				query name="local.qry" dbtype="query" 
					listener={
						before=function (caller,args) {
							if (!structKeyExists( request, "ldev5187" ) ) throw "missing request scope";
							if (!structKeyExists( application, "ldev5187" ) ) throw "missing application scope";
							//systemOutput("Good! before listener has scopes", true);
							return arguments;
						},
						after=function (caller,args) {
							if (!structKeyExists( request, "ldev5187" ) ) throw "missing request scope";
							if (!structKeyExists( application, "ldev5187" ) ) throw "missing application scope";
							//systemOutput("Good! after listener has scopes", true);
							return arguments;
						}
					} {
						echo("select a from q");
				};
				expect(qry.a).toBe( "" );
				structDelete(request, "ldev5187");
				structDelete(application, "ldev5187");
			});

			it(title = "check query error listener scopes, async=false", body = function( currentSpec ) {
				var q = queryNew("a");
				application name="ldev5187";
				request.ldev5187 = true;
				application.ldev5187 = true;
				query name="local.qry" dbtype="query" 
					listener={
						error=function (caller,args) {
							if (!structKeyExists( request, "ldev5187" ) ) throw "missing request scope";
							if (!structKeyExists( application, "ldev5187" ) ) throw "missing application scope";
							//systemOutput("Good! error listener has scopes", true);
						}
					} {
						echo("select a from missing");
				};
				structDelete( request, "ldev5187" );
				structDelete( application, "ldev5187" );
			});

			it(title = "check query error listener scopes, async=true", skip=true, body = function( currentSpec ) {
				var q = queryNew("a");
				application name="ldev5187";
				// test will pass if application name is ""
				application name=""; // query listener gets the default name="" application scope
				// but this will cause the test to fail
				// application name="ldev5187"; 

				request.ldev5187 = false;
				application.ldev5187 = false;
				server.ldev5187_applicationName = "";
				query async=true name="local.qry" dbtype="query" 
					listener={
						error=function (caller,args) {
							//systemOutput("In async error listener has scopes", true);
							if (!structKeyExists( request, "ldev5187" ) ){
								//systemOutput("Bad! async error listener missing request scope", true);
								throw "missing request scope";
							}
							if (!structKeyExists( application, "ldev5187" ) ) {
								server.ldev5187_applicationName = getApplicationSettings().name;
								//systemOutput("Bad! async error listener missing correct application scope [#server.ldev5187_applicationName#]", true);
								throw "missing application scope";
							}
							//systemOutput("Good! async error listener has scopes", true);
							request.ldev5187 = true;
							application.ldev5187 = true;
						}
					} {
						echo("select a from missing");
				};
				sleep( 200 );
				expect( server.ldev5187_applicationName ).toBe( getApplicationSettings().name );
				// expect( request.ldev5187 ).toBeTrue(); request scope isn't propagated back, to be expected for async
				expect( application.ldev5187 ).toBeTrue();
				structDelete( request, "ldev5187");
				structDelete( application, "ldev5187");
				structDelete( server, "ldev5187_applicationName");
			});

		});
	}
}
