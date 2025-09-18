component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title = "LDEV-5806 Test for CFML Strict Equality (===) Behavior Issues", body = function() {

			it( title = 'len() function strict equality bug', body = function( currentSpec ) localmode=true {
				emptyString = "";
				lenResult = len(emptyString);
				
				expect( lenResult ).toBe( 0 );
				expect( lenResult == 0 ).toBeTrue();
				expect( lenResult === 0 ).toBeTrue();
				expect( getMetadata(lenResult).name ).toBe( "java.lang.Double" );
			});

			it( title = 'find() function strict equality bug', body = function( currentSpec ) localmode=true {
				text = "hello world";
				findResult = find("xyz", text);
				
				expect( findResult ).toBe( 0 );
				expect( findResult == 0 ).toBeTrue();
				
				// Check the actual return type
				var actualType = getMetadata(findResult).name;
				
				// BUG: strict equality fails due to type mismatch
				expect( findResult === 0 ).toBeTrue();
				
				// Document what type is actually returned
				systemOutput("find() returns: " & actualType, true);
			});

			it( title = 'arrayLen() function strict equality bug', body = function( currentSpec ) localmode=true {
				emptyArray = [];
				arrayLenResult = arrayLen(emptyArray);
				
				expect( arrayLenResult ).toBe( 0 );
				expect( arrayLenResult == 0 ).toBeTrue();
				expect( arrayLenResult === 0 ).toBeTrue();
				expect( getMetadata(arrayLenResult).name ).toBe( "java.lang.Integer" );
			});

			it( title = 'mod operator strict equality bug', body = function( currentSpec ) localmode=true {
				j = 10;
				interval = 5;
				modResult = j mod interval;
				
				expect( modResult ).toBe( 0 );
				expect( modResult == 0 ).toBeTrue();
				expect( modResult === 0 ).toBeTrue();
				expect( getMetadata(modResult).name ).toBe( "java.lang.Double" );
			});

			it( title = 'val() function and expression result strict equality bugs', body = function( currentSpec ) localmode=true {
				section1 = 1;          // Direct assignment
				section2 = 0 + 1;      // Expression result
				section3 = val("1");   // String conversion
				
				// Direct assignment works
				expect( section1 == 1 ).toBeTrue();
				expect( section1 === 1 ).toBeTrue();
				
				// Expression result fails strict equality
				expect( section2 == 1 ).toBeTrue();
				expect( section2 === 1 ).toBeTrue();
				
				// String conversion fails strict equality
				expect( section3 == 1 ).toBeTrue();
				expect( section3 === 1 ).toBeTrue();
			});

			it( title = 'increment (++) operator strict equality bug', body = function( currentSpec ) localmode=true {
				section = 1;
				section++; // Now section = 2, but as java.lang.Double
				
				expect( section ).toBe( 2 );
				expect( section == 2 ).toBeTrue();
				expect( section === 2 ).toBeTrue();
				
				// This simulates the original parsing bug
				var sectionProcessed = false;
				if (section === 2) {
					sectionProcessed = true;
				}
				expect( sectionProcessed ).toBeTrue();
				
				// This is the fix
				var sectionProcessedFixed = false;
				if (section == 2) {
					sectionProcessedFixed = true;
				}
				expect( sectionProcessedFixed ).toBeTrue();
			});

			it( title = 'find() function causing infinite loop with strict equality', body = function( currentSpec ) localmode=true {
				content = "line1" & chr(10) & "line2";
				newlinePos = find(chr(10), content);
				
				expect( newlinePos ).toBe( 6 );
				expect( newlinePos == 6 ).toBeTrue();
				expect( newlinePos === 6 ).toBeTrue();
				
				// Simulate the infinite loop condition that was fixed
				var wouldCauseInfiniteLoop = (newlinePos === 0); // This was the bug
				var correctCondition = (newlinePos == 0);       // This is the fix
				
				expect( wouldCauseInfiniteLoop ).toBeFalse();
				expect( correctCondition ).toBeFalse();
			});

			it( title = 'len() and arrayLen() empty detection failures', body = function( currentSpec ) localmode=true {
				line = "";
				lineLength = len(line);
				
				// Empty string detection
				expect( lineLength == 0 ).toBeTrue();
				expect( lineLength === 0 ).toBeTrue();
				
				// Empty array detection
				arr = [];
				arrLength = arrayLen(arr);
				
				expect( arrLength == 0 ).toBeTrue();
				expect( arrLength === 0 ).toBeTrue();
			});

			it( title = 'len() function type coercion vs strict equality comparison', body = function( currentSpec ) localmode=true {
				// Test various scenarios where regular equality provides proper coercion
				// but strict equality requires exact type matching
				
				stringZero = "0";
				intZero = 0;
				doubleZero = 0.0;
				functionZero = len("");
				
				// Regular equality works with type coercion
				expect( stringZero == intZero ).toBeTrue();
				expect( intZero == doubleZero ).toBeTrue();
				expect( intZero == functionZero ).toBeTrue();
				
				// Strict equality should work for CFML values regardless of Java implementation
				expect( stringZero === intZero ).toBeFalse();  // Different CFML types
				expect( intZero === doubleZero ).toBeTrue();   // Both CFML numbers
				expect( intZero === functionZero ).toBeTrue(); // Both CFML numbers
			});

		});
	}
}
