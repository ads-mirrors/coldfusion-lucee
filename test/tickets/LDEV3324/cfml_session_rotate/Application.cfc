component {
	this.name="ldev3324_csrf_cfml";
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.sessionType="cfml";

	function onApplicationStart(){
		//systemOutput("application start #cgi.SCRIPT_NAME# - #this.name#", true);
	}

	function onApplicationEnd(){
		//systemOutput("#now()# application end #cgi.SCRIPT_NAME# - #this.name#", true);
	}

	function onSessionStart() {
		//systemOutput( "session started #cgi.SCRIPT_NAME#", true );
	}

	function onSessionEnd( sessionScope, applicationScope ) {
		//systemOutput( "session end #cgi.SCRIPT_NAME#", true );
	}

}