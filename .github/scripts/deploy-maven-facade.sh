#!/bin/bash
set -e

VERSION="$1"
if [ -z "$VERSION" ] || [ "$VERSION" = "1" ]; then
    VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout -f loader/pom.xml)
fi

echo "🎯 Creating Maven repository facade for Lucee $VERSION using existing CDN..."

# Base URLs
GITHUB_REPO="${GITHUB_REPOSITORY}"
CDN_BASE="https://cdn.lucee.org"
ARTIFACT_DIR="loader/target"

# Setup temp directory for GitHub Pages
TEMP_DIR="gh-pages-temp"
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"
cd "$TEMP_DIR"

# Initialize git
git init
git config user.name "GitHub Actions"
git config user.email "actions@github.com"
git checkout -b gh-pages

# Create Maven repository structure
MAVEN_PATH="org/lucee/lucee"
mkdir -p "$MAVEN_PATH"

# Function to create artifact redirect page
create_redirect() {
    local file_name="$1"
    local redirect_url="$2"
    
    cat > "$file_name" << EOF
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Redirecting to Lucee CDN</title>
    <meta http-equiv="refresh" content="0; url=$redirect_url">
    <script>window.location.href="$redirect_url";</script>
</head>
<body>
    <p>Redirecting to <a href="$redirect_url">$redirect_url</a></p>
</body>
</html>
EOF
}

# Create version-specific directory
VERSION_DIR="$MAVEN_PATH/$VERSION"
mkdir -p "$VERSION_DIR"

# Create redirect pages for each artifact type
echo "Creating Maven repository structure with CDN redirects..."

# Main artifacts - point to your existing CDN URLs
create_redirect "$VERSION_DIR/lucee-${VERSION}.jar" "$CDN_BASE/lucee-${VERSION}.jar"
create_redirect "$VERSION_DIR/lucee-light-${VERSION}.jar" "$CDN_BASE/lucee-light-${VERSION}.jar" 
create_redirect "$VERSION_DIR/lucee-zero-${VERSION}.jar" "$CDN_BASE/lucee-zero-${VERSION}.jar"
create_redirect "$VERSION_DIR/${VERSION}.lco" "$CDN_BASE/${VERSION}.lco"

# Check if additional artifacts exist and create redirects
if [ -f "../$ARTIFACT_DIR/lucee-${VERSION}-sources.jar" ]; then
    create_redirect "$VERSION_DIR/lucee-${VERSION}-sources.jar" "$CDN_BASE/lucee-${VERSION}-sources.jar"
fi

if [ -f "../$ARTIFACT_DIR/lucee-${VERSION}-javadoc.jar" ]; then
    create_redirect "$VERSION_DIR/lucee-${VERSION}-javadoc.jar" "$CDN_BASE/lucee-${VERSION}-javadoc.jar"
fi

# Copy POM file directly (small file, can be on GitHub Pages)
if [ -f "../$ARTIFACT_DIR/lucee-${VERSION}.pom" ]; then
    cp "../$ARTIFACT_DIR/lucee-${VERSION}.pom" "$VERSION_DIR/"
    echo "Copied POM file to GitHub Pages"
fi

# Generate maven-metadata.xml for the version
cat > "$VERSION_DIR/maven-metadata.xml" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <version>$VERSION</version>
  <versioning>
    <snapshot>
      <timestamp>$(date +%Y%m%d.%H%M%S)</timestamp>
      <buildNumber>${GITHUB_RUN_NUMBER:-1}</buildNumber>
    </snapshot>
    <lastUpdated>$(date +%Y%m%d%H%M%S)</lastUpdated>
  </versioning>
</metadata>
EOF

# Generate main maven-metadata.xml (aggregate all versions)
if [ -f "$MAVEN_PATH/maven-metadata.xml" ]; then
    # Parse existing metadata to get version list
    echo "Updating existing maven-metadata.xml..."
    # For now, just overwrite - in production you'd want to merge versions
fi

cat > "$MAVEN_PATH/maven-metadata.xml" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <versioning>
    <latest>$VERSION</latest>
    <release>$VERSION</release>
    <versions>
      <version>$VERSION</version>
    </versions>
    <lastUpdated>$(date +%Y%m%d%H%M%S)</lastUpdated>
  </versioning>
</metadata>
EOF

# Create a nice index.html for the main repository
cat > "index.html" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>Lucee Maven Repository</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        h1 { color: #2c3e50; }
        .code { background: #f4f4f4; padding: 15px; border-radius: 5px; font-family: monospace; }
        .info { background: #e8f4f8; padding: 15px; border-radius: 5px; margin: 20px 0; }
    </style>
</head>
<body>
    <h1>🚀 Lucee Public Maven Repository</h1>
    
    <div class="info">
        <p><strong>Repository URL:</strong> <code>https://lucee.github.io/Lucee/</code></p>
        <p><strong>Latest Version:</strong> $VERSION</p>
        <p><strong>Powered by:</strong> Lucee CDN + GitHub Pages</p>
    </div>

    <h2>📦 Maven Configuration</h2>
    <div class="code">
&lt;repositories&gt;
    &lt;repository&gt;
        &lt;id&gt;lucee-public&lt;/id&gt;
        &lt;url&gt;https://lucee.github.io/Lucee/&lt;/url&gt;
        &lt;releases&gt;&lt;enabled&gt;true&lt;/enabled&gt;&lt;/releases&gt;
        &lt;snapshots&gt;&lt;enabled&gt;true&lt;/enabled&gt;&lt;/snapshots&gt;
    &lt;/repository&gt;
&lt;/repositories&gt;

&lt;dependencies&gt;
    &lt;dependency&gt;
        &lt;groupId&gt;org.lucee&lt;/groupId&gt;
        &lt;artifactId&gt;lucee&lt;/artifactId&gt;
        &lt;version&gt;$VERSION&lt;/version&gt;
    &lt;/dependency&gt;
&lt;/dependencies&gt;
    </div>

    <h2>🔗 Browse Artifacts</h2>
    <ul>
        <li><a href="org/lucee/lucee/">Browse all versions</a></li>
        <li><a href="org/lucee/lucee/$VERSION/">Latest version ($VERSION)</a></li>
        <li><a href="$CDN_BASE/">Direct CDN access</a></li>
    </ul>

    <p><em>Artifacts are served from Lucee's high-performance CDN at <a href="$CDN_BASE">cdn.lucee.org</a></em></p>
</body>
</html>
EOF

# Commit and push
git add .
git commit -m "Deploy Lucee $VERSION Maven repository facade

- Maven metadata for $VERSION
- Redirects to cdn.lucee.org for fast downloads  
- Updated repository index

Build: ${GITHUB_RUN_NUMBER:-unknown}
Commit: ${GITHUB_SHA:0:7}"

echo "Pushing to gh-pages branch..."
git push -f https://${GITHUB_ACTOR}:${GITHUB_TOKEN}@github.com/${GITHUB_REPOSITORY}.git gh-pages

cd ..
rm -rf "$TEMP_DIR"

echo "✅ Maven repository facade deployed successfully!"
echo "🌐 Repository URL: https://lucee.github.io/Lucee/"
echo "📦 Latest version: $VERSION"
echo "🚀 Downloads served from: $CDN_BASE"