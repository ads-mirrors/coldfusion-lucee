component extends="org.lucee.cfml.test.LuceeTestCase" {

	
	function run( testResults , testBox ) {
		describe( "test case for LDEV-5074", function() {
			it( title="cast cfc as CharSequence and load from string", body=function( currentSpec ) {
				// create a proxy class from the component
				var proxy=javacast("java.lang.CharSequence", new LDEV5074.Test());
				// get class name of the proxy
				var className=proxy.getClass().getName();
				// load an new instance of that proxy class
				var cs=createObject("java", className).init();

				expect( cs.length() ).toBe(0);
				expect( cs&"" ).toBe("");
			});

			it( title="cast sub cfc as CharSequence and load from string", body=function( currentSpec ) {
				// create a proxy class from the sub component
				var proxy=javacast("java.lang.CharSequence", new LDEV5074.Test$Sub());
				// get class name of the proxy
				var className=proxy.getClass().getName();
				// load an new instance of that proxy class
				var cs=createObject("java", className).init();

				expect( cs.length() ).toBe(1);
				expect( cs&"" ).toBe("a");
			});


			it( title="cast sub cfc as CharSequence and load from string", body=function( currentSpec ) {
				var inline=new component implementsJava="java.lang.CharSequence" {
					function length() {
						return 2;
					}
					function toString() {
						return "ab";
					}
				}
				
				// create a proxy class from the sub component
				var proxy=javacast("java.lang.CharSequence", inline);
				// get class name of the proxy
				var className=proxy.getClass().getName();
				// load an new instance of that proxy class
				var cs=createObject("java", className).init();

				expect( cs.length() ).toBe(2);
				expect( cs&"" ).toBe("ab");
			});

		});
	}
}