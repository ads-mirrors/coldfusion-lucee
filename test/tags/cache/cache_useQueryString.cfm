<cfparam name="form.useQueryString">
<cfparam name="form.useCache" default="true">
<cfoutput>
	rendered at #now()#.<br/>
	#htmlEditFormat(cgi.query_string)#<br>
	#form.toJson()#<br>
</cfoutput>
<cfcache useQueryString="#form.useQueryString#" useCache="#form.useCache#">
	<cfoutput>
		cached at #now()#.<br/>
		#cgi.query_string#<br>
		#form.toJson()#<br>
	</cfoutput>
</cfcache>
