component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for acos()", body=function() {
			it(title="checking acos(1) function", body = function( currentSpec ) {
				assertEquals(0,acos(1));
			});

			it(title="checking acos(0.7) function", body = function( currentSpec ) {
				if ( getApplicationSettings().preciseMath )
					assertEquals("0.7953988301841436", toString(acos(0.7)));
				else 
					assertEquals("0.795398830184", toString(acos(0.7)));
				
			});

			it(title="checking acos() function invalid range", body = function( currentSpec ) {
				try{
					assertEquals(1,tostring(acos(1.7)));
					fail("must throw:1.7 must be within range: ( -1 : 1 )");
				}
				catch(local.exp){}
			});
		});
	}
}