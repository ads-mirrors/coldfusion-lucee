component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function beforeAll(){
		application enableNullSupport=false;
	}

	function afterAll(){
		application enableNullSupport=false;
	}

	function run( testResults , testBox ) {
		describe( title='LDEV-4869' , body=function(){
			it( title='test null query param without value' , body=function() {
				var ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV4868" ) );
				var queryArgs = "";
				var listener = {
					before=function (caller,args) {
						queryArgs = args;
					}
				};
				```
				<cfquery name="local.q" datasource="#ds#" listener="#listener#" result="local.result">
					select <cfqueryparam cfsqltype="cf_sql_integer" null="true">
				</cfquery>
				```
				/*
				systemOutput( queryArgs, true );
				systemOutput( "", true );
				systemOutput( local.q );
				systemOutput( "", true );
				systemOutput( local.result );
				*/

				expect( queryArgs.params[ 1 ] ).toHaveKey( "null" );
				expect( queryArgs.params[ 1 ].null ).toBeTrue();
				expect( queryArgs.params[ 1 ].value ).toBe( "" );
				expect( result.sqlParameters[1] ).toBe( "" );
				expect( q[ q.columnlist ][1] ).toBe( "" );

			});

			it( title='test null query param without value, full null support' , body=function() {
				application enableNullSupport=true;
				var ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV4868" ) );
				var queryArgs = "";
				var listener = {
					before=function ( caller, args ) {
						queryArgs = args;
					}
				};
				```
				<cfquery name="local.q" datasource="#ds#" listener="#listener#" result="local.result">
					select <cfqueryparam cfsqltype="cf_sql_integer" null="true">
				</cfquery>
				```
				/*
				systemOutput( queryArgs, true) ;
				systemOutput( "", true );
				systemOutput( local.q );
				systemOutput( "", true );
				systemOutput( local.result );
				*/

				expect( queryArgs.params[ 1 ] ).toHaveKey( "null" );
				expect( queryArgs.params[ 1 ].null ).toBeTrue();
				expect( isNull( queryArgs.params[ 1 ].value ) ).toBeTrue();
				expect( isNull( result.sqlParameters[ 1 ] ) ).toBeTrue();
				expect( isNull( q[ q.columnlist ][ 1 ] ) ).toBeTrue();

			});

		});
	}

}