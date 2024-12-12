component extends = "org.lucee.cfml.test.LuceeTestCase" {


	function beforeAll(){
		variables.preciseMath = getApplicationSettings().preciseMath;
		variables.datasource =  server.getDatasource( "h2", server._getTempDir( "ldev3188" ) );
	};

	function afterAll(){
		application action="update" preciseMath=variables.preciseMath;
	};


	function run( testResults, testBox ){
		describe( "Test case for LDEV3188", function(){
			it(title = "cfqueryparam sql integer types overflow, large number, preciseMath false", skip=true, body = function( currentSpec ){
				application action="update" preciseMath=false;
				_test( false, "10000223372036854775807", true );
				_test( false, "10000223372036854775807", true );
			});

			it(title = "cfqueryparam sql integer types overflow, large number, preciseMath true", skip=true, body = function( currentSpec ){
				application action="update" preciseMath=true;
				_test( true, "10000223372036854775807", true );
				_test( true, "10000223372036854775807", true );
			});

			it(title = "cfqueryparam sql integer types, preciseMath false", body = function( currentSpec ){
				application action="update" preciseMath=false;
				_test( false, "123", false );
				_test( false, 123, false );
			});

			it(title = "cfqueryparam sql integer types, small number, preciseMath true", body = function( currentSpec ){
				application action="update" preciseMath=true;
				_test( true, "123", false );
				_test( true, 123, false );
			});
		});
	}

	private function _test( boolean precise, string num, boolean expectFail ){
		var sqltypes = [ "integer", "bigint", "tinyint", "smallint" ];

		loop array=sqltypes index="local.type" {
			var err = false;
			try {
				```
				<cfquery name="local.q" datasource="#variables.datasource#" result="local.result">
					select <cfqueryparam  CFSQLTYPE="cf_sql_#type#" value="#arguments.num#"> as result
				</cfquery>
				```
			} catch ( e ){
				err = true;
			}
			if ( arguments.expectFail ){
				expect( err ).toBeTrue();
				expect( q.result ).toBe( arguments.num ); // should be unreachable
			} else {
				expect(err).toBeFalse();
				expect( q.result ).toBe( arguments.num );
			}
		}
	}

}
