component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run ( testResults , testbox ){
		describe( title="Testcase for LDEV-3167", body=function() {
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
            });
			afterEach( function(){
                setTimeZone(variables.startingTZ?:"UTC");
            });
			it( title = "Check with invalid struct member function", body = function( currentSpec ){
				var str = { one : "one",two : "two" };
				try{
					var errorMsg = str.tokey();
				}
				catch (any e){
					var errorMsg = e.message;
					var errorDetail = e.Detail;
				}

				// append,clear,copy,count,delete,duplicate,each,every,filter,find,findKey,findValue,insert,isEmpty,keyArray,keyExists,keyList,keyTranslate,len,map,reduce,some,sort,update
				expect(findNocase("does not exist in the Struct",errorMsg)>0).toBe(true);
				expect(findNocase("toJson",errorDetail)>0).toBe(true);
				expect(findNocase("keyList",errorDetail)>0).toBe(true);
			});

			// skip this iteration because when the member function count reaches 51 the error message doesn't append the list of the available functions
			it( title = "Check with invalid array member function", skip=true, body = function( currentSpec ){
				var arr = [1,2,3,4];
				try{
					var errorMsg = arr.tokey();
				}
				catch (any e){
					var errorMsg = e.message;
					var errorDetail = e.Detail;
				}
				expect(findNocase("does not exist in the Array",errorMsg)>0).toBe(true);
				expect(findNocase("toJson",errorDetail)>0).toBe(true);
				expect(findNocase("indexExists",errorDetail)>0).toBe(true);
			});
			
			it( title = "Check with invalid date member function", body = function( currentSpec ){
				var date = now();
				try{
					var errorMsg = date.tokey();
				}
				catch (any e){
					var errorMsg = e.message;
					var errorDetail = e.Detail;
				}
				expect(findNocase("does not exist in the Datetime",errorMsg)>0).toBe(true);
				expect(findNocase("toJson",errorDetail)>0).toBe(true);
				expect(findNocase("dayOfWeek",errorDetail)>0).toBe(true);
			});	
		});
	}
}