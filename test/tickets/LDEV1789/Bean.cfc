component accessors="true" {
	property name="foo";
	public any function init() {
		variables.foo = "bar";
		return this;
	}
}