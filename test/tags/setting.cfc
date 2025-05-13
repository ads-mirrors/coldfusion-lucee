component extends="org.lucee.cfml.test.LuceeTestCase" labels="setting" {

	function run( testResults, testBox ) {

		describe("Testcase for cfsetting tag - enablecfoutput", function() {

			it( title="simple enablecfoutput", body=function( currentSpec ) {
				var uri = createURI("setting/enablecfoutput_simple.cfm")
				var res = _InternalRequest(
					template: uri
				);
				var result = listChangeDelims( trim( res.filecontent ), ",", " #chr(10)##chr(13)#" );
				expect( result ).toBe( "1,2,2" );
			});

			it( title="nested enablecfoutput with reset", body=function( currentSpec ) {
				var uri = createURI("setting/enablecfoutput_reset.cfm")
				var res = _InternalRequest(
					template: uri
				);
				var result = listChangeDelims( trim( res.filecontent ), ",", " #chr(10)##chr(13)#" );
				expect( result ).toBe( "1,2,3,4,5,6,6" );
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI & "" & calledName;
	}

}


