component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) {

		describe( title='context/lib tests' , body=function(){

			it( title='drop jsoup in context/lib', body=function() {
				var contextLibDir = expandPath( "{lucee-server}/lib" );
				http url="https://repo1.maven.org/maven2/org/jsoup/jsoup/1.18.3/jsoup-1.18.3.jar" file="jsoup-1.18.3.jar" path="#contextLibDir#";
				var jSoup = createObject( "java", "org.jsoup.Jsoup" );
				expect( isObject( jSoup ) ).toBe( true );
			});

		});
	}

}