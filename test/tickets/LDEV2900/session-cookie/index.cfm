<cfset session.sessionCreated = true>
<cfoutput>#serializeJSON(getApplicationSettings().sessioncookie)#</cfoutput>