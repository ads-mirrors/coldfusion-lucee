component {
    this.name="cfmail-test-ldev-3867";
    variables.smtp=server.getTestService("smtp");
    this.mailservers = [ {
        host: smtp.SERVER,
        port: smtp.PORT_INSECURE,
        username: smtp.USERNAME,
        password: smtp.PASSWORD,
        ssl: false,
        tls: false,
        lifeTimespan: createTimeSpan(0,0,1,0),
        idleTimespan: createTimeSpan(0,0,0,10)
    } ];
  }