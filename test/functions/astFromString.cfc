component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for astFromString", body = function() {

			it( title = 'Test basic variable assignment parsing', body = function( currentSpec ) {
				var result = astFromString("<cfset x = 1>");
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
			});

			it( title = 'Test AstUtil.astFromString method', body = function( currentSpec ) {
				var astUtil = new lucee.runtime.util.AstUtil();
				var result = astUtil.astFromString("<cfset test = 'util'>");
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
			});


			it( title = 'Test basic variable assignment parsing', body = function( currentSpec ) {
				var result = astFromString("Susi");
				assertEquals(
					'{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":4,"offset":4},"type":"Program","body":[{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":4,"offset":4},"type":"ExpressionStatement","expression":{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":4,"offset":4},"type":"StringLiteral","value":"Susi"}}]}', 
					serializeJSON(var:result,compact:true)
					);
			});

		});
	}
}