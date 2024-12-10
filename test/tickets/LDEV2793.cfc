component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function beforeAll(){
		variables.preciseMath = getApplicationSettings().preciseMath;
	};

	function afterAll(){
		application action="update" precisemath=variables.preciseMath;
	};

	function run( testResults , testBox ) {
		describe( title='LDEV-2793' , body=function(){
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
            });
			afterEach( function(){
                setTimeZone(variables.startingTZ?:"UTC");
            });
			it( title='test parseDateTime (preciseMath=true)' , body=function() {
				application action="update" preciseMath=true;
				var projects = [
					{
						id: 1,
						name: "Really old project",
						createdAt: createDate( 2015, 12, 15 ).getTime() // 1450155600000
					},
					{
						id: 500,
						name: "Recent project",
						createdAt: createDate( 2019, 10, 30 ).getTime() // 1572408000000
					},
					{
						id: 1000,
						name: "Current project",
						createdAt: createDate( 2020, 02, 26 ).getTime() // 1582693200000
					}
				];
				projects.sort(
					( a, b ) => {
						return( b.createdAt - a.createdAt );
					}
				);

			});

			it( title='test parseDateTime (preciseMath=false)',  body=function() {
				application action="update" preciseMath=false;
				var projects = [
					{
						id: 1,
						name: "Really old project",
						createdAt: createDate( 2015, 12, 15 ).getTime() // 1450155600000
					},
					{
						id: 500,
						name: "Recent project",
						createdAt: createDate( 2019, 10, 30 ).getTime() // 1572408000000
					},
					{
						id: 1000,
						name: "Current project",
						createdAt: createDate( 2020, 02, 26 ).getTime() // 1582693200000
					}
				];
				// errors, see LDEV-5178
				projects.sort(
					( a, b ) => {
						return( b.createdAt - a.createdAt );
					}
				);

			});
		});
	}
}