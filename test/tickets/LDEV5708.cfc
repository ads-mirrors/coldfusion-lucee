component extends="org.lucee.cfml.test.LuceeTestCase"  accessors="true" {

	property name="susi" type="string"; 

	function run( testResults , testBox ) {

		describe( title="Test case LDEV-5708", body=function() {

			it(title="create/load class from component", body = function( currentSpec ) {
				var clazz=lucee.runtime.op.Caster::cfTypeToClass(getPageContext(), "LDEV5708");
				expect(isInstanceOf(clazz,"java.lang.Class")).toBeTrue();

				var instance=createObject("java",clazz);
				expect(isInstanceOf(instance,"lucee.runtime.type.Pojo")).toBeTrue();
				
				instance.setSusi("Susanne");
				expect(instance.getSusi()).toBe("Susanne");

				// do it twice to check existing
				var clazz=lucee.runtime.op.Caster::cfTypeToClass(getPageContext(), "LDEV5708");
				var instance=createObject("java",clazz);
				instance.setSusi("Susanne");
				expect(instance.getSusi()).toBe("Susanne");

			});

		});
	}

}
