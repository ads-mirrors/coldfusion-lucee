component accessors="true" output="false" {
	/*
		The root cause of the exception is specifying that the constructor
		return an instance of "DynamicProxyExplicitReturnType5578".

		Change this to "any" and the problem goes away, like in the
		DynamicProxyAnyReturnType5578.cfc.
	*/
	public DynamicProxyExplicitReturnType5578 function init(){
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