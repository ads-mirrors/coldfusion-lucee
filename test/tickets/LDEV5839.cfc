component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" skip=true {

	function beforeAll() {
		variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5839";
		variables.debug = false;
		logger( "Test artifacts directory: " & variables.testDir );

		// Clean up previous test artifacts before running tests
		if ( directoryExists( variables.testDir ) ) {
			var files = directoryList( variables.testDir, false, "name", "*.json" );
			for ( var file in files ) {
				fileDelete( variables.testDir & "/" & file );
			}
		} else {
			directoryCreate( variables.testDir );
		}
	}

	function afterAll() {
		// Only remove ast json files from test artifact directory
		if ( !variables.debug && directoryExists( variables.testDir ) ) {
			var astFiles = directoryList( variables.testDir, false, "name", "*.json" );
			for ( var file in astFiles ) {
				fileDelete( variables.testDir & "/" & file );
			}
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5839 - astFromString() should parse .cfc components correctly", function() {

			it( "should parse component with function using astFromPath()", function() {
				// First test with astFromPath() - this should work correctly
				var testFile = variables.testDir & "/ldev5839-simple-component.cfc";

				var ast = astFromPath( testFile );

				// Write the full AST to JSON file for inspection
				var astJson = serializeJSON( ast );
				fileWrite( variables.testDir & "/astFromPath-result.json", astJson );

				logger( "=== astFromPath() Result ===" );
				logger( "AST written to: " & variables.testDir & "/astFromPath-result.json" );

				// Verify we get a Program with proper structure
				expect( ast.type ).toBe( "Program" );
				expect( ast.body.len() ).toBeGT( 0 );

				// The first element should be a component
				var firstNode = ast.body[ 1 ];
				logger( "First node type from astFromPath(): " & firstNode.type );

				// Should NOT be a StringLiteral
				if ( structKeyExists( firstNode, "expression" ) && isStruct( firstNode.expression ) ) {
					expect( firstNode.expression.type ).notToBe( "StringLiteral", "Component should not be parsed as a StringLiteral" );
				}
			});

			it( "should parse component with function using astFromString() - CURRENTLY FAILS", function() {
				// Now test with astFromString() - this currently treats component as StringLiteral
				var testFile = variables.testDir & "/ldev5839-simple-component.cfc";
				var cfcCode = fileRead( testFile );

				var ast = astFromString( cfcCode );

				// Write the full AST to JSON file for inspection
				var astJson = serializeJSON( ast );
				fileWrite( variables.testDir & "/astFromString-result.json", astJson );

				logger( "=== astFromString() Result ===" );
				logger( "AST written to: " & variables.testDir & "/astFromString-result.json" );
				logger( astJson );

				// Verify we get a Program
				expect( ast.type ).toBe( "Program" );
				expect( ast.body.len() ).toBeGT( 0 );

				// The first element should be a component, NOT an ExpressionStatement with a StringLiteral
				var firstNode = ast.body[ 1 ];
				logger( "First node type from astFromString(): " & firstNode.type );

				// This currently FAILS - the component is treated as a StringLiteral
				expect( firstNode.type ).notToBe( "ExpressionStatement", "Component should not be parsed as ExpressionStatement" );

				// If it IS an ExpressionStatement, it should not have a StringLiteral expression
				if ( firstNode.type == "ExpressionStatement" && structKeyExists( firstNode, "expression" ) ) {
					logger( "Expression type: " & firstNode.expression.type );
					expect( firstNode.expression.type ).notToBe( "StringLiteral", "Component should not be parsed as a StringLiteral" );
				}
			});

			it( "astFromString() and astFromPath() should produce equivalent AST structures", function() {
				var testFile = variables.testDir & "/ldev5839-simple-component.cfc";
				var cfcCode = fileRead( testFile );

				var astFromPath = astFromPath( testFile );
				var astFromString = astFromString( cfcCode );

				// Both should have the same type for the first body element
				expect( astFromPath.body[ 1 ].type ).toBe( astFromString.body[ 1 ].type,
					"astFromPath() and astFromString() should produce the same node type" );
			});
		});
	}

	/**
	 * Helper function for debug logging
	 */
	private void function logger( required string message ) {
		if ( variables.debug ?: false ) {
			systemOutput( arguments.message, true );
		}
	}
}
