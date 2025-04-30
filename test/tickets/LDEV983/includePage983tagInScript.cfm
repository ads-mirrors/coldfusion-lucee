<cfscript>
	numberOfRuns = 0;
	try {
		include template="test983.cfm" runonce="true";
		echo( numberOfRuns );
	} catch ( any e ){
		echo( e.stacktrace );
	}
</cfscript>
