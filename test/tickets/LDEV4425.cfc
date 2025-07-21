component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.uri = createURI("LDEV4425");
	}

	function afterAll() {
		if (!notHasMysql()) {
			queryExecute( sql="DROP TABLE IF EXISTS LDEV4425", options: {
				datasource: server.getDatasource("mysql")
			}); 
		}
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4425 - queryExecute", function() {
			it( title="Check generatedKey in insert operation using queryExecute() with returntype=query", skip="#notHasMysql()#", body=function( currentSpec ){
				var result = _internalRequest(
					template = "#variables.uri#/test4425.cfm",
					forms = {returnType: "query", queryType: "queryExecute"}
				);
				expect( deserializeJson( result.fileContent ) ).toHaveKey( "generatedKey" );
			});

			xit( title="Check generatedKey in insert operation using queryExecute() with returntype=array", skip="#notHasMysql()#", body=function( currentSpec ){
				var result = _internalRequest(
					template = "#variables.uri#/test4425.cfm",
					forms = {returnType: "array", queryType: "queryExecute"}
				);
				expect( deserializeJson( result.fileContent ) ).toHaveKey( "generatedKey" );
			});

			xit( title="Check generatedKey in insert operation using queryExecute() with returntype=struct", skip="#notHasMysql()#", body=function( currentSpec ){
				var result = _internalRequest(
					template = "#variables.uri#/test4425.cfm",
					forms = {returnType: "struct", queryType: "queryExecute"}
				);
				expect( deserializeJson( result.fileContent ) ).toHaveKey( "generatedKey" );
			});

		});

		describe("Testcase for LDEV-4425 - cfquery", function() {
			it( title="Check generatedKey in insert operation using cfquery() with returntype=query", skip="#notHasMysql()#", body=function( currentSpec ){
				var result = _internalRequest(
					template = "#variables.uri#/test4425.cfm",
					forms = {returnType: "query", queryType: "cfquery"}
				);
				expect( deserializeJson( result.fileContent ) ).toHaveKey( "generatedKey" );
			});

			xit( title="Check generatedKey in insert operation using cfquery() with returntype=array", skip="#notHasMysql()#", body=function( currentSpec ){
				var result = _internalRequest(
					template = "#variables.uri#/test4425.cfm",
					forms = {returnType: "array", queryType: "cfquery"}
				)
				expect( deserializeJson( result.fileContent ) ).toHaveKey( "generatedKey" );
			});

			xit( title="Check generatedKey in insert operation using cfquery() with returntype=struct", skip="#notHasMysql()#", body=function( currentSpec ){
				var result = _internalRequest(
					template = "#variables.uri#/test4425.cfm",
					forms = {returnType: "struct", queryType: "cfquery"}
				);
				expect( deserializeJson( result.fileContent ) ).toHaveKey( "generatedKey" );
			});

		});
	}

	private function notHasMysql() {
		return structCount(server.getDatasource("mysql")) == 0;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
