component extends="org.lucee.cfml.test.LuceeTestCase"{
		
	function beforeAll(){
		variables.preciseMath = getApplicationSettings().preciseMath;
	};

	function afterAll(){
		application action="update" preciseMath=variables.preciseMath;
	};

	function run( testResults , testBox ) {
		describe( title="Test suite for ASin()", body=function() {
			beforeEach( function(){
				application action="update" preciseMath=variables.preciseMath;
			});

			afterEach( function(){
				application action="update" preciseMath=variables.preciseMath;
			});
			it(title="Checking ASin() function", body = function( currentSpec ) {
				application action="update" preciseMath=true;
				assertEquals("0.3046926540153975",tostring( asin(0.3) ));
				application action="update" preciseMath=false;
				assertEquals("0.304692654015",tostring( asin(0.3) ));
			});
			it(title="Checking ASin() function invaid input", body = function( currentSpec ) {
				try{
					assertEquals("0",tostring(asin(1.3)));
					fail("must throw:1.3 must be within range: ( -1 : 1 )");
				} catch(any e){}
			});
		});
	}
}