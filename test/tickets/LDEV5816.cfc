component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {
	function run( testResults , testBox ) {
		describe( title = "LDEV-5816 Test for CFPROPRTY Behavior with types and defaults", body = function() {
			xit( "LDEV-5816: default values which are the wrong type should throw an error", function() {
				expect( function() {
					var cfc = new component accessors="true" {
						property name="arr" type="numeric" default="#[1,2,3]#";
						property name="st" type="numeric" default="#{lucee:"rocks"}#";
						property name="st" type="numeric" default="#queryNew('a,b')#";
					};
				}).toThrow();
			});

			xit( "LDEV-5816: default values which are the wrong type should throw an error", function() {
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

		});
	}
}
