<cfscript>
	param name="url.createSessionInThread" default=false;
	param name="url.testThread" default=true;
	param name="url.checkSession" default=true;
	systemOutput(url, true);
	//echo("pre thread has session: #getPageContext().hasCFSession()# ");
	if ( url.testThread ){
		threadName = "test-ldev2308-cfml";
		thread name="#threadName#" {
			if (url.createSessionInThread)
				session.hello = true;
		};

		threadJoin( threadName );
		if ( url.createSessionInThread ){
			echo( ThreadData()[ threadName ].error.stacktrace ?: "" );
		}
	}
	if (url.checkSession)
		echo("post thread has session: #getPageContext().hasCFSession()# ");
</cfscript>