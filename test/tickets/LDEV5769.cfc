component extends="org.lucee.cfml.test.LuceeTestCase" {

    function run(testResults, testBox) {
        describe( title = "Testcase for LDEV-5768: Only negative currency formatting for USD ($) should use brackets", body = function() {

            it( title = "International format for negative EUR (de_DE) should return a negative value", skip=true, body = function(currentSpec) {
                var result = LSCurrencyFormat(-1, "international", "de_DE");
                expect(result).toBe("EUR -1,00");
            });

            it( title ="None format for negative EUR (de_DE) should return a negative value", skip=true, body = function(currentSpec) {
                var result = LSCurrencyFormat(-1, "none", "de_DE");
                expect(result).toBe("-1,00");
            });

            it( title = "Local format for negative USD (en_US) should return the value in brackets", body = function(currentSpec) {
                var result = LSCurrencyFormat(-1, "local", "en_US");
                expect(result).toBe("($1.00)");
            });

            it( title = "None format for negative USD (en_US) should return the value in brackets", body = function(currentSpec) {
                var result = LSCurrencyFormat(-1, "none", "en_US");
                expect(result).toBe("(1.00)");
            });

        });
    }

}