component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "cfproperty default attribute should support complex types", function(){

			it( "inline component with complex default property", function(){
				var comp = new component accessors=true {
					property name="MyArray" type="Array" getter="true" setter="true" default="#[]#";
					property name="name" type="string" getter="true" setter="true" default="lucee";
					function getInstance(){
						return this;
					}
				};
				debug( getComponentMetadata( comp ) );
				debug( comp.getInstance() );

				expect( comp.getName() ).toBeString();
				expect( comp.getName() ).toBe( "lucee" );

				expect( isNull( comp.getMyArray() ) ).toBeFalse( 'myArray property should be an array, not null' );
				expect( comp.getMyArray() ).toBeArray( 'myArray property should be an array' );
				expect( comp.getMyArray() ).toHaveLength( 0 );

				comp.setMyArray( [ 1, 2, 3 ] );
				expect( comp.getMyArray() ).toBe( [ 1, 2, 3] );
			});

			it( "normal component component with complex default property", function(){
				var comp = new LDEV801.ComponentWithComplexDefaultProperty();
				debug( getComponentMetadata( comp ) );
				var inst = comp.getInstance();
				debug( inst );
				
				expect( isNull( comp.getMyArray() ) ).toBeFalse( 'myArray property should be an array, not null' );
				expect( comp.getMyArray() ).toBeArray( 'myArray property should be an array' );
				expect( comp.getMyArray() ).toHaveLength( 0 );

				comp.setMyArray( [ 1, 2, 3 ] );
				expect( comp.getMyArray() ).toBe( [ 1, 2, 3] );
			});

			it( "inline component with complex property, no default", function(){
				var comp = new component accessors=true{
					property name="MyArray" type="Array" getter="true" setter="true" default="#[1]#";
					property name="name" type="string" getter="true" setter="true" default="lucee";
					property name="id" type="numeric" getter="true" setter="true" default="#123#";
					property name="luceeRocks" type="boolean" getter="true" setter="true" default="#true#";
					function getInstance(){
						return this;
					}
				};
				
				debug( comp );
				expect( comp.getName().getClass().getName() ).toBe( "java.lang.String");
				expect( comp.getName() ).toBe( "lucee" );

				expect( comp.getId().getClass().getName() ).toBe( "java.lang.Double");
				expect( comp.getId() ).toBe( 123 );

				expect( comp.getLuceeRocks().getClass().getName() ).toBe( "java.lang.Boolean" );
				expect( comp.getLuceeRocks() ).toBe( true );

				// maybe different with FNS?
				expect( isNull( comp.getMyArray() ) ).toBeFalse( 'myArray property should be an array, not null' );
				expect( comp.getMyArray() ).toBeArray( 'myArray property should be an array' );
				expect( comp.getMyArray() ).toBe( [ 1 ] );
			});

			it( "inline component with simple properties defaults, check types", function(){
				var comp = new component accessors=true{
					property name="name" type="string" getter="true" setter="true" default="lucee";
					property name="id" type="numeric" getter="true" setter="true" default="#123#";
					property name="luceeRocks" type="boolean" getter="true" setter="true" default="#true#";
					function getInstance(){
						return this;
					}
				};
				
				expect( comp.getName().getClass().getName() ).toBe( "java.lang.String");
				expect( comp.getName() ).toBe( "lucee" );

				expect( comp.getId().getClass().getName() ).toBe( "java.lang.Double");
				expect( comp.getId() ).toBe( 123 );

				expect( comp.getLuceeRocks().getClass().getName() ).toBe( "java.lang.Boolean" );
				expect( comp.getLuceeRocks() ).toBe( true );

			});

			it( "normal component with simple properties defaults, check types", function(){
				var comp = new LDEV801.ComponentWithSimpleDefaultProperties();

				debug(comp);
				
				expect( comp.getName().getClass().getName() ).toBe( "java.lang.String");
				expect( comp.getName() ).toBe( "lucee" );

				expect( comp.getId().getClass().getName() ).toBe( "java.lang.Double");
				expect( comp.getId() ).toBe( 123 );

				expect( comp.getLuceeRocks().getClass().getName() ).toBe( "java.lang.Boolean" );
				expect( comp.getLuceeRocks() ).toBe( true );

			});

		} );
	}
}
