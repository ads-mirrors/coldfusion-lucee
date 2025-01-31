<cfscript>
	param name="url.testSession" default=false;
	param name="url.testThread" default=true;
	//echo("pre thread has session: #getPageContext().hasCFSession()# ");
	if ( url.testThread ){
		threadName = "test-ldev2308-cfml";
		thread name="#threadName#" {
			if (url.testSession)
				session.hello = true;
		};

		threadJoin( threadName );
		if ( url.testSession ){
			echo( ThreadData()[ threadName ].error.stacktrace ?: "" );
		}
	}
	echo("post thread has session: #getPageContext().hasCFSession()# ");
</cfscript>