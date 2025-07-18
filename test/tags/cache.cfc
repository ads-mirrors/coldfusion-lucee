component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {

		describe( title="Test basic cache", body=function() {

			function beforeEach(){
				cfcache( action="flush" );
			}
			// Note - Lucee defaults to useQueryString true, CF since 9 to false

			it(title="checking cfcache - default", body = function( currentSpec ) {
				var result=_test( "simple", "one=1" );
				expect( result.filecontent ).toInclude( "one=1" );

				result=_test( "simple", "one=2");
				expect( result.filecontent ).toInclude( "one=2" ); // not cached
			});

		});

		describe( title="Test useQueryString", body=function() {
			it(title="checking cfcache - useQueryString=false", body = function( currentSpec ) {
				var result=_test( "useQueryString", "one=1", "useQueryString=false" );
				expect( result.filecontent ).toInclude( "one=1" );

				result=_test( "useQueryString", "one=2", "useQueryString=false" );
				expect( result.filecontent ).toInclude( "one=1" ); // cached
			});

			it(title="checking cfcache - useQueryString=true", body = function( currentSpec ) {
				var result=_test( "useQueryString", "three=3", "useQueryString=true" );
				expect( result.filecontent ).toInclude( "three=3" );

				result=_test( "useQueryString", "three=3", "useQueryString=true" );
				expect( result.filecontent ).toInclude( "three=3" ); // cached

				result=_test( "useQueryString", "four=4", "useQueryString=true" );
				expect( result.filecontent ).toInclude( "four=4" );
			});
		});

		describe( title="Test useCache", body=function() {
			it(title="checking cfcache - useCache=false && useQueryString=false", body = function( currentSpec ) {
				var result=_test( "useQueryString", "three=3", "useQueryString=false&wasCached=true" );
				expect( result.filecontent ).toInclude( "three=3" );

				result=_test( "useQueryString", "four=4", "useQueryString=false&useCache=false&cacheAnyway=true" );
				expect( result.filecontent ).notToInclude( "wasCached=true" ); // not cached, but overwrites cache
				expect( result.filecontent ).toInclude( "four=4" );

				result=_test( "useQueryString", "four=4", "useQueryString=false" );
				expect( result.filecontent ).toInclude( "cacheAnyway" ); // cached from previous
				expect( result.filecontent ).toInclude( "four=4" );
			});

			it(title="checking cfcache - useCache=false && useQueryString=true", body = function( currentSpec ) {
				var result=_test( "useQueryString", "three=3", "useQueryString=true&wasCached=true" );
				expect( result.filecontent ).toInclude( "three=3" );

				result=_test( "useQueryString", "three=3", "useQueryString=true&useCache=false&cacheAnyway=true" );
				expect( result.filecontent ).notToInclude( "wasCached=true" ); // not cached, but overwrites cache
				expect( result.filecontent ).toInclude( "three=3" );

				result=_test( "useQueryString", "three=3", "useQueryString=true" );
				expect( result.filecontent ).toInclude( "cacheAnyway" ); // cached from previous
				expect( result.filecontent ).toInclude( "three=3" );
			});
		});

		describe( title="Test stripWhiteSpace", body=function() {
			it(title="checking cfcache - stripWhiteSpace=false", body = function( currentSpec ) {
				var result=_test( "whitespace", "", "stripWhiteSpace=false" );
				expect( result.filecontent ).toInclude( "[#chr(9)#  lucee  #chr(9)#]" );
			});

			it(title="checking cfcache - stripWhiteSpace=true", body = function( currentSpec ) {
				var result=_test( "whitespace", "", "stripWhiteSpace=true" );
				expect( result.filecontent ).notToInclude( " [#chr(9)#  lucee  #chr(9)#] " );

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
		// systemOutput(result.fileContent, true);
		return result;
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}