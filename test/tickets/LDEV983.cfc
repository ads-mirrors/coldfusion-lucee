component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV983");
	}

	function run( testResults , testBox ) {

		// keep in mind these tests need internal request to isolate them from each other!

		describe( title="Test suite for LDEV-0983", body=function() {
			it(title = "Checking cfinclude with runonce attribute", body = function( currentSpec ) {
				var numberOfRuns=0;
				cfinclude(template="#variables.uri#/test983.cfm", runonce="true");
				expect(numberOfRuns).toBe( 1 );
			});

			it(title = "Checking include run twice with runonce attribute", body = function( currentSpec ) {
				var result = _InternalRequest(
					template:"#variables.uri#/test983Twice.cfm"
				);
				expect( result.filecontent.trim() ).toBe( 1 );
			});

			xit(title = "Checking include with runonce attribute, tag in script syntax", body = function( currentSpec ) {
				var result = _InternalRequest(
					template:"#variables.uri#/includePage983TagInScript.cfm"
				);
				expect( result.filecontent.trim()).toBe( 1);
			});

			it(title = "Checking include with runonce attribute, tag as function syntax", body = function( currentSpec ) {
				result = _InternalRequest(
					template:"#variables.uri#/includePage983TagAsFunction.cfm"
				);
				expect( result.filecontent.trim() ).toBe( 1 );
			});

			it(title = "Checking include without runonce attribute", body = function( currentSpec ) {
				var numberOfRuns=0;
				include template="#variables.uri#/test983.cfm";
				expect(numberOfRuns).toBe( 1);
			});

			it(title = "Checking include without any attributes", body = function( currentSpec ) {
				var numberOfRuns=0;
				include "#variables.uri#/test983.cfm";
				expect(numberOfRuns).toBe( 1 );
			});

			it(title = "Checking include with runonce attribute, with a function", body = function( currentSpec ) {
				result = _InternalRequest(
					template:"#variables.uri#/includeWithfunction983.cfm"
				);
				expect( result.filecontent.trim() ).toBe( 3 );
			});
			

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}

