component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {
	
	function beforeAll() {
		variables.urlPath = "http://"&cgi.SERVER_NAME & createURI("LDEV5747/index.cfm");
	}

	function run( testResults, textbox ) {
		describe(title="testcase for LDEV-5747", body=function(){
			it(title = "update a scheduled tasks", body = function ( currentSpec ){
				cfschedule(
					action="update",
					url="#urlPath#",
					task="LDEV-5747",
					interval="daily",
					paused="true",
					startdate="#dateformat(now())#",
					starttime="#timeFormat(now()+1)#"
				);
			});
			it(title = "Concurrent updating of Scheduled tasks", body = function ( currentSpec ){
				var arr = [];
				arraySet(arr, 1, 100, "");
				arrayEach(arr, function(el, idx){
					systemOutput("LDEV-5747 updating scheduled tasks #idx#", true);
					cfschedule(
						action="update",
						url="#urlPath#",
						task="LDEV-5747",
						interval="daily",
						paused="true",
						startdate="#dateformat(now())#",
						starttime="#timeFormat(now()+1)#"
					);
				}, true);
			});
		});
	}

	function afterAll() {
		cfschedule( action="delete", task="LDEV-5747");
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}