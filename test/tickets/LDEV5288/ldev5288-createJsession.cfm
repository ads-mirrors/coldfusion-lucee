<cfscript>
	session.ldev5288 = true;
	session.created = now();
	echo("session:" & session.toJson());
	echo(chr(10));
	echo("cookie: " & cookie.toJson());
	echo(chr(10));
</cfscript>