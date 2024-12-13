component extends="org.lucee.cfml.test.LuceeTestCase"{

    function beforeAll(){
        variables.preciseMath = getApplicationSettings().preciseMath;
    };

    function afterAll(){
        application action="update" preciseMath=variables.preciseMath;
    };

    function run( testResults , testBox ) {
        describe( title="Test suite for BitSHLN()", body=function() {

            beforeEach( function(){
                application action="update" preciseMath=variables.preciseMath;
            });

            afterEach( function(){
                application action="update" preciseMath=variables.preciseMath;
            });

            it(title="Checking BitSHLN() function with small shifts", body = function(currentSpec) {
                assertEquals("2", BitSHLN(1, 1));  // 1 << 1 = 2
                assertEquals("1073741824", BitSHLN(1, 30)); 
                assertEquals("2147483648", BitSHLN(1, 31)); 
                assertEquals("4294967296", BitSHLN(2, 31));  
            });

            it(title="Checking BitSHLN() function with zero shift", body = function(currentSpec) {
                assertEquals("1", BitSHLN(1, 0));  // 1 << 0 = 1 (no shift)
            });

            it(title="Checking BitSHLN() function with large number shift", body = function(currentSpec) {
                // Shift a large number that's already near the boundary of 32-bit integer range
                application action="update" preciseMath=true;
                assertEquals("4294967294", toString(BitSHLN(2147483647, 1)));  // 2147483647 << 1 = 0 (overflow)
                application action="update" preciseMath=false;
                assertEquals("2147483647", toString(BitSHLN(2147483647, 1)));  // 2147483647 << 1 = 0 (overflow)
            });

            it(title="Checking BitSHLN() function with negative shift", body = function(currentSpec) {
                // Negative shifts aren't typically allowed for left shifts, but adding this to ensure error handling
                // Assuming an error or specific behavior is handled correctly in Lucee or by the function definition
                var result = "";
                try {
                    result = BitSHLN(1, -1);
                    fail("BitSHLN should throw an error or handle negative shift values appropriately.");
                } catch (any e) {
                    assertTrue(e.message contains "Invalid shift value");  // Assuming an error is thrown
                }
            });

            it(title="Checking BitSHLN() function with extreme shifts 16", body = function(currentSpec) {
                // Extreme shift cases where the shift count is very large
                application action="update" preciseMath=true;
                assertEquals(65536, BitSHLN(1, 16));  // 1 << 64 = 0 (all bits shifted out in a 64-bit context)
            });
            it(title="Checking BitSHLN() function with extreme shifts 64", body = function(currentSpec) {
                // Extreme shift cases where the shift count is very large
                application action="update" preciseMath=true;
                assertEquals("18446744073709551616",toString(BitSHLN(1, 64)));  // 1 << 64 = 0 (all bits shifted out in a 64-bit context)
            });
            it(title="Checking BitSHLN() function with extreme shifts 128", body = function(currentSpec) {
                // Extreme shift cases where the shift count is very large
                application action="update" preciseMath=true;
                assertEquals("340282366920938463463374607431768211456",toString(BitSHLN(1, 128)));  
                application action="update" preciseMath=false;
                assertEquals("4611686018427387904", BitSHLN(1, 62)); 
            });
        });
    }
}