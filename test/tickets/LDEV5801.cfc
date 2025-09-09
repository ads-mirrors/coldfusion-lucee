component extends="org.lucee.cfml.test.LuceeTestCase" labels="jdbc" {

	function beforeAll() {
		if (!structKeyExists(server, "getDatasource")) {
			// for testing via browser
			variables.postgres = {
				"bundleName":"org.postgresql.jdbc",
				"bundleVersion":"42.7.7",
				"password":"lucee",
				"connectionString":"jdbc:postgresql://localhost:15432/lucee",
				"class":"org.postgresql.Driver",
				"username":"lucee"
			};
			variables.postgresConfig = {
				"host":"localhost",
				"port":"15432",
				"database":"lucee"
			};
			request.serverAdminPassword = "webweb";
		} else {
			variables.postgres = server.getDatasource(service="postgres");
			variables.postgresConfig = server.getDatasource(service="postgres", onlyConfig=true);
		}
		variables.tmpConfigDatasources = {};
	}

	function afterAll(){
		structEach( variables.tmpConfigDatasources, function( key ) {
			cfadmin (action="removeDatasource",	type="server",password="#request.serverAdminPassword#",name="#key#");
		});
	}

	function run( testResults , testBox ) {

		describe( title="LDEV-5801 check exceptions via datasource struct", body=function() {

			it(title="call dbinfo with valid datasource", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = variables.postgres;
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should not throw an exception" );
				expect( result ).toBeQuery();
			});

			it(title="call dbinfo with valid datasource [only class]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "class" ] = variables.postgres.class;
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should not throw an exception" );
				expect( result ).toBeQuery();
			});

			it(title="call dbinfo with valid datasource [only invalid class]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "class" ] = variables.postgres.class & "zzzz";
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should throw an exception" );
				expect( error.message ).toInclude( ds.class );
			});

			xit(title="call dbinfo with valid datasource [type]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "type" ] = "PostgreSQL";
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					//  key [class] doesn't exist
					debug(e);
					error = e;
				}
				//expect( error ).toBe( "", "should not throw an exception" );
				expect( error.message ).notToInclude( "specified name [null] could be found" );
				expect( error.message ).notToInclude( "key [class] doesn't exist" );

			});

			xit(title="call dbinfo with invalid datasource [dbdriver]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "dbdriver" ] = "PostgreSQL";
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					// key [class] doesn't exist
					debug(e);
					error = e;
				}
				// expect( error ).notToBe( "", "should not throw an exception" );
				expect( error.message ).notToInclude( "specified name [null] could be found" );
				expect( error.message ).notToInclude( "key [class] doesn't exist" );
			});

			xit(title="call dbinfo with invalid datasource, no dbdriver or type", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					//key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should throw an exception" );
				expect( error.message ).notToInclude( "specified name [null] could be found" );
				expect( error.message ).notToInclude( "key [class] doesn't exist" );
			});
		});

		describe( title="LDEV-5801 check exceptions via config datasource", body=function() {

			it(title="call dbinfo with valid datasource", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = duplicate(variables.postgres);
				ds = createConfigDatasource(ds, "default");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should not throw an exception" );
				expect( result ).toBeQuery();
			});

			it(title="call dbinfo with valid datasource [only class]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "class" ] = variables.postgres.class;
				ds = createConfigDatasource(ds, "only_class");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should not throw an exception" );
				expect( result ).toBeQuery();
			});

			it(title="call dbinfo with valid datasource [only invalid class]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "class" ] = variables.postgres.class & "zzzz";
				ds = createConfigDatasource(ds, "only_class");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should throw an exception" );
				expect( error.message ).toInclude( "cannot load class through its string name" );
			});

			it(title="call dbinfo with valid datasource [type]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "type" ] = "PostgreSQL";
				ds = createConfigDatasource(ds, "only_type");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					//  key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should not throw an exception" );
				expect( result ).toBeQuery();
			});

			xit(title="call dbinfo with valid datasource invalid [type] ", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "type" ] = "PostgreSQLInvalid";
				ds = createConfigDatasource(ds, "only_type_invalid");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					//  key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should throw an exception" );
				expect(	error.message ).notToInclude( "specified name [null] could be found" );
			});

			xit(title="call dbinfo with datasource [dbdriver]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "dbdriver" ] = "PostgreSQL";
				ds = createConfigDatasource( ds, "only_dbdriver" );
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					// key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should not throw an exception" );
				expect(	error.message ).notToInclude( "specified name [null] could be found" );
			});

			xit(title="call dbinfo with datasource with invalid [dbdriver]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "dbdriver" ] = "PostgreSQLInvalid";
				ds = createConfigDatasource( ds, "only_dbdriver_invalid" );
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					// key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should throw an exception" );
				expect(	error.message ).notToInclude( "specified name [null] could be found" );
			});

			xit(title="call dbinfo with invalid datasource, no dbdriver or type", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = createConfigDatasource(getDatatasourceWithoutClass(), "no_dbdriver_type");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					//key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should throw an exception" );
				expect(	error.message ).notToInclude( "specified name [null] could be found" );
			});
		});
	}

	private function createConfigDatasource( ds, name ) {
		var dsn = "ldev5801-" & name;
		var config = {
			"datasources": {}
		};
		var _ds = duplicate( ds );
		structAppend( _ds, variables.postgresConfig );
		config.datasources[ dsn ] = _ds;
		configImport( config, "server", request.serverAdminPassword );
		tmpConfigDatasources[ dsn ] = true;
		return dsn;
	}

	private function getDatatasourceWithoutClass(){
		var remove = [ "class", "bundleName", "bundleVersion" ];
		var ds = duplicate( variables.postgres );
		ArrayEach( remove, function( key ) {
			structDelete( ds, key );
		});
		return ds;
	}

	private function isPostgresNotAvailable() {
		return structCount(variables.postgres?:{}) == 0;
	}

}
