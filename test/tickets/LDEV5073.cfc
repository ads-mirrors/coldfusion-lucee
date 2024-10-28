component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "Test LDEV-5073", function(){

			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
            });
			afterEach( function(){
                setTimeZone(variables.startingTZ?:"UTC");
            });
			it( "test EEE, dd MMM yyyy HH:mm:ss Z", function(){				
				var result=isDate(ParseDateTime("Wed, 14 Aug 2024 14:50:51 +0000"));
				expect( result ).toBeTrue();
			});

		} );
	}

}