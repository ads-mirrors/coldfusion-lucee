# Lucee Test Suite

This directory contains the CFML test suites for Lucee Server.

Tests are written using [TestBox](https://testbox.ortusbooks.com/)

Lucee tests must extend `"org.lucee.cfml.test.LuceeTestCase"`

## Running Tests

Lucee tests are run using Ant and Maven. You can execute all tests or filter to specific suites.

### Basic Usage

From the `/loader` directory:

```bash
ant test
```

Or, to run a specific test suite:

```bash
mvn test -DtestFilter="{testFilename}"
```

### Options running the test suite

You can use the following Ant options to speed up builds and tests (see [Lucee Docs](https://docs.lucee.org/guides/working-with-source/build-from-source.html#build-performance-tips)):

- `-DskipTests=true` — Skip running tests during build
- `-Dmaven.javadoc.skip=true` — Skip Javadoc generation
- `-Dmaven.source.skip=true` — Skip source JAR generation
- `-DskipNpm=true` — Skip npm install and build steps
- `-DskipAntTests=true` — Skip Ant-based tests
- `-DskipIntegrationTests=true` — Skip integration tests
- `-DskipDeploy=true` — Skip deployment steps
- `-DtestHideJavaStack=true` Hides Java stack traces in the test run output.
- `-DtestLabels="image"` (string, default: empty): Comma-separated list of labels to filter which tests are run

For more details, see the [Lucee build guide](https://docs.lucee.org/guides/working-with-source/build-from-source.html).
