component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie" {

	function run( testResults, testBox ) {
		describe( "LDEV-5869: Cookie value with equals sign", function() {

			it( "should preserve cookie values containing single equals sign", function() {
				var uri = createURI( "LDEV5869/ldev5869.cfm" );
				var result = _InternalRequest(
					template: uri,
					urls: { method: "testCookie", cookieName: "test1", cookieValue: "someValue=123" }
				);
				expect( result.filecontent.trim() ).toBe( "someValue=123" );
			});

			it( "should preserve cookie values containing double equals signs (base64)", function() {
				var uri = createURI( "LDEV5869/ldev5869.cfm" );
				var result = _InternalRequest(
					template: uri,
					urls: { method: "testCookie", cookieName: "test2", cookieValue: "base64Data==" }
				);
				expect( result.filecontent.trim() ).toBe( "base64Data==" );
			});

			it( "should preserve long base64 encoded JWT cookie values", function() {
				var uri = createURI( "LDEV5869/ldev5869.cfm" );
				var cookieValue = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ==";
				var result = _InternalRequest(
					template: uri,
					urls: { method: "testCookie", cookieName: "sessionToken", cookieValue: cookieValue }
				);
				expect( result.filecontent.trim() ).toBe( cookieValue );
			});

			it( "should handle multiple cookies with equals signs in values", function() {
				var uri = createURI( "LDEV5869/ldev5869.cfm" );
				var result = _InternalRequest(
					template: uri,
					urls: { method: "testMultipleCookies" }
				);
				var data = deserializeJSON( result.filecontent );
				expect( data.cookie1 ).toBe( "value1=test" );
				expect( data.cookie2 ).toBe( "value2==" );
				expect( data.cookie3 ).toBe( "normalValue" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/")#/";
		return baseURI & "" & calledName;
	}

}
