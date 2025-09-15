component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title = "LDEV-5816 Test for CFPROPRTY Behavior with types and defaults", body = function() {
			it( "LDEV-5816: default values which are the wrong type should throw an error", function() {
				expect( function() {
					var cfc = new component accessors="true" {
						property name="arr" type="numeric" default="#[1,2,3]#";
						property name="st" type="numeric" default="#{lucee:"rocks"}#";
						property name="st" type="numeric" default="#queryNew('a,b')#";
					};
				}).toThrow();
			});

			it( "LDEV-5816: default values which are the wrong type should throw an error", function() {
				expect( function() {
					var cfc = new component accessors="true" {
						property name="arr" type="array" default="123";
						property name="st" type="struct" default="123";
						property name="qry" type="query" default="123";
					};
				}).toThrow();
			});
			
			it( "LDEV-5816: default values which are the correct type should NOT throw an error", function() {
				var cfc = new component accessors="true" {
					property name="arr" type="array" default="#[1,2,3]#";
					property name="st" type="struct" default="#{lucee:"rocks"}#";
					property name="qry" type="query" default="#queryNew('a,b')#";
				};
				expect( cfc.getSt() ).toBeStruct();
				expect( cfc.getArr() ).toBeArray();
				expect( cfc.getQry() ).toBeQuery();
			});

			it( "LDEV-5816: default values using CFC as type should work correctly", function() {
				var cfc = new component accessors="true" {
					property name="user" type="LDEV5816.User" default="#new LDEV5816.User('testuser', 25)#";
				};
				expect( cfc.getUser() ).toBeInstanceOf( "LDEV5816.User" );
				expect( cfc.getUser().getUsername() ).toBe( "testuser" );
				expect( cfc.getUser().getAge() ).toBe( 25 );
			});

			it( "LDEV-5816: default values with wrong CFC type should throw an error", function() {
				expect( function() {
					var cfc = new component accessors="true" {
						property name="user" type="LDEV5816.User" default="not a user object";
					};
				}).toThrow();
			});

		});
	}
}
