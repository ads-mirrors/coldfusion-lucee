component extends = "org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults, textbox ) {
        describe("testcase for LDEV-5406", function() {
            it(title="with empty param value", skip=notHasMsSQL(), body=function( currentSpec ) {
                var uri = createURI("LDEV5406");
                var result = _InternalRequest(
					template:"#uri#\ldev5406.cfm"
				);
                expect( trim(result.fileContent) ).notToBe('param [reqId] may not be empty');
            });
        });
    }
    // Private functions
	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
    private function notHasMsSQL(){
		return isEmpty( server.getDatasource( "mssql" ) );
	}
}
