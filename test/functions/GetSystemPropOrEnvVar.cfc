component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title = "Testcase for GetSystemPropOrEnvVar() function", body = function() {

			it( title = "Checking GetSystemPropOrEnvVar function", body = function( currentSpec ) {
				var props = GetSystemPropOrEnvVar();
				expect(	props ).toBeArray();
				expect(	len(props) ).toBeGT( 0 );
			});

			xit( title = "Checking GetSystemPropOrEnvVar(prop) function", body = function( currentSpec ) {
				var props = GetSystemPropOrEnvVar();
				ArrayEach( props, function( item ){
					var envVar = GetSystemPropOrEnvVar( item.envvar );
					var sysProp = GetSystemPropOrEnvVar( item.sysprop );
					expect( sysProp ).toBe( envVar, item.envvar ); // first configured value is ever resolved, env before property
				});
			});

		});
	}
}