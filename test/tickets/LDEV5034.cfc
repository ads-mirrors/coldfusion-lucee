component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.baseDir = getTempDirectory() & "LDEV-5034/";
		if ( directoryExists( baseDir ) )
			directoryDelete( baseDir, true );
		directoryCreate( baseDir );
	};

	function afterAll(){
		if ( directoryExists( baseDir ) ){
			directoryDelete( baseDir, true );
		};
	};

	function run( testResults, testBox ){
		describe( "Round trip a set of files and dirs thru compress and extract tar", function(){
			it( title="just files", skip=isNotUnix(), body=function(){
				var srcDir = getTestDir( "files" );
				var destDir = getTestDir( "files-extract" );
				var sourceFiles = [];
				// create some files
				arrayAppend( sourceFiles, _file( srcDir, "644.txt", "644" ) );
				arrayAppend( sourceFiles, _file( srcDir, "743.txt", "743" ) );
				arrayAppend( sourceFiles, _file( srcDir, "043.txt", "443" ) );
				arrayAppend( sourceFiles, _file( srcDir, "400.txt", "400" ) );

				testRoundTripCompressExtract( srcDir, destDir, sourceFiles );
			});

			it( title="file and directory", skip=isNotUnix(), body=function(){
				var srcDir = getTestDir( "mixed2" );
				var destDir = getTestDir( "mixed2-extract" );
				var sourceFiles = [];
				arrayAppend( sourceFiles, _dir(srcDir, "dir-755", "755" ) );
				arrayAppend( sourceFiles, _file(srcDir, "644.txt", "644" ) );
				arrayAppend( sourceFiles, _file(srcDir & "dir-755/", "755-400.txt", "400" ) );

				testRoundTripCompressExtract( srcDir, destDir, sourceFiles );
			});

			it( title="files and directories", skip=isNotUnix(), body=function(){
				var srcDir = getTestDir( "mixed" );
				var destDir = getTestDir( "mixed-extract" );
				var sourceFiles = [];
				// create some dirs
				arrayAppend( sourceFiles, _dir(srcDir, "dir-755", "755" ) );
				arrayAppend( sourceFiles, _dir(srcDir, "dir-777", "777" ) );
				arrayAppend( sourceFiles, _dir(srcDir, "dir-743", "743" ) );
				// create some files
				arrayAppend( sourceFiles, _file(srcDir, "644.txt", "644" ) );
				arrayAppend( sourceFiles, _file(srcDir, "743.txt", "743" ) );
				arrayAppend( sourceFiles, _file(srcDir, "043.txt", "443" ) );
				arrayAppend( sourceFiles, _file(srcDir, "400.txt", "400" ) );
				// emptyDirs are skipped, add some files to them
				arrayAppend( sourceFiles, _file(srcDir & "dir-777/", "777-443.txt", "743" ) );
				arrayAppend( sourceFiles, _file(srcDir & "dir-755/", "755-400.txt", "400" ) );
				arrayAppend( sourceFiles, _file(srcDir & "dir-743/", "743-777.txt", "777" ) );

				testRoundTripCompressExtract( srcDir, destDir, sourceFiles );
			});
			// only disabled for faster CI
			xit( title="test tomcat", skip=isNotUnix(), body=function(){
				var download_url =" https://dlcdn.apache.org/tomcat/tomcat-11/v11.0.2/bin/apache-tomcat-11.0.2.tar.gz";
				var filename = listLast( download_url, "/" );
				var srcDir = getTestDir( "tomcat" );
				var destDir = getTestDir( "tomcat-extract" );
				http method="get" url=download_url path=getTempDirectory() file=filename throwOnError=true;
				var info = fileInfo(getTempDirectory() & filename);
				systemOutput(info, true);
				extract(format="tgz", source=info.path, target=srcDir);
				var sourceFiles = folderToTest( srcDir );
				// for (var s in sourceFiles) systemOutput(s, true);
				testRoundTripCompressExtract( srcDir, destDir, sourceFiles );
			});

		} );
	}

	private function testRoundTripCompressExtract( string srcDir, string destDir,
			array sourceFiles, boolean debug=false ){
		if (arrayLen( arguments.sourceFiles ) == 0)
			throw "testRoundTripCompressExtract: sourceFiles was empty?";
		if ( arguments.debug ){
			systemOutput( "-----------------source files", true );
			loop array=arguments.sourceFiles item="local.sourceFile" {
				structDelete( sourceFile, "dateLastModified" );
				systemOutput( sourceFile, true );
			}
		}

		// compress the source files into a tar archive
		var tarFile = getTempFile( getTempDirectory(), "LDEV-5034", ".tar.gz" );
		// mode 0 here is important!!!!! default is 777 and ignore source permissions
		compress( format="tgz", source=arguments.srcDir, target=tarFile, includeBaseFolder=false, mode=0 );

		//systemOutput(tarFile, true);
		var dest = arguments.destDir;
		extract( "tgz", tarFile, dest );

		var files = directoryList( arguments.srcDir, true, "query");
		var extractedFiles = directoryList( dest, true, "query" );
		QueryAddColumn( extractedFiles, "path" );
		loop query=extractedFiles {
			var _path = mid( extractedFiles.directory, len( arguments.destDir ) + 1 );
			if ( len( _path ) eq 0 )
				_path = extractedFiles.name;
			else
				_path = _path & "/" & extractedFiles.name;

			QuerySetCell( extractedFiles, "path", _path, extractedFiles.currentrow );
		}
		if ( arguments.debug ){
			QueryDeleteColumn( extractedFiles, "directory" );
			QueryDeleteColumn( extractedFiles, "dateLastModified" );
			QueryDeleteColumn( extractedFiles, "attributes" );
		}
		var stExtracted = QueryToStruct( extractedFiles, "path" );

		if ( arguments.debug ){
			systemOutput( "-----------------sourceFiles", true );
			systemOutput( files.toString(), true );
			systemOutput( "-----------------extractedFiles", true );
			systemOutput( extractedFiles.toString(), true );
			systemOutput( "-----------------stExtracted", true );
			loop collection=stExtracted key="local.file" value="local.item" {
				systemOutput( item, true );
			}
			systemOutput("-----------------", true);
		}

		var files = justFiles( files );
		var extractedFiles = justFiles( extractedFiles );
		expect( files.recordcount ).toBe( extractedFiles.recordcount );

		// check that after round tripping thru compress() and extract()
		// the final files have the same permissions as the source files
		loop array=arguments.sourceFiles item="local.sourceFile" {
			if ( arguments.debug )
				systemOutput( sourceFile, true );
			var filename = mid( sourceFile.name, len( arguments.srcDir ) + 1 );
			if ( arguments.debug )
				systemOutput( "filename [#filename#]", true );
			expect( stExtracted ).toHaveKey( filename );
			if ( arguments.debug )
				systemOutput( stExtracted[ filename ], true );
			expect( stExtracted[ filename ].mode ).toBe( sourceFile.mode , sourceFile.name );
		}
	}

	private function justFiles( srcQry ) {
		var qry = duplicate( arguments.srcQry );
		for(var row=qry.recordcount; row > 0; row--) {
			if ( qry.type[ row ] != "File" )
				queryDeleteRow( qry, row);
		}
		querySort( qry, "type" );
		return qry;
	}

	private function _dir( parent, name, mode ){
		var dir = parent & name;
		directoryCreate( dir );
		fileSetAccessMode( dir, mode );
		return {
			name: dir,
			mode: mode,
			type: "dir"
		};
	}

	private function _file( parent, name, mode ){
		var file = parent & name;
		fileWrite( file, "" );
		fileSetAccessMode( file, mode );
		return {
			name: file,
			mode: mode,
			type: "file"
		};
	}

	private function folderToTest( srcDir ){
		var files = directoryList( arguments.srcDir, true, "query");
		var sources = [];
		for (var file in files){
			arrayAppend( sources, {
				name: file.directory & "/" & file.name,
				mode: file.mode,
				type: file.type
			} );
		}
		return sources;
	}

	private function isNotUnix(){
		return ( server.os.name contains "windows" );
	}

	private function getTestDir( suffix ){
		var testDir = variables.baseDir & arguments.suffix & "/";
		if ( directoryExists( testDir ) )
			directoryDelete( testDir, true );
		directoryCreate( testDir );
		return testDir;
	};

}
