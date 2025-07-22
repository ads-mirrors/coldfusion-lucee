import { S3Client, ListObjectsV2Command, CopyObjectCommand, DeleteObjectCommand, PutObjectCommand, GetObjectCommand, HeadObjectCommand } from '@aws-sdk/client-s3';

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
    // Get inputs from environment variables
    const version = process.env.INPUT_VERSION;
    const operation = process.env.INPUT_OPERATION || 'move';
    const dryRun = process.env.INPUT_DRY_RUN === 'true';
    const accessKeyId = process.env.INPUT_S3_ACCESS_KEY;
    const secretAccessKey = process.env.INPUT_S3_SECRET_KEY;
    const bucket = process.env.INPUT_S3_BUCKET || 'lucee-downloads';
    const region = process.env.INPUT_S3_REGION || 'us-east-1';

    if (!version) {
      throw new Error('Version is required');
    }
    
    if (!accessKeyId || !secretAccessKey) {
      throw new Error('S3 credentials are required');
    }

    log(`Starting ${dryRun ? 'DRY RUN of ' : ''}${operation} operation for version ${version}`);
    log(`S3 Bucket: ${bucket}, Region: ${region}`);
    
    // Test S3 connectivity
    try {
      log('Testing S3 connectivity...');
      await s3Client.send(new ListObjectsV2Command({
        Bucket: bucket,
        MaxKeys: 1
      }));
      log('✓ S3 connectivity successful');
    } catch (error) {
      throw new Error(`S3 connectivity test failed: ${error.name} - ${error.message}. Check bucket name and credentials.`);
    }

    // Initialize S3 client
    const s3Client = new S3Client({
      region,
      credentials: {
        accessKeyId,
        secretAccessKey
      }
    });

    // Define file mappings from source to target
    const fileMappings = [
      {
        source: `lucee-${version}.jar`,
        target: `org/lucee/lucee/${version}/lucee-${version}.jar`
      },
      {
        source: `${version}.lco`,
        target: `org/lucee/lucee/${version}/lucee-${version}.lco`
      },
      {
        source: `lucee-light-${version}.jar`,
        target: `org/lucee/lucee/${version}/lucee-${version}-light.jar`
      },
      {
        source: `lucee-zero-${version}.jar`,
        target: `org/lucee/lucee/${version}/lucee-${version}-zero.jar`
      },
      {
        source: `lucee-${version}.war`,
        target: `org/lucee/lucee/${version}/lucee-${version}.war`
      },
      {
        source: `lucee-express-${version}.zip`,
        target: `org/lucee/lucee/${version}/lucee-${version}-express.zip`
      },
      {
        source: `forgebox-${version}.zip`,
        target: `org/lucee/lucee/${version}/lucee-${version}-forgebox.zip`
      },
      {
        source: `forgebox-light-${version}.zip`,
        target: `org/lucee/lucee/${version}/lucee-${version}-forgebox-light.zip`
      }
    ];

    log(`Found ${fileMappings.length} file mappings to process`);

    // In dry run mode, also list files in bucket to help with debugging
    if (dryRun) {
      try {
        log('Listing files in S3 bucket (first 50 files):');
        const listResponse = await s3Client.send(new ListObjectsV2Command({
          Bucket: bucket,
          MaxKeys: 50
        }));
        
        if (listResponse.Contents && listResponse.Contents.length > 0) {
          listResponse.Contents.forEach(obj => {
            log(`  - ${obj.Key} (${obj.Size} bytes, modified: ${obj.LastModified})`);
          });
        } else {
          log('  No files found in bucket');
        }
      } catch (error) {
        logWarning(`Could not list bucket contents: ${error.message}`);
      }
    }

    // Process each file mapping
    let processedCount = 0;
    let skippedCount = 0;
    let errorCount = 0;

    for (const mapping of fileMappings) {
      try {
        const result = await processFile(s3Client, bucket, mapping.source, mapping.target, operation, dryRun);
        if (result.processed) {
          processedCount++;
          log(`✓ ${result.action}: ${mapping.source} -> ${mapping.target}`);
        } else {
          skippedCount++;
          logWarning(`⚠ Skipped: ${mapping.source} (${result.reason})`);
        }
      } catch (error) {
        errorCount++;
        logError(`✗ Failed to process ${mapping.source}: ${error.message}`);
      }
    }

    // Generate and upload version-specific maven-metadata.xml
    if (!dryRun && processedCount > 0) {
      try {
        const versionMetadata = generateVersionMetadata(version);
        await uploadMetadata(s3Client, bucket, `org/lucee/lucee/${version}/maven-metadata.xml`, versionMetadata);
        log(`✓ Generated version-specific maven-metadata.xml for ${version}`);
      } catch (error) {
        errorCount++;
        logError(`✗ Failed to generate version metadata: ${error.message}`);
      }

      // Update parent maven-metadata.xml
      try {
        await updateParentMetadata(s3Client, bucket, version);
        log(`✓ Updated parent maven-metadata.xml with version ${version}`);
      } catch (error) {
        errorCount++;
        logError(`✗ Failed to update parent metadata: ${error.message}`);
      }
    } else if (dryRun) {
      log(`[DRY RUN] Would generate maven-metadata.xml files for version ${version}`);
    }

    // Summary
    log('=== SUMMARY ===');
    log(`Processed: ${processedCount}`);
    log(`Skipped: ${skippedCount}`);
    log(`Errors: ${errorCount}`);
    
    if (dryRun) {
      log('This was a dry run - no actual changes were made');
    }
    
    if (errorCount > 0) {
      logWarning(`Completed with ${errorCount} errors`);
      process.exit(1);
    } else {
      log('Artifact organization completed successfully');
    }

  } catch (error) {
    logError(`Script failed: ${error.message}`);
    process.exit(1);
  }
}

async function processFile(s3Client, bucket, sourceKey, targetKey, operation, dryRun) {
  // Check if source file exists
  try {
    log(`Checking if source file exists: ${sourceKey}`);
    await s3Client.send(new HeadObjectCommand({
      Bucket: bucket,
      Key: sourceKey
    }));
    log(`✓ Source file exists: ${sourceKey}`);
  } catch (error) {
    log(`Source file check failed for ${sourceKey}: ${error.name} - ${error.message}`);
    if (error.name === 'NotFound' || error.name === 'NoSuchKey' || error.$metadata?.httpStatusCode === 404) {
      return { processed: false, reason: 'source file not found' };
    }
    if (error.name === 'AccessDenied' || error.$metadata?.httpStatusCode === 403) {
      throw new Error(`Access denied to ${sourceKey}. Check S3 permissions.`);
    }
    throw new Error(`Failed to check source file ${sourceKey}: ${error.name} - ${error.message}`);
  }

  // Check if target file already exists
  try {
    log(`Checking if target file already exists: ${targetKey}`);
    await s3Client.send(new HeadObjectCommand({
      Bucket: bucket,
      Key: targetKey
    }));
    log(`Target file already exists: ${targetKey}`);
    return { processed: false, reason: 'target file already exists' };
  } catch (error) {
    if (error.name === 'NotFound' || error.name === 'NoSuchKey' || error.$metadata?.httpStatusCode === 404) {
      // Target doesn't exist, proceed
      log(`✓ Target location is available: ${targetKey}`);
    } else if (error.name === 'AccessDenied' || error.$metadata?.httpStatusCode === 403) {
      throw new Error(`Access denied to ${targetKey}. Check S3 permissions.`);
    } else {
      throw new Error(`Failed to check target file ${targetKey}: ${error.name} - ${error.message}`);
    }
  }

  if (dryRun) {
    return { processed: true, action: `[DRY RUN] Would ${operation}` };
  }

  try {
    // Copy file to new location
    log(`Copying ${sourceKey} to ${targetKey}`);
    await s3Client.send(new CopyObjectCommand({
      Bucket: bucket,
      CopySource: `${bucket}/${sourceKey}`,
      Key: targetKey,
      MetadataDirective: 'COPY'
    }));

    // Delete original file if operation is 'move'
    if (operation === 'move') {
      log(`Deleting original file: ${sourceKey}`);
      await s3Client.send(new DeleteObjectCommand({
        Bucket: bucket,
        Key: sourceKey
      }));
      return { processed: true, action: 'Moved' };
    } else {
      return { processed: true, action: 'Copied' };
    }
  } catch (error) {
    throw new Error(`Failed to ${operation} ${sourceKey}: ${error.name} - ${error.message}`);
  }
}

function generateVersionMetadata(version) {
  const timestamp = new Date().toISOString().replace(/[-:T]/g, '').split('.')[0];
  
  return `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <version>${version}</version>
  <versioning>
    <lastUpdated>${timestamp}</lastUpdated>
    <snapshotVersions>
      <snapshotVersion>
        <extension>jar</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>light</classifier>
        <extension>jar</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>zero</classifier>
        <extension>jar</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <extension>lco</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <extension>war</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>express</classifier>
        <extension>zip</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>forgebox</classifier>
        <extension>zip</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>forgebox-light</classifier>
        <extension>zip</extension>
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

async function updateParentMetadata(s3Client, bucket, newVersion) {
  const metadataKey = 'org/lucee/lucee/maven-metadata.xml';
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
      log('Parent maven-metadata.xml not found, creating new one');
    } else {
      throw error;
    }
  }

  const timestamp = new Date().toISOString().replace(/[-:T]/g, '').split('.')[0];
  let updatedMetadata;

  if (existingMetadata) {
    // Parse existing metadata and add new version
    updatedMetadata = addVersionToMetadata(existingMetadata, newVersion, timestamp);
  } else {
    // Create new metadata
    updatedMetadata = `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
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

function addVersionToMetadata(existingXml, newVersion, timestamp) {
  // Simple XML manipulation - handles basic cases
  let updated = existingXml;
  
  // Update lastUpdated
  updated = updated.replace(/<lastUpdated>\d*<\/lastUpdated>/, `<lastUpdated>${timestamp}</lastUpdated>`);
  
  // Update latest
  updated = updated.replace(/<latest>.*?<\/latest>/, `<latest>${newVersion}</latest>`);
  
  // Update release if it's not a SNAPSHOT
  if (!newVersion.includes('SNAPSHOT')) {
    updated = updated.replace(/<release>.*?<\/release>/, `<release>${newVersion}</release>`);
  }
  
  // Add version if it doesn't exist
  if (!updated.includes(`<version>${newVersion}</version>`)) {
    updated = updated.replace('</versions>', `      <version>${newVersion}</version>\n    </versions>`);
  }
  
  return updated;
}

// Run the script
run();