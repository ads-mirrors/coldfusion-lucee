component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults , testBox ) {

        describe( title="Test case for LDEV-5744", body=function() {

            it(title="parseDateTime with zero-padded day (Mon Mar 03)", body=function( currentSpec ) {
                var date = parseDateTime("Mon Mar 03 03:09:07 PDT 2025");
                expect( isDate(date) ).toBeTrue();
            });
            xit(title="parseDateTime with single-digit day (Mon Mar 3)", body=function( currentSpec ) {
                var date = parseDateTime("Mon Mar 3 03:09:07 PDT 2025");
                expect( isDate(date) ).toBeTrue();
            });
            xit(title="parseDateTime with single-digit day padded with space (Mon Mar  3)", body=function( currentSpec ) {
                var date = parseDateTime("Mon Mar  3 03:09:07 PDT 2025");
                expect( isDate(date) ).toBeTrue();
            });
            // works only in Java 17
            xit(title="parseDateTime with comma in date string (9/20/22, 12:34 PM)", body=function( currentSpec ) {
                var date = parseDateTime("9/20/22, 12:34 PM");
                expect( isDate(date) ).toBeTrue();
            });
            xit(title="parseDateTime with narrow no-break space (9/20/22 12:34‹PM)", body=function( currentSpec ) {
                var date = parseDateTime("9/20/22 12:34" & chr(2039) & "PM");
                expect( isDate(date) ).toBeTrue();
            });
            
        });

    }
}