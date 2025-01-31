<cfscript>
	session.hello=true;
	echo(url.toJson());
	echo(cookie.toJson());

	systemOutput("URL: " & url.toJson(), true);
	systemOutput("COOKIE: " & cookie.toJson(), true);
</cfscript>