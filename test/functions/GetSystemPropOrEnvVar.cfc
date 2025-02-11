component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title = "Testcase for GetSystemPropOrEnvVar() function", body = function() {

			it( title = "Checking GetSystemPropOrEnvVar function", body = function( currentSpec ) {
				var props = GetSystemPropOrEnvVar();
				expect(	props ).toBeArray();
				expect(	len(props) ).toBeGT( 0 );
			});

			it( title = "Check sysprop matches envVar", body = function( currentSpec ) {
				var props = GetSystemPropOrEnvVar();
				ArrayEach( props, function( item ){
					var sysPropNameAsEnv = uCase(Replace( item.sysProp, ".", "_", "all" ));
					expect( item.envVar ).toBe( sysPropNameAsEnv );
					expect( item ).toHaveKey( "desc", item.sysProp );
				});
			});

			it( title = "Checking GetSystemPropOrEnvVar(prop) function", body = function( currentSpec ) {
				var props = GetSystemPropOrEnvVar();
				ArrayEach( props, function( item ){
					var envVar = GetSystemPropOrEnvVar( item.envVar );
					var sysProp = GetSystemPropOrEnvVar( item.sysProp );
					// first configured value is ever resolved, env before property

					if ( sysProp neq envVar) {
						// manually fail as not to reveal secrets
						fail( "envar [#item.envvar#] neq sysprop [#item.sysprop#]" );
					}
				});
			});

		});
	}
}