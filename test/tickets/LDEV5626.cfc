component extends="org.lucee.cfml.test.LuceeTestCase" {

    function run( testResults , testBox ) {
        describe( title="Testcase for LDEV-5626", body=function() {

            it( title="load a Java class using java.lang.String::class", body=function( currentSpec ) {
                var clazz = "";
                try {
                    clazz = java.lang.String::class.getName();
                } catch (any error) {
                    clazz = error.message;
                }
                expect(clazz).toBe("java.lang.String");
            });

        });
    }

}
