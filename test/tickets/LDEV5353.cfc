component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( title='java clone() method missing for array objects' , body=function(){

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

			// Added tests for struct LDEV-5440
			it( title='java clone() method on cfml struct should work', body=function() {
				var foo = StructNew();
				foo.name = "lucee";
				var bar = foo.clone();
				expect( bar.name ).toBe( "lucee" );
			});

			it( title='cfml duplicate() method on cfml struct should work', body=function() {
				var foo = StructNew();
				foo.name = "lucee";
				var bar = duplicate(foo);
				expect( bar.name).toBe( "lucee" );
			});

		});
	}
}