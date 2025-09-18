
<!--- Simple direct function call as attribute (should match index.cfm and repo bug) --->
<cfscript>
someClass = new component name="someClass" {
    function methodWhichCallsWriteDump() {
    echo("Called from methodWhichCallsWriteDump");
    writedump(var="Called from methodWhichCallsWriteDump", output="console");
        return "test-simple";
    }
}
</cfscript>
<cf_peerView data="#someClass.methodWhichCallsWriteDump()#">
