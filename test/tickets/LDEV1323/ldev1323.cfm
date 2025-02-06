<cfscript>
	transaction {
		```
		<cfquery name="qry1">
			UPDATE	ldev1323
			SET		FirstName = 'lucee'
			WHERE 	Title = 'sample'
		</cfquery>
		```
		//transaction action="commit";
		transaction action="setSavePoint" savePoint="SavePoint1";

		```
		<cfquery name="qry2">
			UPDATE	ldev1323
			SET		FirstName = 'CF'
			WHERE	Title = 'sample'
		</cfquery>
		```
		//transaction action="commit";
		transaction action="setSavePoint" savePoint="SavePoint2";

		transaction action="rollback" savePoint="SavePoint1"; // firstName should be lucee again
		transaction action="commit";
	}
</cfscript>

<cfquery name="qry3">
	SELECT	FirstName
	FROM	ldev1323
	WHERE	Title = 'sample'
</cfquery>
<cfoutput>#qry3.FirstName#</cfoutput>
