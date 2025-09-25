component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults, textbox ) {
        date1 =  createDateTime( year=2025, month=9, day=25, hour=12, minute=30, second=30, millisecond=555 );
        date2 =  createDateTime( year=2025, month=9, day=25, hour=12, minute=30, second=30, millisecond=007 );
        date3 =  createDateTime( year=2025, month=9, day=25, hour=12, minute=30, second=30, millisecond=9 );
        describe("Test case for LDEV-5833", function() {
            xit( title = "dateTimeFormat with single 'l' mask", body = function( currentSpec ) {
                expect( dateTimeFormat(date1, "l") ).toBe( "555" ); // expected 555 but returns 5
                expect( dateTimeFormat(date2, "l") ).toBe( "7" );   // expected 7 but returns 0
                expect( dateTimeFormat(date3, "l") ).toBe( "9" );   // expected 9 but returns 0
            });

            xit( title = "dateTimeFormat with double 'll' mask", body = function( currentSpec ) {
                expect( dateTimeFormat(date1, "ll") ).toBe( "555" ); // expected 555 but returns 55
                expect( dateTimeFormat(date2, "ll") ).toBe( "07" );  // expected 07 but returns 00
                expect( dateTimeFormat(date3, "ll") ).toBe( "09" );  // expected 09 but returns 00
            });

            it( title = "dateTimeFormat with triple 'lll' mask", body = function( currentSpec ) {
                expect( dateTimeFormat(date1, "lll") ).toBe( "555" );
                expect( dateTimeFormat(date2, "lll") ).toBe( "007" );
                expect( dateTimeFormat(date3, "lll") ).toBe( "009" );
            });
        });
    }
}