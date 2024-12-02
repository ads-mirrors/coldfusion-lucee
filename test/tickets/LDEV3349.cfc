component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ){

        describe( "checking query string encoding for spaces", function() {
            variables.uri = createURI("LDEV3349/ldev3349.cfm");

            it( title="cfhttp with queryString %20", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "%20"
                );
                expect(result.filecontent).toBe(" =");
            });
            it( title="cfhttp with queryString space", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: " "
                );
                expect(result.filecontent).toBe(" =");
            });
            it( title="cfhttp with queryString +", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "+"
                );
                expect(result.filecontent).toBe(" =");
            });
            it( title="cfhttp with queryString +%20", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "+%20"
                );
                expect(result.filecontent).toBe("  =");
            });
            it( title="cfhttp with queryString %2B+%2B", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "%2B+%2B"
                );
                expect(result.filecontent).toBe("+ +=");
            });
            it( title="cfhttp with queryString space1space", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: " 1 "
                );
                expect(result.filecontent).toBe(" 1 =");
            });

            it( title="cfhttp with queryString a=?", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "a=?" // ? is a reserved character and will be converted to %3F encoding
                );
                expect(result.filecontent).toBe("a=?");
            });

            it( title="cfhttp with queryString a=?&b=+", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "a=?&b=+" // ? is a reserved character and will be converted to %3F encoding
                );
                expect(result.filecontent).toBe("a=?&b= ");
            });

            it( title="cfhttp with queryString a=%3F&b= ", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "a=%3F&b=+"
                );
                expect(result.filecontent).toBe("a=?&b= ");
            });

            it( title="LDEV-5172 azure blob url problem", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "rscd=attachment%3B+filename%3D%22results.zip%22"
                );
                expect( result.filecontent ).toBe( 'rscd=attachment; filename="results.zip"' );
            });
        });
    }

    private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & arguments.calledName;
	}
}