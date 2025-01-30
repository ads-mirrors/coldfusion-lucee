component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ){
		describe(title="LDEV-4259 getMetaData( obj ) returns different class name in some cases than obj.getClass().getName()", body=function( currentSpec ) {
			
			it(title="check obj.getClass().getName()", body=function( currentSpec )  {
				var objClass = createObject( 'java', 'java.lang.ProcessBuilder' ).init( [ 'box', 'info' ] ).start().getClass().getName();
				expect( objClass ).toBe( "java.lang.ProcessImpl" );
			});

			xit(title="check getMetaData( obj ).getClass().getName()", body=function( currentSpec )  {
				var objClass = getMetaData( createObject( 'java', 'java.lang.ProcessBuilder' ).init( [ 'box', 'info' ] ).start() ).getClass().getName();
				expect( objClass ).toBe( "java.lang.ProcessImpl" ); // returns java.lang.ProcessHandleImpl$Info
			});

			xit(title="check getMetaData( obj )", body=function( currentSpec )  {
				var obj = createObject( 'java', 'java.lang.ProcessBuilder' ).init( [ 'box', 'info' ] ).start();
				var meta = getMetaData( obj );
				expect( meta ).toHaveKey( "name" );
			});

			it(title="check getMetaData( obj )", body=function( currentSpec )  {
				var obj = createObject( 'java', 'java.lang.ProcessBuilder' ).init( [ 'box', 'info' ] );
				var meta = getMetaData( obj );
				expect( meta ).toHaveKey( "name" );
			});
		});
	}
}
