component extends="org.lucee.cfml.test.LuceeTestCase"{

    function beforeAll(){
        variables.preciseMath = getApplicationSettings().preciseMath;
    };

    function afterAll(){
        application action="update" preciseMath=variables.preciseMath;
    };

    function run( testResults , testBox ) {
        describe( title="Test suite for BitSHRN()", body=function() {

            beforeEach( function(){
                application action="update" preciseMath=variables.preciseMath;
            });

            afterEach( function(){
                application action="update" preciseMath=variables.preciseMath;
            });

            it(title="Checking BitSHRN() function", body = function( currentSpec ) {
                assertEquals("0",BitSHRN(1,1));
                assertEquals("0",BitSHRN(1,30));
                assertEquals("0",BitSHRN(1,31));
                assertEquals("0",BitSHRN(2,31));
                assertEquals("32",BitSHRN(128,2));
            });

            it(title="Checking BitSHRN() function with shifting zero", body = function(currentSpec) {
                assertEquals("1", BitSHRN(1, 0));  // 1 >> 0 = 1 (no shift should occur)
            });

            it(title="Checking BitSHRN() function with large numbers", body = function(currentSpec) {
                application action="update" preciseMath=true;
                assertEquals("2147483647", BitSHRN(4294967295, 1));  // Large number shifted right
                application action="update" preciseMath=false;
                assertEquals("2147483648", BitSHRN(4294967295, 1));  // Large number shifted right
            });

            it(title="Checking BitSHRN() function with negative numbers", body = function(currentSpec) {
                application action="update" preciseMath=true;
                assertEquals("-1", BitSHRN(-2, 1));  // -2 >> 1 = -1 (propagating the sign bit)
                assertEquals("-64", BitSHRN(-255, 2));  // -255 >> 2 = -64 (propagating the sign bit)
            });

            it(title="Checking BitSHRN() function with extreme shift values", body = function(currentSpec) {
                application action="update" preciseMath=true;
                assertEquals("8", BitSHRN(147573952589676412928, 64));  // 128 >> 64 = 0 (all bits shifted out)
                application action="update" preciseMath=false;
                assertEquals("0", BitSHRN(4611686018427387904, 63));  // 128 >> 64 = 0 (all bits shifted out)
            });
        });
    }
}
