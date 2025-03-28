<cflogin>
	<cfloginuser name = "test" password = "password" roles = "user,admin,editor">
</cflogin>
<cfscript>
	session.startSession = true;
	session.roles = getUserRoles();
	session.cookies = cookie;
	echo(session.toJSon());
	sessionCommit();
</cfscript>