<cfset session.createSession = true>
<cfoutput>#serializeJSON(getApplicationSettings().sessioncookie)#</cfoutput>