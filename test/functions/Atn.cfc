component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for Atn()", body=function() {
			it(title="Checking Atn() function", body = function( currentSpec ) {
				if ( getApplicationSettings().preciseMath ){
					assertEquals( "0.2914567944778671",tostring(atn(0.3)));
					assertEquals( "0.9151007005533605",tostring(atn(1.3)));
					assertEquals("-1.5607966601082315",tostring(atn(-100)));
				} else {
					assertEquals( "0.291456794478",tostring(atn(0.3)));
					assertEquals( "0.915100700553",tostring(atn(1.3)));
					assertEquals("-1.560796660108",tostring(atn(-100)));
				}
				
			});
		});
	}
}