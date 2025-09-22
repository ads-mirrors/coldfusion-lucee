<cfscript>
obj = new component {
	property name="foo";
	public string function getFoo() { return variables.foo; }
	public void function setFoo(string val) { variables.foo = val; }
};
obj.foo = "bar";
function passThrough(o) { return o; }
function passTwice(o) { return passThrough(passThrough(o)); }
obj3 = passTwice(obj);
writeOutput(serializeJSON(obj3));
</cfscript>