component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread" {

	function testThreadRequestScope(){
		request.results5148 = 0;
		var names = [];
		loop from=1 to=5 index="local.i" {
			ArrayAppend( names, "ldev5148_#i#" );
			thread name="ldev5148_#i#" {
				lock name="ldev5148" type="exclusive"{
					request.results5148++;
				}
				request[ thread.name ] = true;
			}
		}
		thread action="join" name="#names.toList()#";

		expect ( request.results5148 ).toBe( arrayLen( names ) ); 

		loop from=1 to=5 index="local.i" {
			expect ( request[ "ldev5148_#i#" ] ).toBeTrue(); 
		}

		for ( var t in names ){
			expect( cfthread[ t ] ).notToInclude( "error" );
		}

	}

	function afterAll(){
		structDelete( request, "results5148" );
		loop from=1 to=5 index="local.i" {
			structDelete( request, "ldev5148_#i#");
		}
	}
}