component accessors="true" output="false" {
	/*
		By returning "component" as the return type, the need for the AXIS extension
		is unneeded.
	*/
	public component function init(){
		variables.implements = createObject("java", "com.givainc.test.TestInterface", [getDirectoryFromPath(getCurrentTemplatePath()) & "/lib/TestInterface.jar"]);

		return this;
	}

	public any function asProxy(){
		return createDynamicProxy(this, [variables.implements]);
	}

	public struct function toLiquid(){
		return {};
	}

}