<cfscript>
	numberOfRuns = 0;
	try {
		cfinclude(template="test983.cfm",runonce="true");
		echo( numberOfRuns );
	} catch ( any e ){
		echo( e.stacktrace );
	}
</cfscript>
