component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults, testBox ) {
		describe("LDEV-5442 Testcase for getApplicationSettings()", function() {

			it( title="getApplicationSettings()", body=function( currentSpec ) {
				var as = getApplicationSettings(onlySupported=true);
				expect( as ).toHaveKey("listenerMode");
				expect( as ).toHaveKey("listenerType");

				expect( as.listenerMode ).toBe( "curr2root" );
				expect( as.listenerType ).toBe( "modern" );
			});
		});
	}

}