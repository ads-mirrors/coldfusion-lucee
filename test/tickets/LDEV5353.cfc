component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( title='LDEV-353 java clone() method missing for array objects' , body=function(){

			it( title='cfml duplicate() method on cfml array should work', body=function() {
				var foo = ArrayNew(1);
				ArrayAppend( foo, "first" );
				var bar = duplicate(foo);
				expect( bar[ 1 ] ).toBe( "first" );
			});

			it( title='java clone() method on cfml array should work', body=function() {
				var foo = ArrayNew(1);
				ArrayAppend( foo, "first" );
				var bar = foo.clone();
				expect( bar[ 1 ] ).toBe( "first" );
			});
		});

		describe( title='LDEV-5440 java clone() method missing for array objects' , body=function(){
			// Added tests for struct LDEV-5440
			it( title='java clone() method on java.util.LinkedHashMap should work', body=function() {
				var foo =  createObject( "java", "java.util.LinkedHashMap" ).init();
				foo.name = "lucee";
				foo.put("b", 2);
				var bar = foo.clone();
				expect( bar.name ).toBe( "lucee" );
				debug(foo);
				debug(bar);
			});

			it( title='cfml duplicate() method on java.util.LinkedHashMap should work', body=function() {
				var foo = createObject( "java", "java.util.LinkedHashMap" ).init();
				foo.name = "lucee";
				foo.put("b", 2);
				var bar = duplicate(foo);
				expect( bar.name).toBe( "lucee" );
				
				debug(foo);
				debug(bar);
			});

		});
	}
}