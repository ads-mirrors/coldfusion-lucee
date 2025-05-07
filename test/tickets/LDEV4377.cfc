component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4377", body=function() {
			
			it( title="Encrypt test", body=function( currentSpec ) {
				var result = "";
				try {
					Encrypt(String='test',key='test',algorithm="AES",encoding="HEX",ivorsalt=3)
				} catch ( e ) {
					// throws cannot convert the input to a binary
					result = e.stacktrace;
				}
				expect( result ).toBe( "" ); // shouldn't throw
			});

		});
	}
}
