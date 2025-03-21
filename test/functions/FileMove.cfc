component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.name=ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
		variables.dir=getDirectoryFromPath(getCurrentTemplatePath())&name&"/";
		if(directoryExists(dir)){
			afterAll();
		}
		cfdirectory(directory="#dir#" action="create" mode="777");
	}

	function run( testResults , testBox ) {
		describe( "test case for fileMove", function() {
			it(title = "Checking with fileMove", body = function( currentSpec ) {
				<!--- begin old test code --->
				var srcDir=dir&"src/";
				var trgDir=dir&"trg/";
				cfdirectory(directory="#srcDir#" action="create" mode="777");
				cfdirectory(directory="#trgDir#" action="create" mode="777");
				// define paths
				var src=srcDir&"test.txt";
				var dest1=trgDir&"testx.txt";
				var dest3=trgDir&'test.txt';
				assertEquals(FileExists(dest1),false);
				assertEquals(FileExists(dest3),false);
				// copy with destination file
				if(!FileExists(src))fileWrite(src,"ABC");
				fileMove(src,dest1);
				// copy with destination dir
				if(!FileExists(src))fileWrite(src,"ABC");
				fileMove(src,trgDir);
				assertEquals(FileExists(dest1),true);
				assertEquals(FileExists(dest3),true);
			});
		});
	}
	function afterAll(){
		directorydelete(dir,true);
	}
}