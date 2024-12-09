component extends="org.lucee.cfml.test.LuceeTestCase" {
	student = [
		student1:[
			id:1,
			name:"joe"
		],
		student2:[
			id:2,
			name:"root"
		],
		student3:[
			id:3,
			name:"jack"
		]
	]
	function run( testResults , testBox ) {
		describe( title = "Test suite for structfindkey", body = function() {

			it( title = 'Test case for structfindkey function',body = function( currentSpec ) {
				var res=structfindkey(student,"id","all");
				
				assertEquals(3,len(res));
				assertEquals(".STUDENT1.ID",res[1].path);
				assertEquals("joe",res[1].owner.name);
				assertEquals(1,res[1].owner.ID);
				assertEquals(1,res[1].value);

				assertEquals(".STUDENT2.ID",res[2].path);
				assertEquals("root",res[2].owner.name);
				assertEquals(2,res[2].owner.ID);
				assertEquals(2,res[2].value);

				assertEquals(".STUDENT3.ID",res[3].path);
				assertEquals("jack",res[3].owner.name);
				assertEquals(3,res[3].owner.ID);
				assertEquals(3,res[3].value);
				
				var res=structfindkey(student,"id");
				
				assertEquals(1,len(res));
				assertEquals(".STUDENT1.ID",res[1].path);
				assertEquals("joe",res[1].owner.name);
				assertEquals(1,res[1].owner.ID);
				assertEquals(1,res[1].value);
			});

			it( title = 'Test case for structfindkey member function',body = function( currentSpec ) {
				var res=student.findkey("id","all");
				
				assertEquals(3,len(res));
				assertEquals(".STUDENT1.ID",res[1].path);
				assertEquals("joe",res[1].owner.name);
				assertEquals(1,res[1].owner.ID);
				assertEquals(1,res[1].value);

				assertEquals(".STUDENT2.ID",res[2].path);
				assertEquals("root",res[2].owner.name);
				assertEquals(2,res[2].owner.ID);
				assertEquals(2,res[2].value);

				assertEquals(".STUDENT3.ID",res[3].path);
				assertEquals("jack",res[3].owner.name);
				assertEquals(3,res[3].owner.ID);
				assertEquals(3,res[3].value);

				var res=student.findkey("id");
				
				assertEquals(1,len(res));
				assertEquals(".STUDENT1.ID",res[1].path);
				assertEquals("joe",res[1].owner.name);
				assertEquals(1,res[1].owner.ID);
				assertEquals(1,res[1].value);
			});
		});
	}
}