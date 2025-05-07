<cfscript>
	numberOfRuns = 0;
	cfinclude(template="test983.cfm", runonce="true");
	cfinclude(template="test983.cfm", runonce="true");
	echo( numberOfRuns );
</cfscript>