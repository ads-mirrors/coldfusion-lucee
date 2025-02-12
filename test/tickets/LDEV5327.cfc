component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	function beforeAll() {
		variables.rounds = 500000;
	}

	function run( testResults, testBox ){
		describe("stress test query to struct", function( currentSpec ) {
			
			it(title="Query.toStruct() member function 24 cols", body=function( currentSpec )  {
				var ext = extensionList();
				systemOutput("with #ext.columnArray().len()# columns", true);
				loop times=variables.rounds {
					var res = ext.ToStruct("id");
				}
				expect( res ).toBeStruct();
			});

			it(title="Query returnType struct 24 cols", body=function( currentSpec ) {
				var ext = extensionList();
				systemOutput("with #ext.columnArray().len()# columns", true);
				loop times=variables.rounds {
					var res =  queryExecute(
						sql = 'SELECT * FROM ext',
						options = {
							dbtype = 'query',
							returnType="struct",
							columnKey = "id"
						}
					);
				}
				expect( res ).toBeStruct();
			});

			it(title="Query.toStruct() member function 5 cols", body=function( currentSpec ) {
				var ext = extensionList();
				ext =  queryExecute(
					sql = 'SELECT id,version,name,symbolicName,type FROM ext',
					options = {
						dbtype = 'query'
					}
				);
				systemOutput("with #ext.columnArray().len()# columns", true);
				loop times=variables.rounds {
					var res = ext.ToStruct("id");
				}
				expect( res ).toBeStruct();
			});

			it(title="Query returnType struct 5 cols", body=function( currentSpec ) {
				var ext = extensionList();
				ext =  queryExecute(
					sql = 'SELECT id,version,name,symbolicName,type FROM ext',
					options = {
						dbtype = 'query'
					}
				);
				
				systemOutput("with #ext.columnArray().len()# columns", true);

				loop times=variables.rounds {
					var res =  queryExecute(
						sql = 'SELECT * FROM ext',
						options = {
							dbtype = 'query',
							returnType="struct",
							columnKey = "id"
						}
					);
				}
				expect( res ).toBeStruct();
			});

		})
	}
}