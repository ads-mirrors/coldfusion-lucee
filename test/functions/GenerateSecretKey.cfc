component extends="org.lucee.cfml.test.LuceeTestCase" {

	public function run( testResults, testBox ) {
		// trigger expection to extract the support list of algorithms
		variables.algorithms = "";
		try {
			generateSecretKey("I am not a valid algo");
		} catch (e) {
			variables.algorithms = ListToArray(listLast( e.message, "[]" ));
		}

		loop array="#algorithms#" value="local.algo" {
			describe( title="Testcase for GenerateSecretKey(#algo#) function", body=function() {

				it(title="Checking the GenerateSecretKey(#algo#) function", 
						data={ algo=algo },
						body=function( data ) {
					expect( isNull( generateSecretKey( trim(data.algo) ) ) ).toBeFalse();
				});

			});
		}
	}
}