#!/bin/bash
set -e

echo "🔄 Migrating existing Lucee snapshots from OSSRH to GitHub Pages..."

# Old OSSRH URLs
OLD_OSSRH_SNAPSHOTS="https://oss.sonatype.org/content/repositories/snapshots"
OLD_S01_SNAPSHOTS="https://s01.oss.sonatype.org/content/repositories/snapshots"

GROUP_PATH="org/lucee/lucee"
TEMP_DIR="temp-migration"

# Setup git
git config --global user.name "GitHub Actions"
git config --global user.email "actions@github.com"

# Create temp directory for migration
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"
cd "$TEMP_DIR"

# Function to download metadata and extract versions
download_versions_from_ossrh() {
    local base_url="$1"
    local repo_name="$2"
    
    echo "📡 Checking $repo_name for existing versions..."
    
    local metadata_url="$base_url/$GROUP_PATH/maven-metadata.xml"
    echo "Trying: $metadata_url"
    
    if curl -f -s "$metadata_url" -o "metadata-$repo_name.xml"; then
        echo "✅ Found metadata from $repo_name"
        
        # Extract all snapshot versions
        grep -o '<version>[^<]*SNAPSHOT[^<]*</version>' "metadata-$repo_name.xml" | \
        sed 's/<version>\(.*\)<\/version>/\1/' | \
        sort -V > "versions-$repo_name.txt"
        
        echo "Found $(wc -l < "versions-$repo_name.txt") snapshot versions in $repo_name"
        cat "versions-$repo_name.txt"
    else
        echo "❌ No metadata found in $repo_name"
        touch "versions-$repo_name.txt"
    fi
}

# Function to download all artifacts for a version
download_version_artifacts() {
    local base_url="$1"
    local version="$2"
    local repo_name="$3"
    
    echo "📦 Downloading $version from $repo_name..."
    
    local version_dir="artifacts/$version"
    mkdir -p "$version_dir"
    
    # Download version-specific metadata to get actual timestamped files
    local version_metadata_url="$base_url/$GROUP_PATH/$version/maven-metadata.xml"
    
    if curl -f -s "$version_metadata_url" -o "$version_dir/maven-metadata.xml"; then
        echo "  ✅ Downloaded version metadata"
        
        # Try to download common artifact patterns
        local artifacts=(
            "lucee-$version.jar"
            "lucee-$version.pom"
            "lucee-$version-sources.jar"
            "lucee-$version-javadoc.jar"
            "lucee-zero-$version.jar"
            "lucee-light-$version.jar"
            "$version.lco"
        )
        
        # Also try with signatures
        local sig_artifacts=(
            "lucee-$version.jar.asc"
            "lucee-$version.pom.asc"
            "lucee-$version-sources.jar.asc"
            "lucee-$version-javadoc.jar.asc"
            "lucee-zero-$version.jar.asc"
            "lucee-light-$version.jar.asc"
            "$version.lco.asc"
        )
        
        # Download main artifacts
        for artifact in "${artifacts[@]}"; do
            local artifact_url="$base_url/$GROUP_PATH/$version/$artifact"
            if curl -f -s "$artifact_url" -o "$version_dir/$artifact"; then
                echo "    ✅ Downloaded $artifact"
            else
                echo "    ❌ Not found: $artifact"
            fi
        done
        
        # Download signatures
        for artifact in "${sig_artifacts[@]}"; do
            local artifact_url="$base_url/$GROUP_PATH/$version/$artifact"
            if curl -f -s "$artifact_url" -o "$version_dir/$artifact"; then
                echo "    ✅ Downloaded signature: $artifact"
            else
                echo "    ⚠️  No signature: $artifact"
            fi
        done
        
        # Count successful downloads
        local downloaded_count=$(find "$version_dir" -name "*.jar" -o -name "*.pom" -o -name "*.lco" | wc -l)
        echo "  📊 Downloaded $downloaded_count artifacts for $version"
        
        if [ "$downloaded_count" -eq 0 ]; then
            echo "  ❌ No artifacts downloaded for $version, removing directory"
            rm -rf "$version_dir"
            return 1
        fi
    else
        echo "  ❌ Could not download version metadata for $version"
        rm -rf "$version_dir"
        return 1
    fi
    
    return 0
}

# Download versions from both possible OSSRH locations
download_versions_from_ossrh "$OLD_OSSRH_SNAPSHOTS" "oss"
download_versions_from_ossrh "$OLD_S01_SNAPSHOTS" "s01"

# Combine and deduplicate versions
cat versions-*.txt | sort -V | uniq > all-versions.txt

echo ""
echo "📋 Total unique snapshot versions found: $(wc -l < all-versions.txt)"
echo ""

if [ ! -s all-versions.txt ]; then
    echo "❌ No snapshot versions found to migrate"
    exit 0
fi

# Download artifacts for each version
mkdir -p artifacts
successful_downloads=0

while IFS= read -r version; do
    echo ""
    echo "🔄 Processing version: $version"
    
    # Try oss.sonatype.org first, then s01 if that fails
    if download_version_artifacts "$OLD_OSSRH_SNAPSHOTS" "$version" "oss"; then
        ((successful_downloads++))
    elif download_version_artifacts "$OLD_S01_SNAPSHOTS" "$version" "s01"; then
        ((successful_downloads++))
    else
        echo "  ❌ Failed to download $version from both repositories"
    fi
    
done < all-versions.txt

echo ""
echo "📊 Migration Summary:"
echo "  Total versions found: $(wc -l < all-versions.txt)"
echo "  Successfully downloaded: $successful_downloads"
echo ""

if [ "$successful_downloads" -eq 0 ]; then
    echo "❌ No versions were successfully downloaded"
    exit 1
fi

# Now upload to GitHub Pages
echo "🚀 Uploading migrated versions to GitHub Pages..."

# Clone gh-pages branch
PAGES_DIR="../gh-pages-migration"
rm -rf "$PAGES_DIR"

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
    cd ../temp-migration
fi

cd "$PAGES_DIR"

# Copy all downloaded artifacts
for version_dir in ../temp-migration/artifacts/*/; do
    if [ -d "$version_dir" ]; then
        version=$(basename "$version_dir")
        target_dir="org/lucee/lucee/$version"
        
        echo "📋 Adding $version to repository..."
        mkdir -p "$target_dir"
        cp -r "$version_dir"/* "$target_dir/"
    fi
done

# Regenerate maven-metadata.xml with all versions
echo "📝 Regenerating maven-metadata.xml with all versions..."

METADATA_FILE="org/lucee/lucee/maven-metadata.xml"
mkdir -p "org/lucee/lucee"

cat > "$METADATA_FILE" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <versioning>
    <versions>
EOF

# Add all versions (both migrated and existing)
find org/lucee/lucee -maxdepth 1 -type d -name "*.*.*" | \
  sed 's|org/lucee/lucee/||' | \
  sort -V | \
  while read version; do
    echo "      <version>$version</version>" >> "$METADATA_FILE"
done

# Set latest version
LATEST_VERSION=$(find org/lucee/lucee -maxdepth 1 -type d -name "*.*.*" | \
  sed 's|org/lucee/lucee/||' | \
  sort -V | tail -1)

cat >> "$METADATA_FILE" << EOF
    </versions>
    <latest>$LATEST_VERSION</latest>
    <lastUpdated>$(date +%Y%m%d%H%M%S)</lastUpdated>
  </versioning>
</metadata>
EOF

# Update index.html
cat > index.html << EOF
<!DOCTYPE html>
<html>
<head>
    <title>Lucee Maven Repository</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .code { background: #f5f5f5; padding: 15px; border-radius: 5px; }
        .latest { color: #28a745; font-weight: bold; }
        .migrated { color: #007bff; font-style: italic; }
    </style>
</head>
<body>
    <h1>🚀 Lucee Public Maven Repository</h1>
    <p>This repository contains all Lucee releases and snapshots, including migrated versions from OSSRH.</p>
    
    <h2>How to Use</h2>
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
    <p class="latest">$LATEST_VERSION</p>
    
    <h2>Repository Contents</h2>
    <ul>
        <li><a href="org/lucee/lucee/">Browse all Lucee versions</a></li>
        <li><a href="org/lucee/lucee/maven-metadata.xml">View metadata</a></li>
        <li class="migrated">Includes migrated snapshots from OSSRH ($(wc -l < ../temp-migration/all-versions.txt) versions)</li>
    </ul>
    
    <hr>
    <p><small>Last updated: $(date) | Migration completed automatically</small></p>
</body>
</html>
EOF

# Commit everything
echo "💾 Committing migrated versions..."
git add .

if git diff --staged --quiet; then
    echo "No new changes to commit"
else
    git commit -m "Migrate existing OSSRH snapshots to GitHub Pages

- Migrated $successful_downloads snapshot versions from OSSRH
- Updated maven-metadata.xml with all versions  
- Preserved access to historical Lucee snapshots
- Migration completed: $(date)"
    
    echo "📤 Pushing to gh-pages branch..."
    git push origin gh-pages
    
    echo "✅ Migration completed successfully!"
    echo "🌐 All versions now available at: https://${GITHUB_REPOSITORY_OWNER}.github.io/${GITHUB_REPOSITORY##*/}/"
fi

# Cleanup
cd ../..
rm -rf "$TEMP_DIR" "$PAGES_DIR"

echo ""
echo "🎉 Migration Summary:"
echo "  📥 Downloaded: $successful_downloads versions from OSSRH"
echo "  📤 Uploaded: All versions to GitHub Pages"
echo "  🌐 Repository: https://${GITHUB_REPOSITORY_OWNER}.github.io/${GITHUB_REPOSITORY##*/}/"
echo ""
echo "ℹ️  Your users can now access all historical snapshots via GitHub Pages!"