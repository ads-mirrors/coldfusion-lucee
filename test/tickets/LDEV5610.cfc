component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title="Test method matching and auto-casting", body=function() {

			// Setup POI component once for all tests
			var poi = new component javaSettings='{
					"maven": [
						"org.apache.poi:poi:5.4.1",
						"org.apache.poi:poi-ooxml:5.4.1",
						"org.apache.poi:poi-ooxml-full:5.4.1",
						"org.apache.xmlbeans:xmlbeans:5.3.0"
					]
				}' {

				import "org.apache.poi.xwpf.usermodel.*";

				function getPara() {
					var doc = New XWPFDocument();
					var para = doc.createParagraph();
					return para;
				}

			};

			it(title="call method which requires a BigInteger with a numeric, should match and auto cast", body = function( currentSpec ) {
				// This should auto-cast numeric to BigInteger
				poi.getPara().setNumID( 1 );
				expect(true).toBeTrue(); // Test passes if no exception thrown
			});

			it(title="call method which requires an int with a numeric, should match and auto cast", body = function( currentSpec ) {
				// This should auto-cast numeric to int
				poi.getPara().setSpacingAfter(1);
				expect(true).toBeTrue(); // Test passes if no exception thrown
			});

			it(title="call method which requires a double with a numeric, should match and auto cast", body = function( currentSpec ) {
				// This should auto-cast numeric to double
				poi.getPara().setSpacingBetween(1);
				expect(true).toBeTrue(); // Test passes if no exception thrown
			});

			it(title="call Date constructor which requires a long with a numeric, should match and auto cast", body = function( currentSpec ) {
				// Simulate the git commit time scenario
				var commitTime = 1642780800; // Unix timestamp
				var date = new Date( commitTime * 1000 );
				expect(isDate(date)).toBeTrue();
			});

			it(title="comprehensive method matching test with all numeric types", body = function( currentSpec ) {
				var para = poi.getPara();
				
				// Test int argument - should auto-cast
				para.setSpacingAfter(1);
				
				// Test double argument - should auto-cast
				para.setSpacingBetween(1.5);
				
				// Test BigInteger argument - should auto-cast (this is the failing case)
				para.setNumID( 1 );
				
				// Test with different numeric values
				para.setNumID( 42 );
				para.setSpacingAfter(100);
				para.setSpacingBetween(2.0);
				
				expect(true).toBeTrue(); // Test passes if no exception thrown
			});

			// Test case to verify explicit casting still works
			it(title="explicit javacast should continue to work", body = function( currentSpec ) {
				var para = poi.getPara();
				
				// Explicit casting should continue to work
				para.setNumID( javacast("BigInteger", 1 ) );
				
				// Test with various explicit casts
				para.setSpacingAfter( javacast("int", 10) );
				para.setSpacingBetween( javacast("double", 1.5) );
				
				// Test Date with explicit long cast
				var commitTime = 1642780800;
				var date = new Date( javacast("long", commitTime * 1000) );
				
				expect(isDate(date)).toBeTrue();
			});

		});
	}

}