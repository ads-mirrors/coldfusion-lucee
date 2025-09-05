import { S3Client, ListObjectsV2Command, CopyObjectCommand, DeleteObjectCommand, PutObjectCommand, GetObjectCommand, HeadObjectCommand, GetBucketLocationCommand } from '@aws-sdk/client-s3';

// Helper function to log with timestamp
function log(message, level = 'INFO') {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] [${level}] ${message}`);
}

function logError(message) {
  log(message, 'ERROR');
}

function logWarning(message) {
  log(message, 'WARN');
}

async function run() {
  try {
    // Debug: Log all INPUT_ environment variables
    log('Environment variables:');
    Object.keys(process.env)
      .filter(key => key.startsWith('INPUT_'))
      .forEach(key => {
        log(`  ${key}=${process.env[key]}`);
      });

    // Get inputs from environment variables
    const sourceExtension = process.env.INPUT_SOURCE_EXTENSION;
    const targetArtifactId = process.env.INPUT_TARGET_ARTIFACT_ID;
    const versionsInput = process.env.INPUT_VERSIONS;
    const operation = process.env.INPUT_OPERATION || 'move';
    const dryRun = process.env.INPUT_DRY_RUN === 'true';
    const accessKeyId = process.env.INPUT_S3_ACCESS_KEY;
    const secretAccessKey = process.env.INPUT_S3_SECRET_KEY;
    const targetBucket = process.env.INPUT_S3_BUCKET || 'lucee-downloads';
    const sourceBucket = process.env.INPUT_SOURCE_BUCKET || 'extension-downloads';
    const region = process.env.INPUT_S3_REGION || 'us-east-1';

    log(`Source extension: "${sourceExtension}"`);
    log(`Target artifact ID: "${targetArtifactId}"`);
    log(`Versions input: "${versionsInput}"`);

    if (!sourceExtension || sourceExtension.trim() === '') {
      throw new Error('Source extension is required and cannot be empty');
    }

    if (!targetArtifactId || targetArtifactId.trim() === '') {
      throw new Error('Target artifact ID is required and cannot be empty');
    }

    if (!versionsInput || versionsInput.trim() === '') {
      throw new Error('Versions list is required and cannot be empty');
    }
    
    if (!accessKeyId || !secretAccessKey) {
      throw new Error('S3 credentials are required');
    }

    // Parse versions from comma-separated input
    const versions = versionsInput.split(',').map(v => v.trim()).filter(v => v.length > 0);
    
    if (versions.length === 0) {
      throw new Error('No valid versions found in input');
    }

    log(`Starting ${dryRun ? 'DRY RUN of ' : ''}${operation} operation for ${versions.length} version(s): ${versions.join(', ')}`);
    log(`Source Bucket: ${sourceBucket}, Target Bucket: ${targetBucket}, Initial Region: ${region}`);

    // Create initial S3 client to detect bucket region
    let s3Client = new S3Client({
      region,
      credentials: {
        accessKeyId,
        secretAccessKey
      }
    });
    
    // Test S3 connectivity and detect correct region for both buckets
    let sourceS3Client = s3Client;
    let targetS3Client = s3Client;
    
    try {
      log('Testing S3 connectivity and detecting target bucket region...');
      await targetS3Client.send(new ListObjectsV2Command({
        Bucket: targetBucket,
        MaxKeys: 1
      }));
      log('✓ Target S3 connectivity successful');
    } catch (error) {
      if (error.name === 'PermanentRedirect') {
        try {
          log('Target bucket is in different region, detecting correct region...');
          const locationResponse = await targetS3Client.send(new GetBucketLocationCommand({ Bucket: targetBucket }));
          const correctRegion = locationResponse.LocationConstraint || 'us-east-1';
          log(`✓ Detected target bucket region: ${correctRegion}`);
          
          targetS3Client = new S3Client({
            region: correctRegion,
            credentials: {
              accessKeyId,
              secretAccessKey
            }
          });
          
          await targetS3Client.send(new ListObjectsV2Command({
            Bucket: targetBucket,
            MaxKeys: 1
          }));
          log('✓ Target S3 connectivity successful with correct region');
        } catch (regionError) {
          throw new Error(`Failed to detect target bucket region: ${regionError.name} - ${regionError.message}`);
        }
      } else {
        throw new Error(`Target S3 connectivity test failed: ${error.name} - ${error.message}. Check bucket name and credentials.`);
      }
    }

    // Test source bucket connectivity and detect region
    try {
      log('Testing source bucket connectivity...');
      await sourceS3Client.send(new ListObjectsV2Command({
        Bucket: sourceBucket,
        MaxKeys: 1
      }));
      log('✓ Source S3 connectivity successful');
    } catch (error) {
      if (error.name === 'PermanentRedirect') {
        try {
          log('Source bucket is in different region, detecting correct region...');
          const locationResponse = await sourceS3Client.send(new GetBucketLocationCommand({ Bucket: sourceBucket }));
          const correctRegion = locationResponse.LocationConstraint || 'us-east-1';
          log(`✓ Detected source bucket region: ${correctRegion}`);
          
          sourceS3Client = new S3Client({
            region: correctRegion,
            credentials: {
              accessKeyId,
              secretAccessKey
            }
          });
          
          await sourceS3Client.send(new ListObjectsV2Command({
            Bucket: sourceBucket,
            MaxKeys: 1
          }));
          log('✓ Source S3 connectivity successful with correct region');
        } catch (regionError) {
          throw new Error(`Failed to detect source bucket region: ${regionError.name} - ${regionError.message}`);
        }
      } else {
        throw new Error(`Source S3 connectivity test failed: ${error.name} - ${error.message}. Check bucket name and credentials.`);
      }
    }

    // Overall statistics
    let totalProcessed = 0;
    let totalMissing = 0;
    let totalSkipped = 0;
    let totalErrors = 0;
    let successfulVersions = 0;

    // Process each version
    for (let i = 0; i < versions.length; i++) {
      const version = versions[i];
      log(`\n${'='.repeat(60)}`);
      log(`Processing version ${i + 1}/${versions.length}: ${version}`);
      log(`${'='.repeat(60)}`);

      try {
        const versionStats = await processExtensionVersion(sourceS3Client, targetS3Client, sourceBucket, targetBucket, sourceExtension, targetArtifactId, version, operation, dryRun);
        totalProcessed += versionStats.processed;
        totalMissing += versionStats.missing;
        totalSkipped += versionStats.skipped;
        totalErrors += versionStats.errors;
        
        if (versionStats.errors === 0) {
          successfulVersions++;
          log(`✅ Version ${version} completed successfully`);
        } else {
          log(`⚠️ Version ${version} completed with ${versionStats.errors} errors`);
        }
      } catch (error) {
        totalErrors++;
        logError(`❌ Version ${version} failed: ${error.message}`);
      }
    }

    // Generate directory listing HTML if any files were processed
    if (!dryRun && totalProcessed > 0) {
      try {
        await generateDirectoryListing(targetS3Client, targetBucket);
        log('✓ Generated HTML directory listing for org/lucee');
      } catch (error) {
        totalErrors++;
        logError(`✗ Failed to generate directory listing: ${error.message}`);
      }
    } else if (dryRun) {
      log('[DRY RUN] Would generate HTML directory listing for org/lucee');
    }

    // Overall summary
    log(`\n${'='.repeat(60)}`);
    log('OVERALL SUMMARY');
    log(`${'='.repeat(60)}`);
    log(`Versions processed: ${successfulVersions}/${versions.length}`);
    log(`Total files processed: ${totalProcessed}`);
    log(`Total missing files (skipped): ${totalMissing}`);
    log(`Total already organized (skipped): ${totalSkipped}`);
    log(`Total errors: ${totalErrors}`);
    
    if (dryRun) {
      log('This was a dry run - no actual changes were made');
    }
    
    if (totalErrors > 0 || successfulVersions < versions.length) {
      logWarning(`Completed with issues. ${successfulVersions}/${versions.length} versions processed successfully.`);
      process.exit(1);
    } else {
      log('🎉 All versions processed successfully!');
    }

  } catch (error) {
    logError(`Script failed: ${error.message}`);
    process.exit(1);
  }
}

async function processExtensionVersion(sourceS3Client, targetS3Client, sourceBucket, targetBucket, sourceExtension, targetArtifactId, version, operation, dryRun) {
  log(`Starting processing for extension: ${sourceExtension}, version: ${version}`);

  // Define the source and target paths for the extension
  const sourceKey = `${sourceExtension}-${version}.lex`;
  const targetKey = `org/lucee/${targetArtifactId}/${version}/${targetArtifactId}-${version}.lex`;

  log(`Processing extension file mapping:`);
  log(`  Source: ${sourceBucket}/${sourceKey}`);
  log(`  Target: ${targetBucket}/${targetKey}`);

  // In dry run mode, also list files in source bucket to help with debugging (only for first version)
  if (dryRun) {
    try {
      log(`Listing files in source S3 bucket ${sourceBucket} (matching pattern ${sourceExtension}*):`);
      const listResponse = await sourceS3Client.send(new ListObjectsV2Command({
        Bucket: sourceBucket,
        Prefix: sourceExtension,
        MaxKeys: 50
      }));
      
      if (listResponse.Contents && listResponse.Contents.length > 0) {
        listResponse.Contents.forEach(obj => {
          log(`  - ${obj.Key} (${obj.Size} bytes, modified: ${obj.LastModified})`);
        });
      } else {
        log(`  No files found matching pattern ${sourceExtension}* in ${sourceBucket}`);
      }
    } catch (error) {
      logWarning(`Could not list source bucket contents: ${error.message}`);
    }
  }

  // Process the extension file
  let processedCount = 0;
  let skippedCount = 0;
  let errorCount = 0;
  let missingCount = 0;

  try {
    const result = await processExtensionFile(sourceS3Client, targetS3Client, sourceBucket, targetBucket, sourceKey, targetKey, operation, dryRun);
    if (result.processed) {
      processedCount++;
      log(`  ✓ ${result.action}: ${sourceBucket}/${sourceKey} -> ${targetBucket}/${targetKey}`);
      
      // Generate and upload maven-metadata.xml files if not dry run
      if (!dryRun) {
        try {
          // Get the timestamp from the extension file for metadata
          let artifactTimestamp;
          
          try {
            const headResponse = await targetS3Client.send(new HeadObjectCommand({
              Bucket: targetBucket,
              Key: targetKey
            }));
            artifactTimestamp = headResponse.LastModified;
            log(`  ✓ Using timestamp from ${targetKey}: ${artifactTimestamp}`);
          } catch (error) {
            artifactTimestamp = new Date();
            log(`  ⚠ Could not get timestamp from ${targetKey}, using current time`);
          }
          
          const versionMetadata = generateExtensionVersionMetadata(targetArtifactId, version, artifactTimestamp);
          await uploadMetadata(targetS3Client, targetBucket, `org/lucee/${targetArtifactId}/${version}/maven-metadata.xml`, versionMetadata);
          log(`  ✓ Generated version-specific maven-metadata.xml for ${targetArtifactId} ${version}`);
          
          // Update parent maven-metadata.xml
          await updateExtensionParentMetadata(targetS3Client, targetBucket, targetArtifactId, version, artifactTimestamp);
          log(`  ✓ Updated parent maven-metadata.xml with version ${version}`);
        } catch (error) {
          errorCount++;
          logError(`  ✗ Failed to generate metadata: ${error.message}`);
        }
      } else {
        log(`  [DRY RUN] Would generate maven-metadata.xml files for ${targetArtifactId} ${version}`);
      }
    } else {
      if (result.reason === 'source file not found') {
        missingCount++;
        log(`  ⚠ Missing: ${sourceKey} (file does not exist in source bucket)`);
      } else {
        skippedCount++;
        logWarning(`  ⚠ Skipped: ${sourceKey} (${result.reason})`);
      }
    }
  } catch (error) {
    errorCount++;
    logError(`  ✗ Failed to process ${sourceKey}: ${error.message}`);
  }

  // Version summary
  log(`--- Extension ${sourceExtension} Version ${version} Summary ---`);
  log(`  Processed: ${processedCount}`);
  log(`  Missing files (skipped): ${missingCount}`);
  log(`  Already organized (skipped): ${skippedCount}`);
  log(`  Errors: ${errorCount}`);

  return {
    processed: processedCount,
    missing: missingCount,
    skipped: skippedCount,
    errors: errorCount
  };
}

async function processExtensionFile(sourceS3Client, targetS3Client, sourceBucket, targetBucket, sourceKey, targetKey, operation, dryRun) {
  // Check if source file exists
  try {
    log(`Checking if source file exists: ${sourceBucket}/${sourceKey}`);
    await sourceS3Client.send(new HeadObjectCommand({
      Bucket: sourceBucket,
      Key: sourceKey
    }));
    log(`✓ Source file exists: ${sourceBucket}/${sourceKey}`);
  } catch (error) {
    if (error.name === 'NotFound' || error.name === 'NoSuchKey' || error.$metadata?.httpStatusCode === 404) {
      log(`Source file not found: ${sourceBucket}/${sourceKey}`);
      return { processed: false, reason: 'source file not found' };
    }
    if (error.name === 'AccessDenied' || error.$metadata?.httpStatusCode === 403) {
      throw new Error(`Access denied to ${sourceBucket}/${sourceKey}. Check S3 permissions.`);
    }
    throw new Error(`Failed to check source file ${sourceBucket}/${sourceKey}: ${error.name} - ${error.message}`);
  }

  // Check if target file already exists
  try {
    log(`Checking if target file already exists: ${targetBucket}/${targetKey}`);
    await targetS3Client.send(new HeadObjectCommand({
      Bucket: targetBucket,
      Key: targetKey
    }));
    log(`Target file already exists: ${targetBucket}/${targetKey}`);
    return { processed: false, reason: 'target file already exists' };
  } catch (error) {
    if (error.name === 'NotFound' || error.name === 'NoSuchKey' || error.$metadata?.httpStatusCode === 404) {
      // Target doesn't exist, proceed
      log(`✓ Target location is available: ${targetBucket}/${targetKey}`);
    } else if (error.name === 'AccessDenied' || error.$metadata?.httpStatusCode === 403) {
      throw new Error(`Access denied to ${targetBucket}/${targetKey}. Check S3 permissions.`);
    } else {
      throw new Error(`Failed to check target file ${targetBucket}/${targetKey}: ${error.name} - ${error.message}`);
    }
  }

  if (dryRun) {
    return { processed: true, action: `[DRY RUN] Would ${operation}` };
  }

  try {
    // Copy file from source bucket to target bucket
    log(`Copying ${sourceBucket}/${sourceKey} to ${targetBucket}/${targetKey}`);
    await targetS3Client.send(new CopyObjectCommand({
      Bucket: targetBucket,
      CopySource: `${sourceBucket}/${sourceKey}`,
      Key: targetKey,
      MetadataDirective: 'COPY'
    }));

    // Delete original file if operation is 'move'
    if (operation === 'move') {
      log(`Deleting original file: ${sourceBucket}/${sourceKey}`);
      await sourceS3Client.send(new DeleteObjectCommand({
        Bucket: sourceBucket,
        Key: sourceKey
      }));
      return { processed: true, action: 'Moved' };
    } else {
      return { processed: true, action: 'Copied' };
    }
  } catch (error) {
    throw new Error(`Failed to ${operation} ${sourceBucket}/${sourceKey}: ${error.name} - ${error.message}`);
  }
}

function generateExtensionVersionMetadata(artifactId, version, artifactTimestamp) {
  // Convert timestamp to the format Maven metadata expects (YYYYMMDDHHMMSS)
  const timestamp = artifactTimestamp.toISOString().replace(/[-:T]/g, '').split('.')[0];
  
  return `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>${artifactId}</artifactId>
  <version>${version}</version>
  <versioning>
    <lastUpdated>${timestamp}</lastUpdated>
    <snapshotVersions>
      <snapshotVersion>
        <extension>lex</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
    </snapshotVersions>
  </versioning>
</metadata>`;
}

async function uploadMetadata(s3Client, bucket, key, content) {
  await s3Client.send(new PutObjectCommand({
    Bucket: bucket,
    Key: key,
    Body: content,
    ContentType: 'application/xml'
  }));
}

async function updateExtensionParentMetadata(s3Client, bucket, artifactId, newVersion, artifactTimestamp) {
  const metadataKey = `org/lucee/${artifactId}/maven-metadata.xml`;
  let existingMetadata = null;
  
  try {
    // Try to get existing parent metadata
    const response = await s3Client.send(new GetObjectCommand({
      Bucket: bucket,
      Key: metadataKey
    }));
    existingMetadata = await response.Body.transformToString();
  } catch (error) {
    if (error.name === 'NoSuchKey' || error.$metadata?.httpStatusCode === 404) {
      log(`Parent maven-metadata.xml not found for ${artifactId}, creating new one`);
    } else {
      throw error;
    }
  }

  // Convert artifact timestamp to Maven format
  const timestamp = artifactTimestamp.toISOString().replace(/[-:T]/g, '').split('.')[0];
  let updatedMetadata;

  if (existingMetadata) {
    // Parse existing metadata and add new version
    updatedMetadata = addVersionToExtensionMetadata(existingMetadata, newVersion, timestamp);
  } else {
    // Create new metadata
    updatedMetadata = `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>${artifactId}</artifactId>
  <versioning>
    <latest>${newVersion}</latest>
    <release>${newVersion.includes('SNAPSHOT') ? '' : newVersion}</release>
    <versions>
      <version>${newVersion}</version>
    </versions>
    <lastUpdated>${timestamp}</lastUpdated>
  </versioning>
</metadata>`;
  }

  await uploadMetadata(s3Client, bucket, metadataKey, updatedMetadata);
}

function addVersionToExtensionMetadata(existingXml, newVersion, timestamp) {
  // Extract existing versions from the XML
  const versionRegex = /<version>([^<]+)<\/version>/g;
  const existingVersions = [];
  let match;
  
  while ((match = versionRegex.exec(existingXml)) !== null) {
    existingVersions.push(match[1]);
  }
  
  // Add new version if it doesn't exist
  if (!existingVersions.includes(newVersion)) {
    existingVersions.push(newVersion);
  }
  
  // Sort versions (simple string sort, which works reasonably well for semantic versions)
  existingVersions.sort();
  
  // Determine latest and release versions
  const latest = newVersion; // Always set the new version as latest
  let release = '';
  
  // Find the latest non-SNAPSHOT version for release
  for (let i = existingVersions.length - 1; i >= 0; i--) {
    if (!existingVersions[i].includes('SNAPSHOT')) {
      release = existingVersions[i];
      break;
    }
  }
  
  // Generate properly formatted XML - extract artifact ID from existing metadata
  const artifactIdMatch = existingXml.match(/<artifactId>([^<]+)<\/artifactId>/);
  const artifactId = artifactIdMatch ? artifactIdMatch[1] : 'unknown-extension';
  
  const versionsXml = existingVersions.map(v => `      <version>${v}</version>`).join('\n');
  
  return `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>${artifactId}</artifactId>
  <versioning>
    <latest>${latest}</latest>
    <release>${release}</release>
    <versions>
${versionsXml}
    </versions>
    <lastUpdated>${timestamp}</lastUpdated>
  </versioning>
</metadata>`;
}

async function generateDirectoryListing(s3Client, bucket) {
  const orgLuceePrefix = 'org/lucee/';
  
  // Get all directories under org/lucee/
  const listResponse = await s3Client.send(new ListObjectsV2Command({
    Bucket: bucket,
    Prefix: orgLuceePrefix,
    Delimiter: '/'
  }));
  
  if (!listResponse.CommonPrefixes || listResponse.CommonPrefixes.length === 0) {
    log('No artifacts found under org/lucee/, skipping directory listing generation');
    return;
  }
  
  // Extract artifact names and their last modified dates
  const artifacts = [];
  
  for (const prefix of listResponse.CommonPrefixes) {
    const artifactName = prefix.Prefix.replace(orgLuceePrefix, '').replace('/', '');
    
    // Get the latest file in this artifact directory to determine last modified date
    const artifactListResponse = await s3Client.send(new ListObjectsV2Command({
      Bucket: bucket,
      Prefix: prefix.Prefix,
      MaxKeys: 1000
    }));
    
    let latestDate = new Date(0); // Start with epoch
    
    if (artifactListResponse.Contents && artifactListResponse.Contents.length > 0) {
      for (const obj of artifactListResponse.Contents) {
        if (obj.LastModified && obj.LastModified > latestDate) {
          latestDate = obj.LastModified;
        }
      }
    }
    
    artifacts.push({
      name: artifactName,
      lastModified: latestDate
    });
  }
  
  // Sort artifacts alphabetically
  artifacts.sort((a, b) => a.name.localeCompare(b.name));
  
  // Generate HTML content
  const html = generateDirectoryListingHtml(artifacts);
  
  // Upload the HTML file
  await s3Client.send(new PutObjectCommand({
    Bucket: bucket,
    Key: 'org/lucee/index.html',
    Body: html,
    ContentType: 'text/html'
  }));
  
  log(`Generated directory listing with ${artifacts.length} artifacts`);
}

function generateDirectoryListingHtml(artifacts) {
  const formatDate = (date) => {
    if (date.getTime() === 0) return '                   -';
    return date.toISOString().slice(0, 16).replace('T', ' ');
  };
  
  const artifactLinks = artifacts.map(artifact => {
    const paddedName = (artifact.name + '/').padEnd(50);
    const formattedDate = formatDate(artifact.lastModified);
    
    return `<a href="${artifact.name}/" title="${artifact.name}/">${paddedName}</a>                     ${formattedDate}         -      `;
  }).join('\n');
  
  return `<!DOCTYPE html>
<html>

<head>
  <title>Central Repository: org/lucee</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
body {
  background: #fff;
}
  </style>
</head>

<body>
  <header>
    <h1>org/lucee</h1>
  </header>
  <hr/>
  <main>
    <pre id="contents">
<a href="../">../</a>
${artifactLinks}
    </pre>
  </main>
  <hr/>
</body>

</html>`;
}

// Run the script
run();