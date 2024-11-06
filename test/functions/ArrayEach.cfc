component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayEach()", body=function() {
			it(title="checking ArrayEach() function", body = function( currentSpec ) {
				var arr=["hello","world"];

				request.arrayEach=[];

				ArrayEach(arr,eachFilter);
				assertEquals('hello,world', arrayToList(request.arrayEach));

				// Closure
				var arr=["hello","world"];
				request.arrayEach=[];
				sseachFilter=function (arg1){
					arrayAppend(request.arrayEach,arg1);
				};
				ArrayEach(arr,eachFilter);
				assertEquals('hello,world', arrayToList(request.arrayEach));
			});
		});
	}


	private function eachFilter(arg1){
		arrayAppend(request.arrayEach,arg1);
	}

	function afterAll(){
		structDelete(request, "arrayEach");
	};
}