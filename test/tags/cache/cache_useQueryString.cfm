<cfparam name="form.useQueryString">
<cfoutput>
	rendered at #now()#.<br/>
	#htmlEditFormat(cgi.query_string)#<br>
	#form.toJson()#<br>
</cfoutput>
<cfcache useQueryString="#form.useQueryString#">
	<cfoutput>
		cached at #now()#.<br/>
		#cgi.query_string#<br>
		#form.toJson()#<br>
	</cfoutput>
</cfcache>
