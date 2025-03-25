component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {

	function beforeAll(){
		variables.ds = getCredentials();
		if (isNotSupported()) return;
		query name="test" datasource=getCredentials() {
			echo("CREATE TABLE ldev5426 (
				fieldA VARCHAR(50),
				timeToRun TIME DEFAULT '00:00:00'
			)");
		}
	}

	function afterAll(){
		if (isNotSupported()) return;
		query name="test" datasource=getCredentials() {
			echo( "DROP TABLE IF EXISTS ldev5426");
		}
	}

	function run() {
		describe(title="TIME Field Query Compatibility", skip=isNotSupported(), body=function() {
			it("should successfully retrieve TIME-type values from myTable", function() {
				queryExecute(
					sql="TRUNCATE TABLE ldev5426",
					options={datasource: getCredentials()}
				);

				queryExecute(
					sql="INSERT INTO ldev5426 (fieldA, timeToRun) VALUES ('test', '08:30:00')",
					options={datasource: getCredentials()}
				);

				// Execute problematic query
				var qry = queryExecute(
					sql="SELECT fieldA, timeToRun FROM ldev5426",
					options={datasource: getCredentials()}
				);

				// Assert expectations
				expect( qry.recordcount ).toBe( 1 );
				expect( qry.timeToRun[ 1 ] ).toBe( "1970-01-01 08:30:00" );
			});

			it("should successfully retrieve default TIME-type values from myTable", function() {
				queryExecute(
					sql="TRUNCATE TABLE ldev5426",
					options={datasource: getCredentials()}
				);

				queryExecute(
					sql=" INSERT INTO ldev5426 (fieldA) VALUES ('test')",
					options={datasource: getCredentials()}
				);

				var qry = queryExecute(
					sql=" SELECT fieldA, timeToRun FROM ldev5426",
					options={datasource: getCredentials()}
				);

				expect( qry.recordcount ).toBe( 1 );
				expect( qry.timeToRun[ 1 ] ).toBe( "1970-01-01 00:00:00" );
			});
		});
	}

	private struct function getCredentials() {
		// getting the credentials from the environment variables
		return server.getDatasource("mysql");
	}

	boolean function isNotSupported() {
		var mySql = getCredentials();
		if (structCount(mySql)) {
			return false;
		} else{
			return true;
		}
	}

}
