component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {

		describe( title="Test case LDEV-5560", body=function() {

			it(title="checking URLDecode() function", body = function( currentSpec ) {
				URLDecode( 'busbar+100a+100%25G' );
			});

			xit(title="checking URLDecode() function via internal request", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5560" );
				var result =_InternalRequest(
					template: "#uri#/ldev5560.cfm",
					url: "test=busbar+100a+100%25G"
				);
			});

			it(title="checking URLDecode() function via internal request %26", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5560" );
				var result =_InternalRequest(
					template: "#uri#/ldev5560.cfm",
					url: "test=%26"
				);
			});

			xit(title="checking URLDecode() function via internal request %25", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5560" );
				var result =_InternalRequest(
					template: "#uri#/ldev5560.cfm",
					url: "test=%25"
				);
			});

			it(title="checking URLDecode() function via internal request %24", body = function( currentSpec ) {
				var uri = createURI( "/LDEV5560" );
				var result =_InternalRequest(
					template: "#uri#/ldev5560.cfm",
					url: "test=%24"
				);
			});


		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
