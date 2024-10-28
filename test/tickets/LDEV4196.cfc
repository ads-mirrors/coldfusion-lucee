component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4196", function() {
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
            });
			afterEach( function(){
                setTimeZone(variables.startingTZ?:"UTC");
            });
			it( title="parseDateTime() with format 'iso'", body=function( currentSpec ) {
				var d = dateTimeFormat("2022/01/02 03:45:05.666", "iso");
				expect(toString(parseDateTime(d, "iso"))).toBe("{ts '2022-01-02 03:45:05'}");
			});
		});
	}
	
}