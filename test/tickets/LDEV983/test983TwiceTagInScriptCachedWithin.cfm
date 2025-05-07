<cfscript>
	numberOfRuns = 0;
	//cfinclude(template="test983.cfm", runonce="true");
	include template="test983.cfm"
		cachedWithin="#createTimespan(0,0,0,1)#"
		runonce="true";
	sleep(100);
	include template="test983.cfm"
		cachedWithin="#createTimespan(0,0,0,1)#"
		runonce="true";
	if (structKeyExists(variables, "runonce"))
		throw "tag in script parsed as variable statement";
	echo( numberOfRuns );
</cfscript>