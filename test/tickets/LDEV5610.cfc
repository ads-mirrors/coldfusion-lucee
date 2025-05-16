component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) {
		describe( title="Test method matching", body=function() {

			it(title="call method which requires a bigDecimal with a numeric, should match and auto cast", body = function( currentSpec ) {
				var poi = new component javaSettings='{
						"maven": [
							"org.apache.poi:poi:5.4.1",
							"org.apache.poi:poi-ooxml:5.4.1",
							"org.apache.poi:poi-ooxml-full:5.4.1",
							"org.apache.xmlbeans:xmlbeans:5.3.0"
						]
					}' {

					import "org.apache.poi.xwpf.usermodel.*";

					function test() {
						var doc = New XWPFDocument();
						var para = doc.createParagraph();
						para.setNumID( 1 ); // crash
						//para.setNumID( javacast("BigInteger", 1 ) ); // works
						return true;
					}

				};

				expect(poi.test()).toBeTrue()
			});

	
		});
	}

}