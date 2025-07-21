component {
	this.name = "test4425";
	this.datasource = server.getDatasource("mysql");

	public function onRequestStart() {
		query{
			echo("DROP TABLE IF EXISTS LDEV4425");
		}
		query{
			echo("CREATE TABLE LDEV4425( id INT AUTO_INCREMENT PRIMARY KEY, test VARCHAR(50))");
		}
	}
}