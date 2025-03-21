component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2628", function() {
			it(title = "ReplaceNoCase Struct Replace All not working when runs continuously", body = function( currentSpec ) {
				for(var i=1; i<=20; i++) {
			        var theString = 'this-is-my-string';
			        var theNewString = ReplaceNoCase( theString,
			                        {
		                                "this-is-my-string" = 'this-is-my-new-string',
		                                "this-is-my-other-string" = 'this-is-my-new-string'
			                        }
			                );
			        local.list[i] = theNewString;
			    }
				expect(arraylen(arrayfindallnocase(list,"this-is-my-new-string"))).toBe(20);
			});
		});
	}
}