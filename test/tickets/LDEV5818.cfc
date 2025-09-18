component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" skip=true {

	function run( testResults , testBox ) {

		describe( "test suite for LDEV-5181() datasource regression", function() {

			it(title = "connect to mysql", skip=noMysql(), body = function( currentSpec ) {
				var dbConfig = server.getDatasource(service="mysql",onlyConfig=true);
				var dsn = {
					type     : 'MySQL'
					, port     : dbConfig.port
					, host     : dbConfig.server
					, database : dbConfig.database
					, username : dbConfig.username
					, password : dbConfig.password
					, custom   : {
						characterEncoding : "UTF-8"
						, useUnicode        : true
					}
				};
				dbinfo type="Version" datasource="#dsn#" name="local.verify";

				expect(	verify ).toBeQuery();
				expect( verify.recordcount ).toBe( 1 );

			});

		});
	}

	private boolean function noMysql() {
		return (structCount(server.getDatasource("mysql")) eq 0);
	}
}