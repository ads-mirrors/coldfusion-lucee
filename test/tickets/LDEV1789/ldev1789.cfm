<cfscript>
bean = new Bean();
result = {"foo": bean.foo};
writeOutput(serializeJSON(result));
</cfscript>