<cfscript>
obj = new component {
	property name="foo";
	public string function getFoo() { return variables.foo; }
	public void function setFoo(string val) { variables.foo = val; }
};
obj.foo = "bar";
json = serializeJSON(obj);
parsed = deserializeJSON(json);
writeOutput(serializeJSON(parsed));
</cfscript>