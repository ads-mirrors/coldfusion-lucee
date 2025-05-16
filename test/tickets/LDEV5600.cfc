component extends="org.lucee.cfml.test.LuceeTestCase" labels="javasettings"  {

	function run( testResults , testBox ) {
		describe( title="Test Tika", body=function() {

			it(title="load tika via javasettings ", body = function( currentSpec ) {
				var t = _getTika();
				var mi = mvnInfo(t);
				//systemOutput(mi,1,1);
				expect( mi.groupId ).toBe( "org.apache.tika");
				expect( mi.artifactId ).toBe( "tika-core");
				expect( mi.version ).toBe( "1.28.5" );
			});

			it(title="load tika via javasettings again", body = function( currentSpec ) {
				var t = _getTika();
				var mi = mvnInfo(t);
				//systemOutput(mi,1,1);
				expect( mi.groupId ).toBe( "org.apache.tika");
				expect( mi.artifactId ).toBe( "tika-core");
				expect( mi.version ).toBe( "1.28.5" );
			});
	
		});
	}

	private function _getTika(){
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