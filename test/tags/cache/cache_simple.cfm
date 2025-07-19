<cfparam name="form.useQueryString" default="">
<cfoutput>
	rendered at #now()#.<br/>
	#htmlEditFormat(cgi.query_string)#<br>
</cfoutput>
<cfcache>
	<cfoutput>
		cached at #now()#.<br/>
		#cgi.query_string#
	</cfoutput>
</cfcache>
