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


			it(title="make sure encapulation happens and class get read from the correct place", body = function( currentSpec ) {
				var t = getTika();
				var mi = mvnInfo(t);
				expect( mi.groupId ).toBe( "org.apache.tika");
				expect( mi.artifactId ).toBe( "tika-core");
				expect( mi.version ).toBe( "1.28.5" );
			});

		});
	}

	private function getTika() {
		// lucee bundles 1.28.4
		var tikaComponent = new component javaSettings='{
				"maven": [ "org.apache.tika:tika-core:1.28.5" ]
			}' {
			
			function getTika(){
				return new org.apache.tika.Tika();
			}
		};
		return tikaComponent.getTika();
	}

	// TODO replace with a proper function
	private function mvnInfo(obj) {
		var sct = dumpStruct(obj);
		var qry=sct.data;
		qry=querySlice(qry,6);
		qry=qry.data1.data;
		var data=[:];
		loop query=qry {
			var arr=listToArray(qry.data1,":");
			data[trim(arr[1])]=trim(arr[2]);
		}
		return data;
	}

}
