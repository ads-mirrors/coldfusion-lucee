<cfscript>
	key = "j2ee_ldev5291";
	token = csrfGenerateToken( key );
	if ( !csrfVerifyToken( token, key ) )
		throw "invalid CSRF token";
	if ( !csrfVerifyToken( token, key, true ) )
		throw "invalid CSRF token after second try";
	if ( csrfVerifyToken( token, key, true ) )
		throw "CSRF token still valid after remove";
	echo( "all good" );
</cfscript>