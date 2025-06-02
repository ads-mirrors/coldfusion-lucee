component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" skip=true {

	function run( testResults , testBox ) {
		describe( title="Test method matching", body=function() {

			it(title="qoq should throw an error when a where clause column doesn't exist", body = function( currentSpec ) {
				var news = queryNew("id,title", "integer,varchar");
				queryAddRow(news);
				querySetCell(news, "id", "1");
				querySetCell(news, "title", "Dewey defeats Truman");
				queryAddRow(news);
				querySetCell(news, "id", "2");
				querySetCell(news, "title", "Men walk on Moon");

				expect(function(){
					```
					<cfquery name="local.q" dbtype="query">
						SELECT *
						FROM news
						WHERE bob = 1
					</cfquery>

					```
				}).toThrow();

			});

	
		});
	}

}