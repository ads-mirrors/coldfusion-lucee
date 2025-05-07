<cfscript>
	numberOfRuns = 0;
	include template="test983.cfm" 
		runonce="true";
	include template="test983.cfm"
		runonce="true";
	if ( structKeyExists( variables, "runonce" ) )
		throw "tag in script parsed as variable statement";
	echo( numberOfRuns );
</cfscript>