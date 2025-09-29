component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		// Set up test artifact directory using current template path
		variables.testDir = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV5836";
		variables.debug = true;

		// Clean up previous test artifacts before running tests
		if (directoryExists(variables.testDir)) {
			var files = directoryList(variables.testDir, false, "name", "*.json");
			for (var file in files) {
				fileDelete(variables.testDir & "/" & file);
			}
		} else {
			directoryCreate(variables.testDir);
		}
	};

	// Clean up after all tests (disabled during dev)
	function xafterAll() {
		// only Remove ast json files from test artifact directory
		if (directoryExists(variables.testDir)) {
			var astFiles = directoryList(variables.testDir, false, "name", "*.json");
			for (var file in astFiles) {
				fileDelete(variables.testDir & "/" & file);
			}			
		}
	}

	function run(testResults, testBox) {
		describe("LDEV-5836 - AST should distinguish between BIFs and UDFs", function() {

			
			it("should mark built-in functions with isBuiltIn=true in AST", function() {
				// Use static test artifact file
				var testFile = variables.testDir & "/test-bifs.cfm";

				var ast = astFromPath(testFile);

				// Write the full AST to JSON file
				var astJson = serializeJSON(ast);
				fileWrite(variables.testDir & "/full-ast.json", astJson);

				// Debug: output the full AST to see its structure
				logger("=== Full AST Structure ===");
				logger("AST written to: " & variables.testDir & "/full-ast.json");
				logger(astJson);

				// Find all CallExpression nodes in the AST
				var callExpressions = findNodes(ast, "CallExpression");

				// Write found CallExpressions to separate file
				var callExpressionsJson = serializeJSON(callExpressions);
				fileWrite(variables.testDir & "/call-expressions.json", callExpressionsJson);

				// Debug: output found call expressions
				logger("=== Found CallExpressions ===");
				logger("Total found: " & callExpressions.len());
				logger("CallExpressions written to: " & variables.testDir & "/call-expressions.json");

				// Write individual call expression details
				for (var i = 1; i <= callExpressions.len(); i++) {
					var expr = callExpressions[i];
					var calleeName = (structKeyExists(expr, "callee") && structKeyExists(expr.callee, "name")) ? expr.callee.name : "unknown";
					logger("CallExpression ##" & i & ":");
					logger("  Callee name: " & calleeName);
					logger("  isBuiltIn: " & (expr.isBuiltIn ?: "not set"));

					// Write each call expression to its own file
					fileWrite(variables.testDir & "/call-expression-" & i & "-" & calleeName & ".json", serializeJSON(expr));
				}

				// We should have 4 CallExpression nodes
				expect(callExpressions.len()).toBe(4, "Should find 4 CallExpression nodes");

				// First three should be built-in functions
				expect(callExpressions[1].isBuiltIn ?: false).toBe(true, "len() should be marked as built-in");
				expect(callExpressions[2].isBuiltIn ?: false).toBe(true, "ucase() should be marked as built-in");
				expect(callExpressions[3].isBuiltIn ?: false).toBe(true, "now() should be marked as built-in");

				// Last one should NOT be marked as built-in (it's a UDF)
				expect(callExpressions[4].isBuiltIn ?: false).toBe(false, "myFunction() should not be marked as built-in");
			});

			it("should handle member function calls correctly", function() {
				// Use static test artifact file
				var testFile = variables.testDir & "/test-member-functions.cfm";

				var ast = astFromPath(testFile);

				// Write the AST to JSON file
				fileWrite(variables.testDir & "/member-functions-ast.json", serializeJSON(ast));

				var callExpressions = findNodes(ast, "CallExpression");

				// Debug output
				logger("=== Member Function Test ===");
				logger("Found " & callExpressions.len() & " CallExpressions");
				fileWrite(variables.testDir & "/member-functions-call-expressions.json", serializeJSON(callExpressions));

				// Member functions of built-in types should also be marked appropriately
				// Note: This test might need adjustment based on how member functions are represented
				expect(callExpressions.len()).toBeGT(0);
			});

			it("should not mark component method calls as built-in", function() {
				// Use static test artifact file
				var testFile = variables.testDir & "/test-component-methods.cfc";

				var ast = astFromPath(testFile);

				// Write the AST to JSON file
				fileWrite(variables.testDir & "/component-methods-ast.json", serializeJSON(ast));

				var callExpressions = findNodes(ast, "CallExpression");

				// Debug output
				logger("=== Component Methods Test ===");
				logger("Found " & callExpressions.len() & " CallExpressions");
				fileWrite(variables.testDir & "/component-methods-call-expressions.json", serializeJSON(callExpressions));

				// Component methods should not be marked as built-in
				for (var expr in callExpressions) {
					if (structKeyExists(expr, "callee") && structKeyExists(expr.callee, "name")) {
						if (expr.callee.name == "init" || expr.callee.name == "someMethod") {
							expect(expr.isBuiltIn ?: false).toBe(false);
						}
					}
				}
			});
		});
	}

	/**
	 * Helper function for debug logging
	 */
	private void function logger(required string message) {
		if (variables.debug ?: false) {
			systemOutput(arguments.message, true);
		}
	}

	/**
	 * Helper function to recursively find all nodes of a specific type in the AST
	 */
	private array function findNodes(required struct node, required string type) {
		var results = [];

		if (structKeyExists(node, "type") && node.type == arguments.type) {
			results.append(node);
		}

		// Recursively search through the node's properties
		for (var key in node) {
			var value = node[key];

			if (isStruct(value)) {
				results.append(findNodes(value, arguments.type), true);
			} else if (isArray(value)) {
				for (var item in value) {
					if (isStruct(item)) {
						results.append(findNodes(item, arguments.type), true);
					}
				}
			}
		}

		return results;
	}
}