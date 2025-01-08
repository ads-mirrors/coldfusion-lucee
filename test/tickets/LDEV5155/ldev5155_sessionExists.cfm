<cfscript>
	param name="url.createSession" default="false";
	if ( url.createSession )
		session.ldev5155 = true;
	echo( structKeyExists(getPageContext().getCFMLFactory().getScopeContext().getAllCFSessionScopes(), "LDEV-5155-#url.type#" ) & ":" & sessionExists() );
</cfscript>
