component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1307", body=function() {
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
            });
			afterEach( function(){
                setTimeZone(variables.startingTZ?:"UTC");
            });
			it(title="checking dateTimeFormat member function", body = function( currentSpec ) {
				var result = now().dateTimeFormat("HH:NN:SS");
				expect(result).toBeTypeof('date');
			});
		});
	}
}