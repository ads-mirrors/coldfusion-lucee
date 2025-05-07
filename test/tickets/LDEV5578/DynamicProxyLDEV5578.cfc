component accessors="true" output="false" {
	/*
		The root cause of the exception is specifying that the constructor
		return an instance of "DynamicProxy".

		Change this to "any" and the problem goes away.

		You will need to flush the Lucee class cache after changing this
		to "any" or it may continue to work (for some reason changing the
		datatype does not seem to force it to re-compile).
	*/
	public DynamicProxyLDEV5578 function init(){
		variables.implements = createObject("java", "com.givainc.test.TestInterface", [getDirectoryFromPath(getCurrentTemplatePath()) & "/lib/TestInterface.jar"]);

		return this;
	}

	public any function asProxy(){
		return createDynamicProxy(this, [variables.implements]);
	}

}