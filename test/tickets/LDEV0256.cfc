component extends="org.lucee.cfml.test.LuceeTestCase" {
//LDEV0256.cfc
	function beforeAll() {
		variables.salt = 'A41n9t0Q';
		variables.passphrase = 'passphrase';
		variables.iterations = 10;
		variables.keysize = 2048;
	}

	function run( testResults , testBox ) {

		describe( 'LDEV-256' , function() {

			describe( 'GeneratePBKDFKey returns expected value' , function() {
				it( 'for PBKDF2WithHmacSHA1' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA1' , passphrase , salt, iterations , keysize )
					).toBe(
						'aSDkAAHiJ4aKeb9T4BJHlIao8qvFs6GzNaGN/Un6evqYcjAuHUaBKJ55vYvFr5YOVaqH7ObSfUvXaEThgAsneQWPLU2dvG9n/2fgfZTiQMx7hOuYnyeEvChGk7EfETthB4stxUuYmDSUeZk0uAe8nuYV1o/aRCZ7n0bPxuqaoYOGnAR1ZDW8t8O0r+CjlhGQO8CMJMW4SgMb166v97IBcbEy6nbatteOBGkh7G1TfBJHWnQBDMKo/M/F9X62/kVoyNcfw1fPq/DD/Op94em8kVDzPxf5Iy6ClXQzthzO9bpSzBjnPmGrBo26VwL6/a966yMuP2/HneSiI9/R5FbNiw=='
					);
				});
				it( 'for PBKDF2WithHmacSHA1 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA1' , passphrase , salt)).toBe('T79SiDbjZA7PE40YtZsdhA==');
				});

				it( 'for PBKDF2WithHmacSHA224' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA224' , passphrase , salt, iterations , keysize )
					).toBe(
						'/4CGECt6xa7xY/fXbqu2rxVy3qcc97kE23xUXjhtwuGZsaGAFDEbgC3w3mjKfkL3FZnDDx6PENDH3dAnXubsxXFoICJmHYFXCpAArQKBQBoJpnKQAE7PZmGlKpqDDnPzY6tJiXrvBKfBuFUiGpZlzlNqvQKQZsxOajOqehVfcRWQE1KgIdZtT3rh/v8C1x/Hu39lI6qEF5mKwA9HZh/ib0HyK8823JEo1cpmEarPwR1iM/HNIwbRWc6vZxb4gsyrnWp+fRaRc8KUsGdkbpMyvQoYa7puRY1TKLC/uiKprUGrtkp1R3TBFPt2bzCvcaKxBK5j32hPKh7qW6qjpPZfyg=='
					);
				});
				it( 'for PBKDF2WithHmacSHA224 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA224' , passphrase , salt)).toBe('wlWb4EdrdkIffsCt2uaHIQ==');
				});


				it( 'for PBKDF2WithHmacSHA256' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA256' , passphrase , salt, iterations , keysize )
					).toBe(
						'AnZBz2CBFz/U/G7WG0Y3ae/wLJfaDR+0pJkmYkPtkpi1Uxu6LFXCaUisYaGmSw7q2c0vE27YFXwsJyC+xXflUqLzFolJmYrKZpPUag8pt8oxit/ls/WQNSsKx0WXAslB0kAmL5M8IThqBF+8oGyq1l+xI6OdsP48t2z904f9p8sKhPJuiKqWXvbZLUAa5RfYnlBFKIo8M1vNtbrGbcRfWsh3h3HujxvN5z3XQ8Arg1Nb471hwmg6osdfAiwJtkmd2qHVyx9iRXXU84+aom23pAbZUFuVUzbP/5SMdVJ2lIPRjS93+XG5VaxCc6KteDGER0Et3OVJGiQujIkZhdo9Yw=='
					);
				});
				it( 'for PBKDF2WithHmacSHA256 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA256' , passphrase , salt)).toBe('l+xVDQz+AbkHVR+1q8yyKA==');
				});

				it( 'for PBKDF2WithHmacSHA384' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA384' , passphrase , salt, iterations , keysize )
					).toBe(
						'AD0cXcbzDZARrVLsQ4Ker3PHscIBq1c5dJXPejY2inGMx5Hl4LH9NwF7dZ/or7h5+LJUH8tHFCULI+7lUvcBrHC1l/9otfahE0bjqTAyMYwEb0XBp9E1+Qb8hiu3bcO0TXEZMq1jfRcsuT8krQa455BZagNngRWU3QsRcteSG6BEDOQKe2gRrQxHPzqeWTcZfM/a/fdAOPKJl/RyRtMlxQVOYPwMvitGlvEyL+iy2XJc0OTbDKYpUzbux+GQKfVxm0JnR9Ep/SzphRnMP5fpdg63XtKoYbCWWN6R5XRX46uqI30qKOLJ9g4x/drQQgI8Wnm0ml9F+3yyZSXBLpJ/Xg=='
					);
				});
				it( 'for PBKDF2WithHmacSHA384 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA384' , passphrase , salt)).toBe('7vZLcvs2laScVQgtpDkuGA==');
				});

				it( 'for PBKDF2WithHmacSHA512' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA512' , passphrase , salt, iterations , keysize )
					).toBe(
						'sJIgeDJf2JFcRpNYe+NeekfMHLyJU6QZ9FZpLw9wk9vICjNOk4LHCbVozn+7x1EB3R0MPTbE/4k5QdbqfdWTWuzdTxUmulvItqlOvAuR7mHXey6h9abJCOMMQs27EbVCWuSV/WhzHZVEK+vAMl3PdE7m37ESdWB6rWwDtraisfDUFn0wEQqtpAm54Fg39IBicDAKpiVy91mjQ3RQAgLA7m2wMSRnAcEhmxCxn5BkErxLgbwnp4heKFSk3f+zjHt0bzMc6+Jgri9DpVaquQQvGh76ej6ZIq+pr2gFx+YD1CyP6QGAAYprxoMMzYaesCLb+M5rsVD40KvUHXgHaoqIyw=='
					);
				});
				it( 'for PBKDF2WithHmacSHA512 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA512' , passphrase , salt)).toBe('FOHqEZmtiJU70L07REmppA==');
				});

				it( 'for PBKDF2WithHmacSHA512/256' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA512/256' , passphrase , salt, iterations , keysize )
					).toBe(
						'NwN12Ew1PUcc1U3LXsht80de6Ix6edbE0uot6TYP1hvFS27GzWz56darpgiRZWojhWlAdeDeBs9c2A/5U2j+4YmNlJE2COxjg+XdOXeYSNlA7laHtf5LrToBANwj8Cj1ZEQNlzstaYQqP7owfTIFysRE2Na9aEuErXRSPWChq2lJcbbTO7BoO4OhodJrPRmM966L2ZW8egOLV9WbYoQbs9LTjhkloozrap1QTvv9QxELrrtDO79QIVAIGB9G0zYJMa3Y6PYorEg212mKk8+TlTKhmnxaUY/UtiSs7dN6qVnCPBDi37t4yVcHGPbndNejVBAoyqJM2TShj8w+JflAJA=='
					);
				});
				it( 'for PBKDF2WithHmacSHA512/256 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA512/256' , passphrase , salt)).toBe('9Isklm6y3m1XNhN0AomndQ==');
				});

				it( 'for PBKDF2WithHmacSHA512/224 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA512/224' , passphrase , salt)).toBe('eFq7loIUDvEhLi/UiNLjHw==');
				});
				it( 'for PBKDF2WithHmacSHA512/224' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA512/224' , passphrase , salt, iterations , keysize )
					).toBe(
						'TlZGh8gJ/q4gL7sTfNuMk8gBEKDk6BOifgtLXdYx6Q4wAKQCEnMnuWEvhJcwLACPCpgwtiKJvq8xXRG79Wv1VYHPS+Udd76vD1mEWkRvPEbkHDcWBapcmHklwfHG2XCvv004+DtYl8hoySHtq67hGZs/2CTjqe/KKuBpZysFJMhuWWwu2kncxxr4sjAJcO7XCxhd3kzOgsZrq8+JVSqm4qyevNaWtY3Isfpna1CWqc6jerc1IGG0fHvmYf1/YhLCjRAPcNY1rcamds5uKBhJsmq4Mg2EFT2TyJsFH9dK2N9pfXLDzCMO4n/weMMJT4Tw6dcMZIfTYIogtTgvbNEwnA=='
					);
				});

				it( 'LDEV-2682 check missing algorithm list' , function() {
					try {
						GeneratePBKDFKey( 'PBKmissing' , passphrase , salt);
					} catch (e){
						var algos = listLast( e.message, "[]" );
						expect( listLen( algos ) ).toBeGT( 1 );
					}
				});

				/*it( 'for PBKDF2WithSHA1' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithSHA1' , passphrase , salt, iterations , keysize )
					).toBe(
						'fRtApvaLO05oqIFHi86g1oyk9As2CfykHPW62wI3T4zl8aLbuTXHL2Z9cNQt/afv1uSsOcqRF/zjlP5Nfo4I4RcFOi+xK8Odixj5S5V7F6p3wN997NZNi9awWT/pBaPEnM2s4MaZclZMghKPGH2LMSF5ysf0VOIlDk30z+mJz3k62wlquISWgiPt7k73lZOsZCpLxQ0SJqFaIHiUm5xskpGt8nR1d8eHDOu7RhwjdjV40aPSowWxRWkIYtiv7gxCV2z1LZIz8B3JCVwfhElC0jS2z5jTpsGNWMGYifEOCCIRUNPPNxFkqehcBJc/ojQhCIKBlkDtFWp9OpslWdfnQw=='
					);
				}); 

				it( 'for PBKDF2WithSHA224' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithSHA224' , passphrase , salt, iterations , keysize )
					).toBe(
						'7+S9VMy0TyX4/2yTYOl7rlln1+GM9nxv3sTxpnE/g7VEOOMIkyuQu3GSN+UAocj1wON7iwPZq4GudRJg9VFswySCJ3SRp1DuTE4sITeoguFvw1DCnzFFAiWB7WRK/lwhbKOS8bdQ+/oZN6KIvZge54j5g4jlUFCJnRkUP8UHb9MdMKf/k4Q00IIS3tIe4SbBCNH+tNevFj9Y6ENMsUSoRS6vrzjh3FTkL2ldBRjixY59qkDxpeYWTIL2jWDsS5GvnRslP1zOFDbr+uCYbQJrWNR1hWFk8VjImsct/hrggw3wZ7qJXIgupIVGdra1tTIOtqsfani5t66r/oUPgkm0KQ=='
					);
				}); 

				it( 'for PBKDF2WithSHA256' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithSHA256' , passphrase , salt, iterations , keysize )
					).toBe(
						'pn/As0rErw6lrprnur+b1TaTszMXkoetNCceI01x+dRZDOBQHN0BYIUIlxmAUcrVGT8YvJE/AvlRuNVVeaXcm2D9ldOdXZ6dEIGdaY4ovQ88aiI3mZSHK6cPvxGRQ5KZA/YymWvBa/NZvHb0JWaZHZsygbXMCAIi3ZYECBonoRd4+s+R0isqjCjeTWZoFqIum78XkTz3zMSLcNMhwr9rVzUrj+WAxLYsx9PU5IC1X5gJ5cRU7e6B3zo6MMJ1qs777hqzCKAyrG6nw93YTinpRWfkPmWJ2aGmFWBj+y5N5d4kI8nv0a+U4G2m6CVtaI1VIaObliSD+CG6yuP4ylOKKQ=='
					);
				}); 

				it( 'for PBKDF2WithSHA384' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithSHA384' , passphrase , salt, iterations , keysize )
					).toBe(
						'cCCChfIzBIdh8h6FSv/ULHXCgClCVwY1ScyZvRl4vFKaYQCNp2JIN9nMQbsKy/iO6QYbuVSgiouz8GVW3AcP+Tmp7OFOZerIf2PP9zGCgd2b3/GkdoUegcgxGREkkMHpuDSRwGwBrusb8bd1Uvt9+cMx8WMLGML8Q7LzDqZyj2Bt3dZHydncAEelWxMXfs+SrOyTdpiVXqHNOWQO2lYROs3RMUjYJmP6IrD4VLsLdOhHadpOqTWExa2uGOiZjlP5Rem3a5mM9tTH7lnxbh7XYRyeDyCQmpk0NxETVrOznclvARYlXTF3WBt/Qg2tw1HfjpoqOL8b5W1yilK8maQdhg=='
					);
				}); 

				it( 'for PBKDF2WithSHA512' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithSHA512' , passphrase , salt, iterations , keysize )
					).toBe(
						'ufg8j2Yzw7rKp/iyXYzFoNe7/BNdwa8OYvlX3oQw0HfTlj9Wm7FIxUnabHXqRpjxRIwmS42Aecn1wrWgdwZXzRLIQB0NtiUNPaTKsBu4jo2pMMxC+MVJiJAW3CEjJyQSi6ZLSSzLh0KPlFtcyrdfu/ZLcUmLVVmBLrIDuVKJwCiXOAuhOaz7NCasBRZGiQTdQreNFDUD00wPt/t2NkDwpUDfVRn7qZ10MS8gOYeo3D4kaE1DxAJpYejxwDMrvxoZdqImTSiE2nzQ+zDDgYMywkhyNYhHjyLYbI1D6t7Rcgr3vc6f7orwgho+M1Ko52caVoTN3c64ioNyI3qSqc5TjQ=='
					);
				}); */

			});

		});

	}
}
