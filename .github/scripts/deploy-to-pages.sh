#!/bin/bash
set -e

VERSION="$1"
if [ -z "$VERSION" ]; then
    echo "Usage: $0 <version>"
    exit 1
fi

echo "Deploying Lucee $VERSION artifacts to GitHub Pages..."

# Setup git
git config user.name "GitHub Actions"
git config user.email "actions@github.com"

# Create temporary directory for gh-pages
PAGES_DIR="gh-pages-temp"
rm -rf "$PAGES_DIR"

# Clone gh-pages branch or create it
if git ls-remote --heads origin | grep -q "refs/heads/gh-pages"; then
    echo "Cloning existing gh-pages branch..."
    git clone --single-branch --branch gh-pages https://x-access-token:${GITHUB_TOKEN}@github.com/${GITHUB_REPOSITORY}.git "$PAGES_DIR"
else
    echo "Creating new gh-pages branch..."
    mkdir "$PAGES_DIR"
    cd "$PAGES_DIR"
    git init
    git remote add origin https://x-access-token:${GITHUB_TOKEN}@github.com/${GITHUB_REPOSITORY}.git
    git checkout -b gh-pages
    cd ..
fi

cd "$PAGES_DIR"

# Create Maven repository structure
MAVEN_PATH="org/lucee/lucee/$VERSION"
mkdir -p "$MAVEN_PATH"

# Copy all artifacts from Maven target directory
echo "Copying artifacts..."
if [ -d "../loader/target" ]; then
    # Copy main JAR
    cp ../loader/target/lucee-*.jar "$MAVEN_PATH/" 2>/dev/null || echo "No JAR files found"
    
    # Copy LCO files  
    cp ../loader/target/*.lco "$MAVEN_PATH/" 2>/dev/null || echo "No LCO files found"
    
    # Copy POM
    cp ../loader/target/*.pom "$MAVEN_PATH/" 2>/dev/null || echo "No POM files found"
    
    # Copy signatures
    cp ../loader/target/*.asc "$MAVEN_PATH/" 2>/dev/null || echo "No signature files found"
    
    # Copy sources and javadoc if they exist
    cp ../loader/target/*-sources.jar "$MAVEN_PATH/" 2>/dev/null || echo "No sources JAR found"
    cp ../loader/target/*-javadoc.jar "$MAVEN_PATH/" 2>/dev/null || echo "No javadoc JAR found"
else
    echo "Warning: ../loader/target directory not found"
fi

# List what we copied
echo "Artifacts copied to $MAVEN_PATH:"
ls -la "$MAVEN_PATH/"

# Update main maven-metadata.xml
METADATA_FILE="org/lucee/lucee/maven-metadata.xml"
echo "Generating $METADATA_FILE..."

# Create maven-metadata.xml
cat > "$METADATA_FILE" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <versioning>
    <latest>$VERSION</latest>
    <versions>
EOF

# Add all existing versions by scanning directories
find org/lucee/lucee -maxdepth 1 -type d -name "*.*.*" | sort -V | while read dir; do
    version=$(basename "$dir")
    echo "      <version>$version</version>" >> "$METADATA_FILE"
done

cat >> "$METADATA_FILE" << EOF
    </versions>
    <lastUpdated>$(date +%Y%m%d%H%M%S)</lastUpdated>
  </versioning>
</metadata>
EOF

# Create version-specific metadata for snapshots
if [[ "$VERSION" == *-SNAPSHOT ]]; then
    VERSION_METADATA="$MAVEN_PATH/maven-metadata.xml"
    echo "Generating snapshot metadata: $VERSION_METADATA..."
    
    # For snapshots, create version-specific metadata
    cat > "$VERSION_METADATA" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<metadata modelVersion="1.1.0">
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <version>$VERSION</version>
  <versioning>
    <snapshot>
      <timestamp>$(date +%Y%m%d.%H%M%S)</timestamp>
      <buildNumber>1</buildNumber>
    </snapshot>
    <lastUpdated>$(date +%Y%m%d%H%M%S)</lastUpdated>
    <snapshotVersions>
EOF

    # Add snapshot versions for each file found
    for file in "$MAVEN_PATH"/*; do
        if [ -f "$file" ]; then
            filename=$(basename "$file")
            extension="${filename##*.}"
            
            # Skip metadata.xml itself
            if [[ "$filename" == "maven-metadata.xml" ]]; then
                continue
            fi
            
            # Extract classifier if present
            classifier=""
            if [[ "$filename" =~ lucee-.*-(sources|javadoc|zero|light)\.jar$ ]]; then
                classifier=$(echo "$filename" | sed -n 's/.*-\([^-]*\)\.jar$/\1/p')
            fi
            
            cat >> "$VERSION_METADATA" << EOF
      <snapshotVersion>
EOF
            if [ -n "$classifier" ]; then
                echo "        <classifier>$classifier</classifier>" >> "$VERSION_METADATA"
            fi
            cat >> "$VERSION_METADATA" << EOF
        <extension>$extension</extension>
        <value>$VERSION</value>
        <updated>$(date +%Y%m%d%H%M%S)</updated>
      </snapshotVersion>
EOF
        fi
    done
    
    cat >> "$VERSION_METADATA" << EOF
    </snapshotVersions>
  </versioning>
</metadata>
EOF
fi

# Create/update index.html for browsing
cat > index.html << EOF
<!DOCTYPE html>
<html>
<head>
    <title>Lucee Maven Repository</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .code { background: #f5f5f5; padding: 15px; border-radius: 5px; }
        .latest { color: #28a745; font-weight: bold; }
    </style>
</head>
<body>
    <h1>🚀 Lucee Public Maven Repository</h1>
    <p>This repository contains all Lucee releases and snapshots as a backup to Maven Central/Sonatype.</p>
    
    <h2>How to Use</h2>
    <p>Add this repository to your Maven <code>pom.xml</code>:</p>
    <div class="code">
&lt;repositories&gt;
  &lt;repository&gt;
    &lt;id&gt;lucee-public&lt;/id&gt;
    &lt;url&gt;https://${GITHUB_REPOSITORY_OWNER}.github.io/${GITHUB_REPOSITORY##*/}/&lt;/url&gt;
    &lt;releases&gt;&lt;enabled&gt;true&lt;/enabled&gt;&lt;/releases&gt;
    &lt;snapshots&gt;&lt;enabled&gt;true&lt;/enabled&gt;&lt;/snapshots&gt;
  &lt;/repository&gt;
&lt;/repositories&gt;
    </div>
    
    <h2>Latest Version</h2>
    <p class="latest">$VERSION</p>
    
    <h2>Browse Artifacts</h2>
    <ul>
        <li><a href="org/lucee/lucee/">Browse all Lucee versions</a></li>
        <li><a href="org/lucee/lucee/maven-metadata.xml">View metadata</a></li>
    </ul>
    
    <hr>
    <p><small>Last updated: $(date) | Generated automatically by GitHub Actions</small></p>
</body>
</html>
EOF

# Commit and push
echo "Committing changes..."
git add .

if git diff --staged --quiet; then
    echo "No changes to commit"
else
    git commit -m "Deploy Lucee $VERSION artifacts

- Version: $VERSION
- Build: ${GITHUB_RUN_NUMBER:-unknown}
- Commit: ${GITHUB_SHA:-unknown}
- Generated: $(date)"
    
    echo "Pushing to gh-pages branch..."
    git push origin gh-pages
    
    echo "✅ Successfully deployed to GitHub Pages!"
    echo "🌐 Repository URL: https://${GITHUB_REPOSITORY_OWNER}.github.io/${GITHUB_REPOSITORY##*/}/"
    echo "📦 Artifacts: https://${GITHUB_REPOSITORY_OWNER}.github.io/${GITHUB_REPOSITORY##*/}/org/lucee/lucee/$VERSION/"
fi

cd ..
rm -rf "$PAGES_DIR"
echo "Deployment complete!"