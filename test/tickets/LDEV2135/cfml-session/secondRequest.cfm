<cfscript>
    if (!structKeyExists(session, "ldev3125")){
        //systemOutput(session.toJson(), true);
        throw "key session.ldev3125 missing";
    }
    //systemOutput(session.ldev3125.toJson(), true);
	echo(session.ldev3125.toJson());
</cfscript>