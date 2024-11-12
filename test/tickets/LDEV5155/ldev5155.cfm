<cfscript>
	x = session.ldev5155 ?: false; // this shouldn't create a session
	echo( structKeyExists(getPageContext().getCFMLFactory().getScopeContext().getAllCFSessionScopes(), "LDEV-5155" ) );
</cfscript>
