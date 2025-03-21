component extends="org.lucee.cfml.test.LuceeTestCase" labels="cfstopwatch" {
    function run( testResults, testBox ) {
        describe("Testcase for cfstopwatch tag", function() {
            it( title="Checking stopwatch", body=function( currentSpec ) {
                stopwatch variable="local.stopwatchVar" {
                    var i = 0;
                    loop from="1" to="10000" index="local.j" {
                        i++;
                    }
                }

                expect(stopwatchVar).toBeNumeric();
                expect(stopwatchVar).toBeTrue();
            });
        });
    }
}