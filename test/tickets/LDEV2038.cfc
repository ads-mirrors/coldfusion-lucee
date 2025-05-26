component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "Test suite for LDEV2352", function(){
			it( title = "interface method with any allows return type struct", body = function( currentSpec ){
				var obj = new LDEV2038.ldev2038_struct();
				var result = obj.test();
				expect ( result ).toBeStruct();
			});

			it( title = "interface method with any allows return type cfc", body = function( currentSpec ){
				var obj = new LDEV2038.ldev2038_obj();
				var result = obj.test();
				expect ( result ).toBeComponent();
			});
		});
	}

}