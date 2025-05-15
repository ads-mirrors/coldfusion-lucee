component extends="org.lucee.cfml.test.LuceeTestCase" {
	/* 
		test suite includes the axis extension, so temp uninstall
		this reproduces the problem but nested in the caused by
		Caused by: java.lang.NoClassDefFoundError: org/lucee/extension/axis/Axis1Caster

		To run only this test, add the following command line argument:

		mvn test -DtestFilter='LDEV5578'

		or

		mvn test -DtestFilter='LDEV5578' -DtestDebugger='true' -DtestDebug='true'
	*/

	function beforeAll(){
		variables.axis = "DF28D0A4-6748-44B9-A2FDC12E4E2E4D38";
		variables.hadAxis = findExtension( axis );

		if ( variables.hadAxis ){
			admin
				action="removeRHExtension"
				type="server"
				password="#server.SERVERADMINPASSWORD#"
				id="#variables.axis#";
		}
	}

	function afterAll(){
		if ( variables.hadAxis ){
			admin
				action="updateRHExtension"
				type="server"
				password="#server.SERVERADMINPASSWORD#"
				id="#variables.axis#";
		}
	}
	
	private function findExtension( axis ){
		var q_axis = extensionList().filter( function (row ){
			return row.id == axis;
		});

		return ( q_axis.recordcount != 0 );
	}


	function run( testResults , testBox ) {
		
		describe( title="[LDEV-5578] createDynamicProxy() should not required Webservices extension", body=function() {

			it( title="Should return instance of dynamic proxy with explicit component return type", body=function( currentSpec ) {
				var context = new DynamicProxyExplicitReturnType5578({});
				var proxy = context.asProxy();

				var implements = [].append(proxy.getClass().getInterfaces(), true).map((item) => item.getName());

				var expected = "com.givainc.test.TestInterface";
				var actual = implements[1];

				expect(expected).toBe(actual);
			});

			it( title="Should return instance of dynamic proxy with 'any' return type", body=function( currentSpec ) {
				var context = new DynamicProxyAnyReturnType5578({});
				var proxy = context.asProxy();

				var implements = [].append(proxy.getClass().getInterfaces(), true).map((item) => item.getName());

				var expected = "com.givainc.test.TestInterface";
				var actual = implements[1];

				expect(expected).toBe(actual);
			});

			it( title="Should return instance of dynamic proxy with 'component' return type", body=function( currentSpec ) {
				var context = new DynamicProxyComponentReturnType5578({});
				var proxy = context.asProxy();

				var implements = [].append(proxy.getClass().getInterfaces(), true).map((item) => item.getName());

				var expected = "com.givainc.test.TestInterface";
				var actual = implements[1];

				expect(expected).toBe(actual);
			});

		});
	}
}
