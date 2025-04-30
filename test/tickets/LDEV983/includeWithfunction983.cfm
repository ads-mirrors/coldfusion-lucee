<cfscript>
	numberOfRuns = 0;
	try {
		cfinclude(template="func983.cfm",runonce="true");
		numberOfRuns = queryColumnToList( numberOfRuns );

		cfinclude(template="func983.cfm",runonce="true");
		numberOfRuns = queryColumnToList( numberOfRuns );

		echo( numberOfRuns );
	} catch ( any e ){
		echo( e.stacktrace );
	}
</cfscript>
