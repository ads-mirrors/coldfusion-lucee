component {
	this.name = "ldev1323";
	mySQL= getCredentials();
	this.datasource = mysql;

	public function onRequestStart() {
		createTable();
	}
	
	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

	private function createTable() {
		query {
			echo("DROP TABLE IF EXISTS ldev1323;");
		}
		query {
			echo("DROP TABLE IF EXISTS ldev1323_missing;");
		}
		query {
			echo("CREATE TABLE ldev1323 (sNo varchar(50), FirstName varchar(50), Title varchar(50))");
		}
		query {
			echo("INSERT INTO ldev1323 (sNo,FirstName,Title) VALUES (22,'john','test'),(33,'jose','sample');");
		}
	}
}