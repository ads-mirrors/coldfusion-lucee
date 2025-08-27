<cfscript>
	param name="url.test";
	param name="url.strict" default="true";
	echo(urlDecode(string=url.test,strict=url.strict));
</cfscript>