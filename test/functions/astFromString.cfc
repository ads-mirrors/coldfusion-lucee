component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for astFromString", body = function() {

			it( title = 'Test basic variable assignment parsing', body = function( currentSpec ) {
				var result = astFromString("<cfset x = 1>");
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
			});


		});
	}
}