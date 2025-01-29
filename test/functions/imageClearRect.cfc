component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll() {
		variables.path = getTempDirectory() & "imageClearRect/";
		if(!directoryExists(path)){
			directoryCreate(path);
		}
	}

	function run( testResults , testBox ) {
		describe( title = "Test suite for imageClearRect", body = function() {

			it( title = 'Checking with imageClearRect()',body = function( currentSpec ) {
				var img = imageRead("../artifacts/images/lucee.png");
				ImageClearRect(img,100,100,100,100);
				cfimage(action = "write", source = img, destination = path&".\rect.png", overwrite = "yes");
				assertEquals(fileexists(path&".\rect.png"),"true");
			});

			it( title = 'Checking with image.ClearRect()', body = function( currentSpec ) {
				var img1 = imageRead("../artifacts/images/lucee.png");
				img1.ClearRect(100,100,100,100);
				cfimage(action = "write", source = img1, destination = path&".\rect1.png", overwrite = "yes");
				assertEquals(fileExists(path&".\rect1.png"),"true");
			});

		});
	}

	function afterAll() {
		if(directoryExists(path)) {
			directoryDelete(path,true);
		}
	}
}