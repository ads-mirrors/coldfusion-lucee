component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

	function run( testResults, testBox ){
		describe( "LDEV-5278 isDate fails with java 21", function(){

			it( "isDate fails on Jan 4, 2018 12:00 AM", function(){
				expect( IsDate( "Jan 4, 2018 12:00 AM" ) ).toBeTrue();
			});

			it( "dateAdd fails on Jan 4, 2018 12:00 AM", function(){
				expect( DateAdd("d", 1, "Jan 4, 2018 12:00 AM" ) ).toBe( createDate( 2018,1, 4 ) );
			});

			it( "isDate fails on Jan 4, 2018 12:00 AM", function(){
				var ok=0;
				var bad=0
				var err = "";
				var d = createDate(2018,1,1);
				var df ="";
				var dd = "";
				for (var i=1; i < 365*20;i++){
					try {
						d= DateAdd("d", 1, d );
						df = d.dateTimeFormat("mmm d, yyyy h:nn tt");
						dd= DateAdd("d", 1, df );
						if ( !IsDate( df ) ) throw "isDate failed with [#df#]";
						//if ( !IsValid( "date", df ) ) throw "bad date [#df#]";
						ok++;
					} catch ( e ){
						
						systemOutput(d, true);
						systemOutput(df, true);
						systemOutput(dd, true);
						rethrow;
						/*
						systemOutput(left(e.stacktrace,100), true);
						*/
						err = e.stacktrace;
						bad++;
					}
				}
				// debug(err);
				// if (len(err)) throw "failed, #ok# ok, #bad# failed";
				expect( bad ).toBe( 0 );
			});

		} );
	}
}
