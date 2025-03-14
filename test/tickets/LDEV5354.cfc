component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){

		describe( "LDEV-5354", function(){

			// Original test cases
			it( "test with zero width space in front", function(){
				var str = chr(8203) & "17" ; 
				var num = str * 1; // Implicit conversion will use BigDecimal in 6.2
				expect( num ).toBe( 17 );
			});
			it( "test with zero width space after", function(){
				var str = "17" & chr(8203); 
				var num = str * 1; // Implicit conversion will use BigDecimal in 6.2
				expect( num ).toBe( 17 );
			});
			
			// Standard whitespace characters
			it( "test with space in front", function(){
				var str = " 17"; 
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with space after", function(){
				var str = "17 "; 
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with tab in front", function(){
				var str = chr(9) & "17"; 
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with tab after", function(){
				var str = "17" & chr(9); 
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with newline in front", function(){
				var str = chr(10) & "17"; 
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with newline after", function(){
				var str = "17" & chr(10); 
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with carriage return in front", function(){
				var str = chr(13) & "17"; 
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with carriage return after", function(){
				var str = "17" & chr(13); 
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			
			// Special Unicode whitespace characters
			it( "test with non-breaking space in front", function(){
				var str = chr(160) & "17"; // Non-breaking space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with non-breaking space after", function(){
				var str = "17" & chr(160); // Non-breaking space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with em space in front", function(){
				var str = chr(8195) & "17"; // Em space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with em space after", function(){
				var str = "17" & chr(8195); // Em space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with thin space in front", function(){
				var str = chr(8201) & "17"; // Thin space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with thin space after", function(){
				var str = "17" & chr(8201); // Thin space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with hair space in front", function(){
				var str = chr(8202) & "17"; // Hair space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with hair space after", function(){
				var str = "17" & chr(8202); // Hair space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			
			// Line/paragraph separators
			it( "test with line separator in front", function(){
				var str = chr(8232) & "17"; // Line separator
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with line separator after", function(){
				var str = "17" & chr(8232); // Line separator
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with paragraph separator in front", function(){
				var str = chr(8233) & "17"; // Paragraph separator
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with paragraph separator after", function(){
				var str = "17" & chr(8233); // Paragraph separator
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			
			// Multiple whitespace characters
			it( "test with multiple different whitespace characters in front", function(){
				var str = chr(9) & chr(32) & chr(160) & chr(8203) & "17"; // Tab, space, non-breaking space, zero-width space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with multiple different whitespace characters after", function(){
				var str = "17" & chr(8203) & chr(160) & chr(32) & chr(9); // Zero-width space, non-breaking space, space, tab
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with whitespace characters on both sides", function(){
				var str = chr(8203) & chr(160) & "17" & chr(32) & chr(8201); // Zero-width space, non-breaking space, space, thin space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			
			// Control characters that are treated as whitespace in some contexts
			it( "test with vertical tab in front", function(){
				var str = chr(11) & "17"; // Vertical tab
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with form feed in front", function(){
				var str = chr(12) & "17"; // Form feed
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			
			// Ideographic space (common in CJK text)
			it( "test with ideographic space in front", function(){
				var str = chr(12288) & "17"; // Ideographic space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
			it( "test with ideographic space after", function(){
				var str = "17" & chr(12288); // Ideographic space
				var num = str * 1;
				expect( num ).toBe( 17 );
			});
		});
	}
}