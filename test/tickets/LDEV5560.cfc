component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {

		describe( title="LDEV-5560 urlEncode strict=true (default)", body=function() {

			it(title="checking URLDecode() function", body = function( currentSpec ) {
				var str = URLDecode( 'busbar+100a+100%25G' );
				expect( str ).toBe( 'busbar 100a 100%G' );
			});

			it(title="checking URLDecode() function via internal request %26", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5560" );
				var result =_InternalRequest(
					template: "#uri#/ldev5560.cfm",
					url: "test=%26"
				);
				expect( trim( result.fileContent ) ).toBe( "&" );
			});

			it(title="checking URLDecode() function via internal request %25", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5560" );
				expect( function(){
					var result =_InternalRequest(
						template: "#uri#/ldev5560.cfm",
						url: "test=%25"
					);
				}).toThrow("", "Invalid URL encoding")
			});

			it(title="checking URLDecode() function via internal request %24", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5560" );
				var result =_InternalRequest(
					template: "#uri#/ldev5560.cfm",
					url: "test=%24"
				);
				expect( trim( result.fileContent ) ).toBe( "$" );
			});

		});

		describe( title="LDEV-5560 urlEncode strict=false", body=function() {
			
			it(title="checking URLDecode() function via internal request", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5560" );
				var result =_InternalRequest(
					template: "#uri#/ldev5560.cfm",
					url: "strict=false&test=busbar+100a+100%25G"
				);
				expect( trim( result.fileContent ) ).toBe("busbar 100a 100%G");
			});

			it(title="checking URLDecode() function via internal request %25", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5560" );
				var result =_InternalRequest(
					template: "#uri#/ldev5560.cfm",
					url: "strict=false&test=%25"
				);
				expect( trim( result.fileContent ) ).toBe("%");
			});

			it(title="checking URLDecode() function via internal request %25Lucee", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5560" );
				var result =_InternalRequest(
					template: "#uri#/ldev5560.cfm",
					url: "strict=false&test=%25Lucee"
				);
				expect( trim( result.fileContent ) ).toBe("%Lucee");
			});

		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
