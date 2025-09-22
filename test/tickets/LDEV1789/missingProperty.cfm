<cfscript>
obj = new component {
	property name="foo";
	public string function getFoo() { return variables.foo; }
	public void function setFoo(string val) { variables.foo = val; }
};
obj.foo = "bar";
try {
	bar = obj.bar;
	writeOutput("should have thrown");
} catch (any e) {
	writeOutput(serializeJSON({error: true}));
}
</cfscript>