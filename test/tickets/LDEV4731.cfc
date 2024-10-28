component extends = "org.lucee.cfml.test.LuceeTestCase" skip=false {

    function run( testResults, testBox ){
        describe( "Testcase for LDEV-4731", function() {
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
            });
			afterEach( function(){
                setTimeZone(variables.startingTZ?:"UTC");
            });
            it( title="check ETC format dates", body=function( currentSpec ) {
                expect (parseDateTime("2023-10-21 04:35:13 Etc/GMT")).toBe(createDateTime(2023, 10, 21, 4, 35, 13,0,"UTC"));
                expect (parseDateTime("2023-10-21 04:35:13 Etc/GMT+1")).toBe(createDateTime(2023, 10, 21, 5, 35, 13,0,"UTC"));
                expect (parseDateTime("2023-10-21 04:35:13 Etc/GMT-1")).toBe(createDateTime(2023, 10, 21, 3, 35, 13,0,"UTC"));
            });
        });
    }
}
