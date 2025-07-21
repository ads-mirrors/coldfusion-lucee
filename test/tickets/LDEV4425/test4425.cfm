<cfscript>
param name="form.returnType";
param name="form.queryType";

if (form.queryType eq "queryExecute")
	queryExecute("INSERT INTO LDEV4425(test) VALUES ('test')", {}, {returntype=form.returnType, result="resultVar"});
else {
	cfquery(returnType=form.returnType result="resultVar"){
		echo("INSERT INTO LDEV4425(test) VALUES ('test')");
	};
}
writeOutput(resultVar.toJson());
</cfscript>