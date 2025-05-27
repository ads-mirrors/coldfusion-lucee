component extends="org.lucee.cfml.test.LuceeTestCase" {

	public function beforeAll() {
		variables.ts=getTimeZone();
	}

	public function afterAll() {
		setTimezone(variables.ts);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4019", body=function() {
			it(title = "Checking getTimezone() as a struct", body = function( currentSpec ) {

				SetTimeZone("Asia/Calcutta");
				expect( toString(getTimeZone()) ).toBeString();
				expect( toString(getTimeZone()) ).toBe("Asia/Calcutta");
				if (getJavaVersion() >= 24) { 
					expect( getTimeZone().shortNameDST ).toBe("IST");
				} else {
					expect( getTimeZone().shortNameDST ).toBe("IDT");
				}
				expect( function() {
					loop struct=getTimeZone() index="local.key" item="local.val" {
						// writeDump(label:k,var:v);
					}
				} ).notToThrow();
			});
		});
	}

	private function getJavaVersion() {
		var raw=server.java.version;
		var arr=listToArray(raw,'.');
		if (arr[1]==1) // version 1-9
			return arr[2];
		return arr[1];
	}
}
