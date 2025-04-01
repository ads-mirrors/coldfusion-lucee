component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( title='LDEV-5459' , body=function(){

			it( title='missing object', body=function() {
				var type = "org.hibernate.criterion.Restrictions";
				var obj = createObject( "java", type );
				expect( obj.getClass().getName() ).toBe( type );

				var javaProxies = createObject( "java", "java.util.concurrent.ConcurrentHashMap" ).init();

				javaProxies.put(
					type,
					{
						type   : type,
						object : createObject( "java", type )
					}
				);
				var res = javaProxies.get( type );
				expect( isNull(res.object) ).toBeFalse();
				expect( obj.getClass().getName() ).toBe( res.object.getClass().getName() );

			});

			it( title='missing object deeper structure ConcurrentHashMap', body=function() {
				var type = "java.io.FileInputStream";
				var obj = createObject( "java", type );
				expect( obj.getClass().getName() ).toBe( type );

				var javaProxies = createObject( "java", "java.util.concurrent.ConcurrentHashMap" ).init();

				javaProxies.put(
					type,
					{
						type   : type,
						object : createObject( "java", type )
					}
				);
				var res = javaProxies.get( type );
				expect( isNull(res.object) ).toBeFalse();
				expect( obj.getClass().getName() ).toBe( res.object.getClass().getName() );

			});

			it( title='missing object flat structure ConcurrentHashMap', body=function() {
				var type = "java.io.FileInputStream";
				var obj = createObject( "java", type );
				expect( obj.getClass().getName() ).toBe( type );

				var javaProxies = createObject( "java", "java.util.concurrent.ConcurrentHashMap" ).init();

				javaProxies.put(
					type,createObject( "java", type )
				);
				var res = javaProxies.get( type );
				expect( isNull(res) ).toBeFalse();
				expect( obj.getClass().getName() ).toBe( res.getClass().getName() );

			});


			it( title='missing object deeper structure Struct', body=function() {
				var type = "java.io.FileInputStream";
				var obj = createObject( "java", type );
				expect( obj.getClass().getName() ).toBe( type );

				var javaProxies = {};

				javaProxies.put(
					type,
					{
						type   : type,
						object : createObject( "java", type )
					}
				);
				var res = javaProxies.get( type );
				expect( isNull(res.object) ).toBeFalse();
				expect( obj.getClass().getName() ).toBe( res.object.getClass().getName() );

			});

			it( title='missing object flat structure Struct', body=function() {
				var type = "java.io.FileInputStream";
				var obj = createObject( "java", type );
				expect( obj.getClass().getName() ).toBe( type );

				var javaProxies = {};

				javaProxies.put(
					type,createObject( "java", type )
				);
				var res = javaProxies.get( type );
				expect( isNull(res) ).toBeFalse();
				expect( obj.getClass().getName() ).toBe( res.getClass().getName() );

			});
		});

	}


}