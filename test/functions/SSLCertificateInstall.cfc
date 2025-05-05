component extends="org.lucee.cfml.test.LuceeTestCase" labels="ssl" {

	function run() {
		describe("SSLCertificateInstall()", function() {

			it("should install SSL certificates for google.com without error", function() {
				// Attempt to install the certificate; expect no errors
				expect(function() {
					SSLCertificateInstall("google.com");
				}).notToThrow();
			});

		});
	}

}
