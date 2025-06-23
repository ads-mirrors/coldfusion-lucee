component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4377 - AES Encrypt with numeric ivorsalt", body=function() {
			
			it( title="AES encrypt/decrypt with string ivorsalt should work and produce expected result", body=function( currentSpec ) {
				var originalString = "test";
				var key = "test";
				var algorithm = "AES";
				var encoding = "HEX";
				var ivorsalt = "3"; // String ivorsalt (was originally numeric which caused the bug)
				var expectedEncrypted = "87BD903885943BE48A4E68AB63B0EC6A"; // Known good result
				
				var encrypted = "";
				var decrypted = "";
				var errorMessage = "";
				
				try {
					// Step 1: Encrypt
					encrypted = Encrypt(
						String=originalString,
						key=key,
						algorithm=algorithm,
						encoding=encoding,
						ivorsalt=ivorsalt
					);
					
					// Step 2: Decrypt
					decrypted = Decrypt(
						String=encrypted,
						key=key,
						algorithm=algorithm,
						encoding=encoding,
						ivorsalt=ivorsalt
					);
					
				} catch ( e ) {
					errorMessage = e.message;
				}
				
				// Assertions
				expect( errorMessage ).toBe( "" ); // Should not throw any error
				expect( encrypted ).toBe( expectedEncrypted ); // Should produce the exact expected encrypted result
				expect( encrypted ).notToBe( originalString ); // Encrypted should be different from original
				expect( decrypted ).toBe( originalString ); // Decrypt should return original string
			});
			
			it( title="AES encrypt/decrypt with numeric ivorsalt should work after fix", body=function( currentSpec ) {
				var originalString = "test";
				var key = "test";
				var algorithm = "AES";
				var encoding = "HEX";
				var ivorsalt = 3; // Numeric ivorsalt - this was the original failing case
				var expectedEncrypted = "87BD903885943BE48A4E68AB63B0EC6A"; // Should be same as string "3"
				
				var encrypted = "";
				var decrypted = "";
				var errorMessage = "";
				
				try {
					// Step 1: Encrypt
					encrypted = Encrypt(
						String=originalString,
						key=key,
						algorithm=algorithm,
						encoding=encoding,
						ivorsalt=ivorsalt
					);
					
					// Step 2: Decrypt
					decrypted = Decrypt(
						String=encrypted,
						key=key,
						algorithm=algorithm,
						encoding=encoding,
						ivorsalt=ivorsalt
					);
					
				} catch ( e ) {
					errorMessage = e.message;
				}
				
				// Assertions
				expect( errorMessage ).toBe( "" ); // Should not throw any error after fix
				expect( encrypted ).toBe( expectedEncrypted ); // Should produce same result as string "3"
				expect( decrypted ).toBe( originalString ); // Decrypt should return original string
			});
			
		});
	}
}