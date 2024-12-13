component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll(){
		variables.preciseMath = getApplicationSettings().preciseMath;
	};

	function afterAll(){
		application action="update" preciseMath=variables.preciseMath;
	};

	function run( testResults, testBox ){
		describe( "LDEV-5198 regression", function(){

			it( "bit preciseMath=false", function(){
				application action="update" preciseMath=false;
				var t= 138;
				var num = 9103313;
				expect ( bitSHLN( t, 24) ).toBe( 3305111552  ); 
				expect ( bitOr( num, 3305111552) ).toBe( 3314214865  ); // returns 3314214912
			});

			it( "bit preciseMath=true", function(){
				application action="update" preciseMath=true;
				var t= 138;
				var num = 9103313;
				expect ( bitSHLN( t, 24) ).toBe( 3305111552  ); 
				expect ( bitOr( num, 3305111552) ).toBe( 3314214865  ); // returns 3314214912
			});
		} );
	}

}
