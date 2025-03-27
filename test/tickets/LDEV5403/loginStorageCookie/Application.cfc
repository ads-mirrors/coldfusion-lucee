component {
	this.name="ldev-5403-loginStorageCookie";
	this.sessionManagement = true;
	this.loginStorage = "cookie";
	this.sessioncluster = true;
	this.sessionTimeout = createTimeSpan(0, 0, 0, 1);
}