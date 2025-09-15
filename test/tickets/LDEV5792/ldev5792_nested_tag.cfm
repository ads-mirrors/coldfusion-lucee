<!--- Nested custom tag inside attribute function call (triggers bug) --->

<cfscript>
someClass = new component name="someClass" {
	function getData() {
	echo("Before nested tag");
	writedump(var="Before nested tag", output="console");
		// Simulate nested tag call
		include "peerView_nested.cfm";
	echo("After nested tag");
	writedump(var="After nested tag", output="console");
		return "nested-tag-value";
	}
}
</cfscript>
<cf_peerView data="#someClass.getData()#">
