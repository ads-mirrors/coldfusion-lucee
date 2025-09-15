<!--- Nested function call as attribute (triggers bug) --->

<cfscript>
someClass = new component name="someClass" {
	function inner() {
	echo("Called from inner");
	writedump(var="Called from inner", output="console");
		return "inner-value";
	}
	function outer() {
	echo("Called from outer");
	writedump(var="Called from outer", output="console");
		return this.inner();
	}
}
</cfscript>
<cf_peerView data="#someClass.outer()#">
