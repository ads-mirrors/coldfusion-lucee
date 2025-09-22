<cfscript>
obj = new component {
	property name="foo";
	public string function getFoo() { return variables.foo; }
	public void function setFoo(string val) { variables.foo = val; }
};
obj.foo = "bar";
function passThrough(o) { return o; }
obj2 = passThrough(obj);
writeOutput(serializeJSON(obj2));
</cfscript>