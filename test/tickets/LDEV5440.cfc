component extends="org.lucee.cfml.test.LuceeTestCase"  {

	function run( testResults , testBox ) {
		describe( title='LDEV-5440', body=function(){

			it( title='clone LinkedHashMap', body=function() {
                var foo = createObject( "java", "java.util.LinkedHashMap" ).init();
                foo["a"] = 1;
                foo.put("b", 2);
                var cloned=foo.clone();
                
                // using a map directly means case sensitive keys
                expect( structCount(cloned) ).toBe(2);
                expect( foo["a"] ).toBe(cloned["a"]);
			});

		});
	}

}