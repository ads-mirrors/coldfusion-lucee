<!--- Reduced test: direct method call as attribute, no writedump, only echo --->
<cfscript>
someClass = new component name="someClass" {
	function methodWhichCallsEcho() {
		echo("Called from methodWhichCallsEcho");
		return "test-echo";
	}
}
</cfscript>
<cf_peerView data="#someClass.methodWhichCallsEcho()#">
