component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	public function beforeAll(){
		variables.ts=getTimeZone();
	}
	
	public function afterAll(){
		setTimezone(variables.ts);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4772", body=function() {
			it(title = "checking if we can load a component from an old lar file", body = function( currentSpec ) {
				
				// set old archive as mapping
				var curr=getDirectoryFromPath(getCurrentTemplatePath());
				var parent=getDirectoryFromPath(mid(curr,1,len(curr)-1));
				var art=parent&"artifacts/lars/lucee-5.lar"; 
				expect( fileExists(art) ).toBeTrue();	
				admin 
						action="updateComponentMapping"
						type="web"
						password=request.WEBADMINPASSWORD
						virtual="/test4772"
						primary="archive"
						physical=""
						archive=art;
				
				
				var test=new org.lucee.test.Test();
				expect( test.foo() ).toBe("bar");	
			});

			it(title = "LDEV-4975 checking if we can load a component from an old lex file", skip=false, body = function( currentSpec ) {
				
				var lex = "https://github.com/Leftbower/cfspreadsheet-lucee-5/releases/download/v3.0.3/cfspreadsheet-lucee-5.lex";
				var lexObj = fileReadBinary(lex);
				var lexFile = getTempFile(getTempDirectory(),"ldev-4975-cfspreadsheet-lucee-5", "lex");
				fileWrite(lexFile, lexObj);
				
				admin 
					action="updateRHExtension"
					type="web"
					password=request.WEBADMINPASSWORD
					source=lexfile;

				var workbook = createObject("java", "org.apache.poi.hssf.usermodel.HSSFWorkbook", "cfspreadsheet", "3.0.1");

				var qry = queryNew("id,name");
				queryAddRow(qry, {id=1,name="Bob"});
				var spreadsheet = spreadsheetNew("test");  // createObject("java", javaclass, "cfspreadsheet", "3.0.1") errors
				spreadsheetAddRows( spreadsheet, qry );
				SpreadsheetWrite( spreadsheet, "test.xls", true );
			});

		});
	}
} 