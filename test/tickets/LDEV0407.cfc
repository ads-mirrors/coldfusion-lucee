<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function beforeAll() {
			variables.httpbin = getTestService("httpbin");
		}

		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-407", function() {
				it(title="checking http() function with timeout attribute on request", body = function( currentSpec ) {
					if ( structCount( variables.httpbin ) == 0 ){
						return;
					}
					try {
						var httpResponse = new http()
						.setUrl("http://#variables.httpbin.server#:#variables.httpbin.port#/delay/3")
						.setTimeout(1)
						.setThrowOnError(true)
						.send()
						.getPrefix();
					} catch ( any e){
						var httpResponse = e.message;
						if(httpResponse=="503 Service Temporarily Unavailable") return;
						if(httpResponse!="408 Request Time-out") rethrow;
					} 
					expect(httpResponse).toBe("408 Request Time-out");
				});

				it(title="checking cfhttp tag with timeout attribute on request", body = function( currentSpec ) {
					if ( structCount( variables.httpbin ) == 0 ){
						return;
					}
					var httpResponse = cfhttptag();
					if(httpResponse=="503 Service Temporarily Unavailable") return;
					expect(httpResponse).toBe("408 Request Time-out");
				});
			});
		}
	</cfscript>


	
	
	<cffunction name="cfhttptag" access="private" returntype="Any">
		<cfset result = "">
		<cftry>
			<cfhttp url="http://#variables.httpbin.server#:#variables.httpbin.port#/delay/3" throwonerror="true" timeout="1">
			</cfhttp>
			<cfcatch type="any">
				<cfset result = cfcatch.message>
			</cfcatch>
		</cftry>
		<cfreturn result>
	</cffunction>
</cfcomponent>
