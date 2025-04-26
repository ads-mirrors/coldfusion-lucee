
component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for getTagData()", function() {
			it(title="checking getTagData() function - query", body=function( currentSpec ) {
				var tagData = getTagData("cf","query");
				expect(tagData).toHaveKey("nameSpace");
				expect(tagData).toHaveKey("name");
				expect(tagData).toHaveKey("description");
				expect(tagData).toHaveKey("attributeCollection");
				expect(tagData).toHaveKey("attributes");
				expect(tagData.name).toBe("query");
				expect(tagData.type).toBe("java");
				expect(tagData.status).toBe("implemented");
				expect(tagData.attributes.name).toHaveKey("required");
			});

			it(title="checking getTagData() camel case - elseif", body=function( currentSpec ) {
				var tagData = getTagData("cf","elseIf");
				expect(tagData).toHaveKey("nameWithCase");
				expect(tagData.nameWithCase).toMatchWithCase("elseIf");
			});

			it(title="checking getTagData() camel case - application", body=function( currentSpec ) {
				var tagData = getTagData("cf","application");
				expect(tagData.attributes).toHaveKey("typeChecking");
				expect(tagData.attributes.typeChecking.nameWithCase).toMatchWithCase("typeChecking");
			});

			it(title="checking getTagData() camel case - execute", body=function( currentSpec ) {
				var tagData = getTagData("cf","execute");
				expect(tagData.attributes).toHaveKey("onProgress");
				expect(tagData.attributes.onProgress.nameWithCase).toMatchWithCase("onProgress");
			});
		});
	}
}