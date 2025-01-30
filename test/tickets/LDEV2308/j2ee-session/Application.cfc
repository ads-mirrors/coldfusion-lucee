component {
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,10)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.name="ldev-2308-thread-session-jee";
	this.sessionType="j2ee";

	public function onRequestStart() {
		setting requesttimeout=10;
		// this is needed to force cookie creation
		//echo("session started");
	}
}