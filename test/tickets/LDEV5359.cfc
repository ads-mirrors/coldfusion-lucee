component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( title='ExpandPath regressions ("*.*")' , body=function(){

			it( title='ExpandPath fails with ("*.*")', body=function() {
				var starDotStar = ExpandPath("*.*");
				var root = ExpandPath("/");
				expect(starDotStar).toBe(root & "*.*");
			});

			it( title='ExpandPath ("*")', body=function() {
				var starDotStar = ExpandPath("*");
				var root = ExpandPath("/");
				expect(starDotStar).toBe(root & "*");
			});

		});
	}
}