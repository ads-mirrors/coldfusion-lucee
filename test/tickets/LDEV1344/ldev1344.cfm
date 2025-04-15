<cfoutput>
	<cfquery name="selectQuery" datasource="LDEV1344">
		SELECT CurDate() as myDate
	</cfquery>
	<cftry>
		<cfquery name="insertQuery" datasource="LDEV1344" result="result">
			INSERT INTO LDEV1344( created )
			VALUES ('#selectQuery.mydate#')
		</cfquery>
		#result.recordcount#
		<cfcatch type="any">
			#cfcatch.stacktrace#
		</cfcatch>
	</cftry>
</cfoutput>