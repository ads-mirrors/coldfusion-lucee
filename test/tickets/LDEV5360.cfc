component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( title='Query Indexed not working with QueryOfQuery' , body=function(){

			xit( title='queryIndex qoq', body=function() {
				var q = QueryNew("id,tag");
				QueryAddRow(q, [ 1, "lucee" ]);
				QueryAddRow(q, [ 2, "ralio" ]);
				```
				<cfquery name="local.qIndexed" indexName="id" dbtype="query">
					SELECT	id, tag
					FROM	q
				</cfquery>
				```
				var res = QueryRowDataByIndex( qIndexed, 2 );
				expect(res.name).toBe("ralio");
			});

			it( title='queryIndex normal query', skip=isMySqlNotSupported(), body=function() {
				```
				<cfquery name="local.q" indexName="table_name" datasource="#mySqlCredentials()#" maxrows=4>
					SELECT	table_name, engine
					FROM	INFORMATION_SCHEMA.TABLES
				</cfquery>
				```
				systemOutput(q, true);
				expect( q.recordcount ).toBe( 4 );
				var res = QueryRowDataByIndex( q, q.table_name[ 1 ] );
				systemOutput(res, true);
				expect( res.engine ).toBe( q.engine[ 1 ] )
			});

		});
	}

	function isMySqlNotSupported() {
		var mySql = mySqlCredentials();
		return isEmpty(mysql);
	}

	private struct function mySqlCredentials() {
		// getting the credentials from the environment variables
		return server.getDatasource("mysql");
	}

}