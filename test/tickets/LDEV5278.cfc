component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.startingTZ=getTimeZone();
	};

	function afterAll(){
		setTimeZone(variables.startingTZ?:"UTC");
	};

	function run( testResults, testBox ){

		describe( "LDEV-5278 serializeJson fails on dates", function(){

			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
			});
			afterEach( function(){
				setTimeZone(variables.startingTZ?:"UTC");
			});

			it( "round trip (UTC)", function(){
				var st = {
					now: now()
				};
				var json = serializeJson( st );
				var result = deserializeJson( json );
				expect( result.now ).toBe( st.now );
				expect( isDate( result.now ) ).toBeTrue();
				expect( isDate( st.now ) ).toBeTrue();
				expect( dateCompare( st.now, result.now, 's' ) ).toBe( 0 );
			});

			it( "round trip (America/Los_Angeles) negative offset", function(){
				setTimeZone("America/Los_Angeles");
				var st = {
					now: now()
				};
				var json = serializeJson( st );
				var result = deserializeJson( json );
				expect( result.now ).toBe( st.now );
				expect( isDate( result.now ) ).toBeTrue();
				expect( isDate( st.now ) ).toBeTrue();
				expect( dateCompare( st.now, result.now, 's' ) ).toBe( 0 );
			});
			

		});

		describe( "LDEV-5278 isDate fails with java 21", function(){

			it( "isDate fails on Jan 4, 2018 12:00 AM", function(){
				expect( IsDate( "Jan 4, 2018 12:00 AM" ) ).toBeTrue();
			});

			it( "isDate fails on March, 04 2025 09:20:38 -0800", function(){
				expect( IsDate( "March, 04 2025 09:20:38 -0800" ) ).toBeTrue();
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
