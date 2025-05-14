/**
 * TestBox test suite for the testArgs variadic function
 */
component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {
		
		describe("TestArgs Variadic Function Tests", function() {
			
			it("returns an empty array when no arguments are passed", function() {
				var result = testArgs();
				expect(result).toBeArray();
				expect(result).toHaveLength(0);
			});
			
			it("returns an array with a single argument when one argument is passed", function() {
				var result = testArgs("one");
				expect(result).toBeArray();
				expect(result).toHaveLength(1);
				expect(result[1]).toBe("one");
			});
			
			it("returns an array with two arguments when two arguments are passed", function() {
				var result = testArgs("one", "two");
				expect(result).toBeArray();
				expect(result).toHaveLength(2);
				expect(result[1]).toBe("one");
				expect(result[2]).toBe("two");
			});
			
			it("returns an array with three arguments when three arguments are passed", function() {
				var result = testArgs("one", "two", "three");
				expect(result).toBeArray();
				expect(result).toHaveLength(3);
				expect(result[1]).toBe("one");
				expect(result[2]).toBe("two");
				expect(result[3]).toBe("three");
			});
			
			it("returns an array with four arguments when four arguments are passed", function() {
				var result = testArgs("one", "two", "three", "four");
				expect(result).toBeArray();
				expect(result).toHaveLength(4);
				expect(result[1]).toBe("one");
				expect(result[2]).toBe("two");
				expect(result[3]).toBe("three");
				expect(result[4]).toBe("four");
			});
			
			it("works with different data types as arguments", function() {
				var result = testArgs(1, "two", true, {name: "test"});
				expect(result).toBeArray();
				expect(result).toHaveLength(4);
				expect(result[1]).toBe(1);
				expect(result[2]).toBe("two");
				expect(result[3]).toBe(true);
				expect(result[4]).toBeStruct();
				expect(result[4].name).toBe("test");
			});
			
		});

		// This section specifically tests the implementation approach used
		describe("Implementation Specific Tests", function() {
			
			it("should use the ternary operator for empty vs non-empty arrays", function() {
				// Note: this test is primarily demonstrative since we can't easily inspect
				// the implementation details through normal testing
				var result1 = testArgs();
				var result2 = testArgs("one");
				
				expect(result1).toBeArray();
				expect(result1).toHaveLength(0);
				expect(result2).toBeArray();
				expect(result2).toHaveLength(1);
			});
			
		});
		
		// Edge case tests
		describe("Edge Cases", function() {
			
			it("handles null arguments correctly", function() {
				var result = testArgs(nullValue());
				expect(result).toBeArray();
				expect(result).toHaveLength(0);
			});
			
			
			
		});
		
	}

	private array function testArgs( /* .... */ ) {
		return arrayIsDefined( arguments, 1 )
			? arraySlice( arguments, 1 )
			: []
		;
	}
}