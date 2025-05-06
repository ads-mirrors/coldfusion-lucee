component extends="org.lucee.cfml.test.LuceeTestCase" {
	/* 
		To run only this test, add the following command line argument:

		mvn test -DtestFilter='LDEV5578'

		or

		mvn test -DtestFilter='LDEV5578' -DtestDebugger='true' -DtestDebug='true'
	*/

	function run( testResults , testBox ) {

		describe( title="[LDEV-5578] createDynamicProxy() should not required Webservices extension", body=function() {

			it( title="Should return instance of dynamic proxy", body=function( currentSpec ) {
				var context = new DynamicProxy({});
				var proxy = context.asProxy();
				expect(proxy).toBeInstanceOf("com.givainc.test.TestInterface");

				// var context = new DynamicProxy({});
				// var proxy = context.asProxy();

				// var implements = [].append(proxy.getClass().getInterfaces(), true).map((item) => item.getName());

				// var expected = "com.givainc.test.TestInterface";
				// var actual = implements[1];

				// expect(expected).toBe(actual);
			});

		});
	}
}
