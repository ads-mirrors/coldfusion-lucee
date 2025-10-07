component extends="org.lucee.cfml.test.LuceeTestCase" labels="date" skip=true {

	function run( testResults, testBox ) {
		describe( "Test case for LDEV-5842: Handle malformed HTTP date headers with single-digit hours", function() {

			it( title="parseDateTime should handle RFC 1123 dates with proper two-digit hours", body=function( currentSpec ) {
				var dateStr = "Sat, 13 Sep 2025 09:29:31 GMT";
				var result = parseDateTime( dateStr );

				expect( isDate( result ) ).toBeTrue();
				expect( dateFormat( result, "yyyy-mm-dd" ) ).toBe( "2025-09-13" );
				expect( hour( result ) ).toBe( 9 );
			});

			it( title="parseDateTime should handle malformed HTTP headers with single-digit hours", body=function( currentSpec ) {
				// Some HTTP servers return malformed Last-Modified headers with single-digit hours
				// Example: "Sat, 13 Sep 2025  9:29:31 GMT" (note the extra space before 9)
				var dateStr = "Sat, 13 Sep 2025  9:29:31 GMT";
				var result = parseDateTime( dateStr );

				expect( isDate( result ) ).toBeTrue();
				expect( dateFormat( result, "yyyy-mm-dd" ) ).toBe( "2025-09-13" );
				expect( hour( result ) ).toBe( 9 );
				expect( minute( result ) ).toBe( 29 );
				expect( second( result ) ).toBe( 31 );
			});

			it( title="parseDateTime should handle single-digit hours without extra space", body=function( currentSpec ) {
				var dateStr = "Fri, 05 Jan 2024 8:15:00 GMT";
				var result = parseDateTime( dateStr );

				expect( isDate( result ) ).toBeTrue();
				expect( dateFormat( result, "yyyy-mm-dd" ) ).toBe( "2024-01-05" );
				expect( hour( result ) ).toBe( 8 );
			});

			it( title="parseDateTime should handle standard RFC 1123 dates", body=function( currentSpec ) {
				var dateStr = "Thu, 20 Jun 2024 20:35:36 GMT";
				var result = parseDateTime( dateStr );

				expect( isDate( result ) ).toBeTrue();
				expect( dateFormat( result, "yyyy-mm-dd" ) ).toBe( "2024-06-20" );
				expect( hour( result ) ).toBe( 20 );
			});

		});
	}

}
