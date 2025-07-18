component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe("cfregistry tag", function() {

			it(title="read registry value", skip=isNotWindows(), body=function() {
				
				```
				<cfregistry action = "getAll"
					branch = "HKEY_LOCAL_MACHINE\Software\Microsoft" 
					type = "Any" name = "RegQuery"> 
				
				```
				expect( RegQuery.recordcount ).toBeGT( 0 );
			});

		});
	}

	private function isNotWindows(){
		return (server.os.name does not contain "windows");
	}
}