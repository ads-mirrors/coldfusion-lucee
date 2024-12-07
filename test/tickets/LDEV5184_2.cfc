component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title='LDEV-5184', body=function(){
			it( title='check if the child threads are linked or not', body=function() {
				request.testReq = "test5148";
				variables.result = runAsync(() => return request).get();
				
				expect(request.testReq?:"undefined").toBe("test5148");
				expect(result.testReq?:"undefined").toBe("test5148");
				structDelete(request, "testReq");
			});
		});
	}
}