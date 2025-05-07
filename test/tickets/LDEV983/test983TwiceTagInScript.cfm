<cfscript>
	numberOfRuns = 0;
	include template="test983.cfm" runonce="true";
	include template="test983.cfm" runonce="true";
	echo( numberOfRuns );
</cfscript>