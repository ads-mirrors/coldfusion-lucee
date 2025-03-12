component {
	this.name="ldev-3841";
	if ( structKeyExists( url, "cgiReadOnly" ) )
		this.cgiReadOnly=url.cgiReadOnly;
	if ( structKeyExists( url, "constructor" ) )
		cgi.constructor=true;
}