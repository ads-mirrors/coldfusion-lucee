component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) {
		describe( title="trigger tag ConcurrentModificationException ", body=function() {

			it(title="constantly force the config to be reloaded", body = function( currentSpec ) {
				SystemOutput("---------pre torture--------", true);
				thread name="myBackgroundThread" action="run" {
					SystemOutput("---------in torture--------", true);
					var c = 0;
					try {
						while (true) {
							SystemOutput("---------do config reload torture--------", true);
							
							admin 
								action="removeCacheConnection"
								type="server"
								password="#request.serverAdminPassword#"
								name="zac";
							sleep(10);
							c++;
							if (c gt 100) break;
						}
					} catch (e){
						SystemOutput(e, true);
					}

					SystemOutput("---------post torture--------", true);
				}

				try {
					loop times=5000 {
						var q = queryNew( "id,name,data", "integer,varchar,varchar" );
						var names= [ 'micha', 'zac', 'brad', 'pothys', 'gert', 'micha', 'zac', 'brad', 'pothys', 'gert','micha', 'zac', 'brad', 'pothys', 'gert' ];
						loop array="#names#" item="local.n" {
							var r = queryAddRow( q );
							querySetCell( q, "id", r, r );
							querySetCell( q, "name", n, r );
						}
						// native engine
						var q = QueryExecute(
							sql = "SELECT id, name FROM q ORDER BY name",
							options = { dbtype: 'query' }
						);
					}

				} catch(e){
					threadTerminate("myBackgroundThread");
					rethrow;
				}
			});
	
		});
	}

}
