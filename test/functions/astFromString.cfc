component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for astFromString", body = function() {

			it( title = 'Test basic variable assignment parsing', body = function( currentSpec ) {
				var result = astFromString("<cfset x = 1>");
				assertEquals("BlockStatement", result.type);
				assertTrue(arrayLen(result.body) > 0);
			});

			it( title = 'Test AstUtil.astFromString method', body = function( currentSpec ) {
				var astUtil = new lucee.runtime.util.AstUtil();
				var result = astUtil.astFromString("<cfset test = 'util'>");
				assertEquals("BlockStatement", result.type);
				assertTrue(arrayLen(result.body) > 0);
			});


		});
	}
}