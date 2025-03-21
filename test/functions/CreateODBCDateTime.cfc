component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for CreateODBCDateTime()", body=function() {
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
            });
			afterEach( function(){
                setTimeZone(variables.startingTZ?:"UTC");
            });
			it(title="checking CreateODBCDateTime() function", body = function( currentSpec ) {
				var fixDate=CreateDateTime(2001, 11, 1, 4, 10, 4);
				assertEquals("{ts '2001-11-01 04:10:04'}","#CreateODBCDateTime(fixDate)#");
			});
		});
	}
}
