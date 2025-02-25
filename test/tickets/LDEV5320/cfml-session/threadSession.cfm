<cfscript>
	param name="url.createSession" default="";

	if (url.createSession eq "before")
		session.ldev5320 = "before";

	thread name="ldev5320_getSession" {
		if (url.createSession eq "inthread")
			session.ldev5320 = "inthread";
			
		if (url.createSession eq "noGetSession"){
			interval = "nosession";
		} else {
			//interval = getPageContext().getOriginalRequest().getSession(true).getMaxInactiveInterval(); // same result
			interval = getPageContext().getRequest().getSession(true).getMaxInactiveInterval();
		}
		echo( interval );
	}
	threadJoin("ldev5320_getSession");

	echo(cfthread.ldev5320_getSession.toJson());
	
</cfscript>