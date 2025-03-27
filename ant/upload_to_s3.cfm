<cfscript>
	// firstly, check are we even deploying to s3
	DO_DEPLOY = server.system.environment.DO_DEPLOY ?: false;

	
	_logger( "" );
	_logger( " #### Publish Builds to S3" );

	// secondly, do we have the s3 extension?
	s3ExtVersion = extensionList().filter( function(row){ return row.name contains "s3"; }).version;
	if ( s3Extversion eq "" ){
		_logger( "ERROR! The S3 Extension isn't installed!" );
		return;
		//throw "The S3 Extension isn't installed!"; // fatal
	} else {
		_logger( "Using S3 Extension: #s3ExtVersion#" );
	}

	// finally check for S3 credentials
	if ( isNull( server.system.environment.S3_ACCESS_ID_DOWNLOAD )
			|| isNull( server.system.environment.S3_SECRET_KEY_DOWNLOAD ) ) {
		if ( DO_DEPLOY ){
			_logger( "no S3 credentials defined to upload to S3");
			return;
		}
		//throw "no S3 credentials defined to upload to S3";
		//trg.dir = "";
	}

	NL = "
";

	src.jar = server.system.properties.luceejar;
	src.core = server.system.properties.luceeCore;
	src.dir = getDirectoryFromPath( src.jar );
	src.jarName = listLast( src.jar,"\/" );
	src.coreName = listLast( src.core,"\/" );
	src.version = mid( src.coreName,1,len( src.coreName )-4 );

	if ( ! FileExists( src.jar ) || ! FileExists( src.core ) ){
		_logger( src.jar & " exists: " & FileExists( src.jar ) );
		_logger( src.core & " exists: " & FileExists( src.core ) );
		throw "missing jar or .lco file";
	}

	trg = {};

	// test s3 access
	if ( DO_DEPLOY ) {
		s3_bucket = "lucee-downloads";
		trg.dir = "s3://#server.system.environment.S3_ACCESS_ID_DOWNLOAD#:#server.system.environment.S3_SECRET_KEY_DOWNLOAD#@/#s3_bucket#/";
		trg.jar = trg.dir & src.jarName;
		trg.core = trg.dir & src.coreName;

		
		_logger( "Testing S3 Bucket Access" );
		// it usually will throw an error, rather than even reach this throw, if it fails
		if (!s3exists( bucketName:s3_bucket, accessKeyId:server.system.environment.S3_ACCESS_ID_DOWNLOAD, secretAccessKey:server.system.environment.S3_SECRET_KEY_DOWNLOAD) &&
		! DirectoryExists( trg.dir ) )
			_logger( "DirectoryExists failed for s3 bucket [#s3_bucket#]", true );
	} else {
		_logger( "Not publishing to S3 as DO_DEPLOY is false, only building Light and Zero" );
	}

	// we only upload / publish artifacts once LDEV-3921
	buildExistsOnS3 = false;
	if ( DO_DEPLOY && fileExists( trg.jar ) && fileExists( trg.core ) ){
		_logger( "Build artifacts have already been uploaded to s3 for this version" );
		buildExistsOnS3 = true;
	}

	if ( DO_DEPLOY && !buildExistsOnS3 ){
		// copy jar
		publishToS3( src.jar, trg.jar, "Publish [#src.jar#] to S3: ");
		// copy core
		publishToS3( src.core, trg.core, "Publish [#src.core#] to S3: ");
	}

	// Lucee light build (no extensions)
	src.lightName = "lucee-light-" & src.version & ".jar";
	src.light = src.dir & src.lightName;
	if ( DO_DEPLOY ){
		if ( !buildExistsOnS3 ){
			_logger( "Build and upload [#src.light#] to S3 / maven" );
		} else {
			_logger( "Build and upload [#src.light#] to maven (already published to s3)" );
		}
	} else {
		_logger( "Build #src.light#" );
	}

	createLight( src.jar,src.light,src.version, false );
	if ( DO_DEPLOY && !buildExistsOnS3 ){
		trg.light = trg.dir & src.lightName;
		publishToS3( src.light, trg.light, "Publish Light build to s3: ");
	}

	// Lucee zero build, built from light but also no admin or docs
	src.zeroName = "lucee-zero-" & src.version & ".jar";
	src.zero = src.dir & src.zeroName;
	if ( DO_DEPLOY ){
		if ( !buildExistsOnS3 ){
			_logger( "Build and upload [#src.zero#] to S3 / maven" );
		} else{
			_logger( "Build and upload [#src.zero#] to maven (already published to s3)" );
		}
	} else {
		_logger( "Build #src.zero#"  );
	}

	createLight( src.light, src.zero, src.version, true );

	if ( DO_DEPLOY && !buildExistsOnS3 ) {
		trg.zero = trg.dir & src.zeroName;
		publishToS3( src.zero, trg.zero, "Publish Zero build to s3: " );
	}

	if ( !DO_DEPLOY ){
		_logger( "Skipping build triggers, DO_DEPLOY is false" );
		return;
	}

	// update provider
	_logger("Trigger builds" );
	http url="https://update.lucee.org/rest/update/provider/buildLatest" method="GET" timeout=90 result="buildLatest";
	_logger(buildLatest.fileContent );

	_logger("Update Extension Provider" );
	http url="https://extension.lucee.org/rest/extension/provider/reset" method="GET" timeout=90 result="extensionReset";
	_logger(extensionReset.fileContent );

	_logger("Update Downloads Page" );
	http url="https://download.lucee.org/?type=snapshots&reset=force" method="GET" timeout=90 result="downloadUpdate";
	_logger("Server response status code: " & downloadUpdate.statusCode );

	// forgebox

	_logger("Trigger forgebox builds" );

	gha_pat_token = server.system.environment.LUCEE_DOCKER_FILES_PAT_TOKEN; // github person action token
	body = {
		"event_type": "forgebox_deploy"
	};
	try {
		http url="https://api.github.com/repos/Ortus-Lucee/forgebox-cfengine-publisher/dispatches" method="POST" result="result" timeout="90"{
			httpparam type="header" name='authorization' value='Bearer #gha_pat_token#';
			httpparam type="body" value='#body.toJson()#';

		}
		_logger("Forgebox build triggered, #result.statuscode# (always returns a 204 no content, see https://github.com/Ortus-Lucee/forgebox-cfengine-publisher/actions for output)" );
	} catch (e){
		_logger("Forgebox build ERRORED?" );
		echo(e);
	}

	// Lucee Docker builds
	if ( buildExistsOnS3 ){
		_logger("Skip Triggering Lucee Docker builds as build was already published to s3" );
	} else {
		_logger("Triggering Lucee Docker builds [#server.system.properties.luceeVersion#]" );

		gha_pat_token = server.system.environment.LUCEE_DOCKER_FILES_PAT_TOKEN; // github person action token
		body = {
			"event_type": "build-docker-images",
			"client_payload": {
				"LUCEE_VERSION": server.system.properties.luceeVersion
			}
		};
		try {
			http url="https://api.github.com/repos/lucee/lucee-dockerfiles/dispatches" method="POST" result="result" timeout="90"{
				httpparam type="header" name='authorization' value='Bearer #gha_pat_token#';
				httpparam type="body" value='#body.toJson()#';
			}
			_logger("Lucee Docker builds triggered, #result.statuscode# (always returns a 204 no content, see https://github.com/lucee/lucee-dockerfiles/actions for output)" );
		} catch (e){
			_logger("Lucee Docker build ERRORED?" );
			echo(e);
		}
	}

	// express

	private function createLight( string loader, string trg, version, boolean noArchives=false ) {
		var sep = server.separator.file;
		var tmpDir = getTempDirectory();

		local.tmpLoader = tmpDir & "lucee-loader-" & createUniqueId(  ); // the jar
		if ( directoryExists( tmpLoader ) )
			directoryDelete( tmpLoader,true );
		directoryCreate( tmpLoader );

		// unzip
		zip action = "unzip" file = loader destination = tmpLoader;

		// remove extensions
		var extDir = tmpLoader&sep&"extensions";
		if ( directoryExists( extDir ) )
			directoryDelete( extDir, true ); // deletes directory with all files inside
		directoryCreate( extDir ); // create empty dir again ( maybe Lucee expect this directory to exist )

		// unzip core
		var lcoFile = tmpLoader & sep & "core" & sep & "core.lco";
		local.tmpCore = tmpDir & "lucee-core-" & createUniqueId(  ); // the jar
		directoryCreate( tmpCore );
		zip action = "unzip" file = lcoFile destination = tmpCore;

		if (arguments.noArchives) {
			// delete the lucee-admin.lar and lucee-docs.lar
			var lightContext =  tmpCore & sep & "resource/context" & sep;
			loop list="lucee-admin.lar,lucee-doc.lar" item="local.larFile" {
				fileDelete( lightContext & larFile );
			}
		}

		// rewrite manifest
		var manifest = tmpCore & sep & "META-INF" & sep & "MANIFEST.MF";
		var content = fileRead( manifest );
		var index = find( 'Require-Extension',content );
		if ( index > 0 )
			content = mid( content, 1, index - 1 ) & variables.NL;
		fileWrite( manifest,content );

		// zip core
		fileDelete( lcoFile );
		zip action = "zip" source = tmpCore file = lcoFile;

		// zip loader
		if ( fileExists( trg ) )
			fileDelete( trg );
		zip action = "zip" source = tmpLoader file = trg;

	}

	function _logger( string message="", boolean throw=false ){
		systemOutput( arguments.message, true );
		if ( !len( server.system.environment.GITHUB_STEP_SUMMARY?:"" ))
			return;
		if ( !FileExists( server.system.environment.GITHUB_STEP_SUMMARY  ) ){
			fileWrite( server.system.environment.GITHUB_STEP_SUMMARY, "#### #server.lucee.version# ");
		}

		if ( arguments.throw ) {
			fileAppend( server.system.environment.GITHUB_STEP_SUMMARY, "> [!WARNING]" & chr(10) );
			fileAppend( server.system.environment.GITHUB_STEP_SUMMARY, "> #arguments.message##chr(10)#");
			throw arguments.message;
		} else {
			fileAppend( server.system.environment.GITHUB_STEP_SUMMARY, " #arguments.message##chr(10)#");
		}
	}

	function publishToS3( src, trg, mess ){
		var copyIt=true;
		if ( fileExists( trg ) ) {
			try {
				fileDelete( trg );
			}
			catch(e){
				_logger( message=mess & " file deleted failed" );
				copyIt=false;
			}
		}
		if( copyIt ){
			try {
				fileCopy( src, trg );
			} catch (e){
				// censored error message due to s3 creds!
				 _logger( message=mess & " file copy failed", throw=true );
			}
			_logger( mess & " SUCCESS");
		}
	}


	// not used
	private function createWAR( string loader, string trg, version, boolean noArchives=false ) {
		/*
		// create war
		src.warName = "lucee-" & src.version & ".war";
		src.war = src.dir & src.warName;
		trg.war = trg.dir & src.warName;


		_logger( "upload #src.warName# to S3" );
		zip action = "zip" file = src.war overwrite = true {

			// loader
			zipparam source = src.jar entrypath = "WEB-INF/lib/lucee.jar";

			// common files
			// zipparam source = commonDir;

			// website files
			// zipparam source = webDir;

			// war files
			// zipparam source = warDir;
		}
		fileCopy( src.war,trg.war );
		*/
	}
</cfscript>