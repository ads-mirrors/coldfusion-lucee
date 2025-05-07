component extends="org.lucee.cfml.test.LuceeTestCase" labels="ssl" {

	function run() {
		describe("SSLCertificateList()", function() {

			it("should return a query of certificates for google.com", function() {
				var certs = SSLCertificateList("google.com");

				expect( isQuery( certs ) ).toBeTrue();
				expect( certs.recordCount ).toBeGT( 0, "Should return at least one certificate" );

				expect( certs ).toHaveKey( "subject" );
				expect( certs ).toHaveKey( "issuer" );
				expect( certs ).toHaveKey( "raw" );

			});

		});
	}

}
