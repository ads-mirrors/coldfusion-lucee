component extends="org.lucee.cfml.test.LuceeTestCase" {

    function beforeAll() {
        variables.timezone=getTimeZone();
        variables.locale=getLocale();
    }
    function afterAll() {
        setTimeZone(variables.timezone);
        setLocale(variables.locale);
    }

	function run( testResults, testBox ){
		describe( "Test for LDEV-5650", function(){

			it( "test dateTimeFormat", function() {
                setTimeZone("Europe/Helsinki");
				expect(dateTimeFormat(createTime(23,39,49),"iso")).toBe( "1899-12-30T23:39:49+02:00" );
				expect(dateTimeFormat(createTime(23,39,49),"yyy-mm-dd HH:nn:ss")).toBe( "1899-12-30 23:39:49" );
                
                setTimeZone("Europe/Berlin");
				expect(dateTimeFormat(createTime(23,39,49),"iso")).toBe( "1899-12-30T23:39:49+01:00" );
				expect(dateTimeFormat(createTime(23,39,49),"yyy-mm-dd HH:nn:ss")).toBe( "1899-12-30 23:39:49" );
                
                setTimeZone("Pacific/Auckland");
				expect(dateTimeFormat(createTime(23,39,49),"iso")).toBe( "1899-12-30T23:39:49+12:00" );
				expect(dateTimeFormat(createTime(23,39,49),"yyy-mm-dd HH:nn:ss")).toBe( "1899-12-30 23:39:49" );
			});



			it( "test timeFormat", function() {
                setTimeZone("Europe/Helsinki");
				expect(timeFormat(createTime(23,39,49),"HH:nn:ss")).toBe( "23:39:49" );
			});
			it( "test lsDateTimeFormat", function() {
                setTimeZone("Europe/Helsinki");
                setLocale("en_US");
				expect(lsDateTimeFormat(createTime(23,39,49),"iso")).toBe( "1899-12-30T23:39:49+02:00" );
				expect(lsDateTimeFormat(createTime(23,39,49),"yyy-mm-dd HH:nn:ss")).toBe( "1899-12-30 23:39:49" );
			});
			it( "test lsTimeFormat", function() {
                setTimeZone("Europe/Helsinki");
                setLocale("en_US");
				expect(lsTimeFormat(createTime(23,39,49),"HH:nn:ss")).toBe( "23:39:49" );
			});

		} );
	}
}