component extends="org.lucee.cfml.test.LuceeTestCase" labels="com" {

	function run() {
		describe("createObject com", function() {
			
			it(title="try instantiate WScript.Shell", skip=isNotWindows(), body=function() {
				var comObj = createObject( "com", "WScript.Shell" );
				expect( isObject( comObj ) ).toBeTrue();
			});

		});
	}

	private function isNotWindows(){
		return (server.os.name does not contain "windows");
	}
}