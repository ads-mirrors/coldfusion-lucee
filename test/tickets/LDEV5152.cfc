component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread" {

	function testThreadJoinThrowOnError(){
		var names = [];
		loop from=1 to=2 index="local.i" {
			ArrayAppend( names, "ldev5152_#i#" );
			thread name="ldev5152_#i#" {
				if ( thread.name eq "ldev5152_2" )
					throw thread.name;
			}
		}

		var err = {};
		try {
			thread action="join" name="#names.toList()#" throwOnError="true";
		} catch ( e ){
			err = e;
		}

		expect( err ).notToBeEmpty();
		expect( err ).toHaveKey( "message" );
		expect( err.message ).toInclude( "ldev5152_2" );

	}

	function testThreadNotJoinThrowOnError(){
		var err = {};
		try {
			var names = [];
			loop from=3 to=4 index="local.i" {
				ArrayAppend( names, "ldev5152_#i#" );
				thread name="ldev5152_#i#" throwOnError="true" {
					if ( thread.name eq "ldev5152_3" )
						throw thread.name;
				}
			}
		} catch ( e ){
			err = e;
		}

		expect( err ).notToBeEmpty();
		expect( err ).toHaveKey( "message" );
		expect( err.message ).toInclude( "Attribute [throwonerror] is only supported for action [join]" );

	}


}