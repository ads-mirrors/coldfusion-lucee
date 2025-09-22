<cfscript>
obj = new component {
	property name="validator";
	public function getValidator() { return "mocked"; }
};
try {
	x = obj.validator; // should fail
	writeOutput("should have thrown");
} catch (any e) {
	writeOutput(serializeJSON({error: true, value: obj.getValidator()}));
}
</cfscript>