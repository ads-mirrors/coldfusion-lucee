component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4101", body=function() {
			it( title="Encrypt base64 test", body=function( currentSpec ) {
				var ex={};
				ex.algo="AES";
				ex.key='56RgnfAaMGCf4Ba4+XifQg=+';
				ex.password='testPassword';
				var result = "";
				expect( function(){
					ex.encrypted_password = encrypt( ex.password, ex.key, 'AES', 'Hex' ); 
				}).toThrow();
			});

			it( title="Encrypt base64 test, precise=false", body=function( currentSpec ) {
				var ex={};
				ex.algo="AES";
				ex.key='56RgnfAaMGCf4Ba4+XifQg=+';
				ex.password='testPassword';
				var result = "";
				try {
					Encrypt( string=ex.password, key=ex.key, algorithm="AES", encoding="hex", precise=false );
				} catch ( e ) {
					// throws invalid character [=] in base64 string at position [23]
					result = e.message;
				}
				expect( result ).toBe( "" ); // shouldn't throw
			});

		});
	}
}
