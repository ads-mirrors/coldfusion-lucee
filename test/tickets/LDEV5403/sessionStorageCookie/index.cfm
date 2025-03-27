<cfscript>
	session.startSession=true;
	session.cookies = cookie;
	echo(session.toJSon());
	sessionCommit();
</cfscript>