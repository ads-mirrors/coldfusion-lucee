component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {
		describe("FileOpen Function", function() {
			it("should open a file for reading", function() {
				var testFilePath = getTempFile(getTempDirectory(), "fileOpen", "txt");
				fileWrite(testFilePath, "Test content");

				var fileObj = fileOpen( testFilePath, "read" );
				expect( fileObj.getMode() ).toBe( "read" );
				expect( fileObj.getStatus() ).toBe( "open" );

				expect( fileObj.name ).toBe( listLast( testFilePath, "\/" ) );
				expect( fileObj.path & server.separator.file ).toBe( getDirectoryFromPath( testFilePath )  );

				expect ( function(){
					fileWrite( fileObj, "Test content"); // file handle is in read mode
				}).toThrow();

				fileClose( fileObj );
				expect( fileObj.getStatus() ).toBe( "close" );

				fileDelete( testFilePath );
			});

			it("should open a file for writing", function() {
				var testFilePath = getTempFile(getTempDirectory(), "fileOpen", "txt");

				var fileObj = fileOpen( testFilePath, "write" );
				expect(fileObj.getMode()).toBe("write");

				fileWrite(fileObj, "Test write content");
				fileClose( fileObj );

				expect ( fileRead( fileObj ) ).toBe( "Test write content" );
				fileDelete( testFilePath );
			});

			xit("should have metadata", function() {
				var testFilePath = getTempFile( getTempDirectory(), "fileOpen", "txt" );

				var fileObj = fileOpen( testFilePath, "write" );
				var meta = getMetaData( fileObj );

				expect( structCount( fileObj.getMetaData() ) ).toBe( 6 ); // returns empty struct
				expect( structCount( meta ) ).toBe( 6 ); // returns empty struct

				fileClose( fileObj );
				fileDelete( testFilePath );
			});

			it("size should not include bytes", function() {
				var testFilePath = getTempFile( getTempDirectory(), "fileOpen", "txt" );

				var fileObj = fileOpen( testFilePath, "write" );

				expect( fileObj.getSize() ).NotToInclude( "bytes" );
				expect( fileObj.getSize() ).toBe( 0 );
				expect( fileObj.size ).ToBe( 0 );

				fileClose( fileObj );
				fileDelete( testFilePath );
			});

			it("should throw an error when opening a non-existent file for reading", function() {
				expect(function() {
					fileOpen("nonexistent.txt", "read");
				}).toThrow();
			});
		});
	}

}