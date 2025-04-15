component {

	this.name = "ldev1344";
	this.datasources["LDEV1344"] = server.getDatasource("mysql");
	this.datasource = "LDEV1344";

	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV1344");
		}
		query{
			echo("CREATE TABLE LDEV1344( created datetime)");
		}
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV1344");
		}
	}
}
