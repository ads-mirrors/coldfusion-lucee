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
			});
		});

		describe( title="Test contructor matching java.util.Date and numeric values", body=function() {

			var dateObj = new component {
				import java.util.Date;
				function createDateFromNum( num ) {
					return new Date( arguments.num );
				}
			};

			it(title="java.util.date should handle long", body = function( currentSpec ) {
				var n = now();
				var epoch = dateTimeFormat( n, "epochms" );
				expect( dateObj.createDateFromNum( javacast("long", epoch ) ) ).toBe( n ); // long		
			});

			xit(title="java.util.date should match long constructor with int argument", body = function( currentSpec ) {
				var n = now();
				var epoch = dateTimeFormat( n, "epochms" );
				expect( dateObj.createDateFromNum( javacast("int", epoch ) ) ).toBe( n ); // int
			});

			xit(title="java.util.Date should accept string date", body = function( currentSpec ) {
				var n = now();
				var epoch = dateTimeFormat( n );
				expect( dateObj.createDateFromNum( epoch ) ).toBe( n ); // java.lang.string
			});
			
			xit(title="java.util.Date match Double arg to constructor with Long", body = function( currentSpec ) {
				var n = now();
				var epoch = dateTimeFormat( n, "epochms" ) * 1; // java.lang.Double
				expect( dateObj.createDateFromNum( epoch ) ).toBe( n );
			});

		});
	}

}