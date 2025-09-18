<!--- Safe version: assign to variable first (should NOT trigger bug) --->
<cfscript>
cfc = new component {
	function methodWhichCallsWriteDump() {
		echo("Called from methodWhichCallsWriteDump");
		return "test-safe";
	}
};
data = cfc.methodWhichCallsWriteDump();
</cfscript>
<cf_peerView data="#data#">
