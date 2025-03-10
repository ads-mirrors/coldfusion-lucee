component extends="org.lucee.cfml.test.LuceeTestCase" {
    
		function run() {
			describe("LDEV-5382 - GetHTTPTimeString Tests", function() {
				
				it("should format date without hyphens", function() {
					// Get current date for testing
					var testDate = createDateTime(year:2023, month:10, day:15, hour:14, minute:30, second:45,timezone:"UTC");
					
					// Get the HTTP time string
					var httpTimeStr = getHTTPTimeString(testDate);
					
					expect(httpTimeStr).toBe("Sun, 15 Oct 2023 14:30:45 GMT");

					expect(testDate).toBe(httpTimeStr);
				
				});
			});
		}
	}