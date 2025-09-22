<cfscript>
obj = new component {
	property name="foo";
	public string function getFoo() { return variables.foo; }
	public void function setFoo(string val) { variables.foo = val; }
};
obj.foo = "bar";
writeOutput(serializeJSON(obj));
</cfscript>