component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "Test new Query addParam", function(){

			it( "check new query() addParam with 500 params", function(){

				var ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV5689" ) );
				cfquery( sql="CREATE TABLE ldev5689 (id numeric NOT NULL)", datasource=ds );

				var q = new Query(datasource=ds);
				var sql = "select count(*) as cnt from ldev5689 where id in (";
				var delim = "";
				var c = 500;
				var insertSql="insert into ldev5689 (id) values (?)";
				transaction {
					for ( var i = 1 ; i <= c ; i++ ){
						q.addParam( type: 'integer' , value: i );
						sql &= "#delim#?";
						delim = ",";
						cfquery( sql=insertSql, params=[ i ], datasource=ds );
					}
					sql &= ")";
				}

				var result = q.execute(
					sql = sql
				).getResult();

				expect( result.cnt ).toBe( c );
			});

		} );
	}

}
