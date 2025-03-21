<cfscript>
    result="";
        try { 
            param name="arguments.status" type="array" default=[];
            param name="arguments.reqId" type="numeric" default=0;
        sqlStmt = "
            SELECT 	*
            FROM   LDEV5406
            WHERE  id = :reqId
            ORDER BY id;
        ";
        params = {
            "statuses" : { type: "string" , value: arguments.status },
            "reqId"    : { type: "integer", value: arguments.reqId }
        };
        query name="result" sql=sqlStmt params=params returnType="array";
        result=true;
    } catch (any msg) {
        result=false;
    }
    writeOutput(result);
</cfscript>
