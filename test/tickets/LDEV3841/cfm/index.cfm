<cfscript>
	cgi.readOnly = url.cgiReadonly ?: "default";
	echo(cgi.toJson());
</cfscript>