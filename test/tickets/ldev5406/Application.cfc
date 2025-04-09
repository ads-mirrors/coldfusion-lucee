component {
    this.name = "LDEV5406";
    this.datasource = server.getDatasource("mssql");

    public function onRequestStart() {
        setting requesttimeout=10 showdebugOutput=false;
    }

    function onApplicationStart() {
        queryExecute("DROP TABLE IF EXISTS LDEV5406", [], {datasource=this.datasource});
        queryExecute("CREATE TABLE LDEV5406( id INT , name VARCHAR(30) )", [], {datasource=this.datasource});
        queryExecute("INSERT INTO LDEV5406 VALUES(1,'pothys'), (100, 'lucee')", [], {datasource=this.datasource});
    }
}
