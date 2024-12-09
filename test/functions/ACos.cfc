component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.preciseMath = getApplicationSettings().preciseMath;
	};

	function afterAll(){
		application action="update" preciseMath=variables.preciseMath;
	};
	function run( testResults , testBox ) {
		describe( title="Test suite for acos()", body=function() {

			beforeEach( function(){
				application action="update" preciseMath=variables.preciseMath;
			});

			afterEach( function(){
				application action="update" preciseMath=variables.preciseMath;
			});

			it(title="checking acos(1) function", body = function( currentSpec ) {
				assertEquals(0,acos(1));
			});

			it(title="checking acos(0.7) function", body = function( currentSpec ) {
				application action="update" preciseMath=true;
				assertEquals("0.7953988301841436", toString(acos(0.7)));
				application action="update" preciseMath=false;
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