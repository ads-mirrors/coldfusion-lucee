component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {

		describe( title="Test basic cache", body=function() {

			function beforeEach(){
				cfcache( action="flush" );
			}
			// Note - Lucee defaults to useQueryString true, CF since 9 to false

			xit(title="checking cfcache - default", body = function( currentSpec ) {
				var result=_test( "simple", "one=1");
				expect( result.filecontent ).toInclude( "one=1" );

				result=_test( "simple", "one=2");
				expect( result.filecontent ).toInclude( "one=2" ); // not cached
			});

			it(title="checking cfcache - useQueryString=false", body = function( currentSpec ) {
				var result=_test( "useQueryString", "one=1","useQueryString=false");
				expect( result.filecontent ).toInclude( "one=1" );

				result=_test( "useQueryString", "one=2", "useQueryString=false" );
				expect( result.filecontent ).toInclude( "one=1" ); // cached
			});

			xit(title="checking cfcache - useQueryString=true", body = function( currentSpec ) {
				var result=_test( "useQueryString", "three=3","useQueryString=true" );
				expect( result.filecontent ).toInclude( "three=3" );

				result=_test( "useQueryString", "three=3", "useQueryString=true" );
				expect( result.filecontent ).toInclude( "three=3" ); // cached

				result=_test( "useQueryString", "four=4", "useQueryString=true" );
				expect( result.filecontent ).toInclude( "four=4" );
			});

		});
	}

	private function _test(type, qs, f=""){
		var uri = createURI( "/cache/cache_#type#.cfm" );
		var result =_InternalRequest(
			template: uri,
			url: qs,
			form: f
		);
		systemOutput(result.fileContent, true);
		return result;
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}