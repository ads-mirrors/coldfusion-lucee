component extends = "org.lucee.cfml.test.LuceeTestCase" skip=false {
    function run( testResults, textbox ) {
        describe("testcase for LDEV-5238", function(){
            it(title="Convert java object int[] into string", body=function( currentSpec ){
                arrayInput = [1,2,3,4,5];
                castedArrayInt = javaCast("Int[]", arrayInput);
                expect( function() {
					castedArrayInt.toString();
				} ).notToThrow();

                expect( castedArrayInt[1] ).toBeTypeOf("Integer");
                expect( javaCast("java.lang.Integer[]", arrayInput).toString() ).toInclude("Integer");
            });
        });
    }
}