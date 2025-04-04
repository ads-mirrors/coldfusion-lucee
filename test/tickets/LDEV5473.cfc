component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title='LDEV-5473', body=function(){

			it( title='calling .toJson() on an empty argument throws a NPE', body=function() {
				function test( name ) {
					arguments.name.toJson();
				}
				try {
					test();
				} catch( e ){
					expect( e.type ).notToBe( "java.lang.NullPointerException" );
				}
			});

		});
	}

}