
component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for getFunctionData()", function() {
			it(title="checking getFunctionData() function", body=function( currentSpec ) {
				var functionData = getFunctionData("dateCompare")
				expect(functionData).toHaveKey("name");
				expect(functionData).toHaveKey("description");
				expect(functionData).toHaveKey("returnType");
				expect(functionData).toHaveKey("arguments");
				expect(functionData.name).toBe("datecompare");
				expect(functionData.type).toBe("java");
				expect(functionData.status).toBe("implemented");
				expect(functionData.arguments[1].name).toBe("date1");
				expect(functionData.arguments[1]).toHaveKey("required");
			});

			it(title="checking getFunctionData() camel case", body=function( currentSpec ) {
				var functionData = getFunctionData("dateCompare");
				expect(functionData).toHaveKey("nameWithCase");
				expect(functionData.nameWithCase).toMatchWithCase("dateCompare");
				expect(functionData.arguments[3].nameWithCase).toMatchWithCase("datePart");
			});
		});
	}
}