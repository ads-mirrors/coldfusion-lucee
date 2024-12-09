component extends = "org.lucee.cfml.test.LuceeTestCase" labels="struct" {
	function run( testResults , testBox ) {
		describe( title = "Test suite for structUpdate", body = function() {
			var student = [ name:"student1", id="1", class="A" ];
			it( title = 'Testcase for structUpdate function',body = function( currentSpec ) {
				assertEquals('true', structUpdate(student, "name", "student2"));
			});

			it( title = 'Test case for struct.Update member function',body = function( currentSpec ) {
				var res=student.update("class", "B");

				assertEquals(len(res),3);
				assertEquals(res.name,"student2");
				assertEquals(res.id,"1");
				assertEquals(res.class,"B");
			});
		});
	}
}