<cfscript>
	// the missing space between silent and { causes an exception, see LDEV3951_dump.cfm
	silent{
		dump( var="lucee",  //comment
			label="result");
	}
	echo("success");
</cfscript>