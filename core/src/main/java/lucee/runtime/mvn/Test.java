package lucee.runtime.mvn;

import java.util.Map.Entry;

import lucee.aprint;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.runtime.mvn.MavenUtil.GAVSO;

public final class Test {
	public static void main(String[] args) throws Exception {

		for (Entry<Object, Object> e: System.getProperties().entrySet()) {
			aprint.e(e.getKey() + ":" + e.getValue());
		}

		Resource dir = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Tmp3/www");
		GAVSO[] arr = new GAVSO[] {

				new GAVSO("org.apache.maven", "maven-parent", "40"),

				new GAVSO("org.apache", "apache", "30"),

				new GAVSO("com.puppycrawl.tools", "checkstyle", "7.8"),

				new GAVSO("org.apache.commons", "commons-lang3", "3.12.0"),

				new GAVSO("org.apache.httpcomponents", "httpclient", "4.5.14"),

				new GAVSO("org.apache.httpcomponents", "httpcomponents-client", "4.5.14"),

				new GAVSO("org.apache.commons", "commons-pool2", "2.12.0"),

				new GAVSO("org.apache.commons", "commons-parent", "62"),

				new GAVSO("org.slf4j", "slf4j-api", "1.6.1"),

				new GAVSO("net.bytebuddy", "byte-buddy", "1.14.17"),

				new GAVSO("net.bytebuddy", "byte-buddy-parent", "1.14.17"),

				new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4", null, null, "sha1:d52b9abcd97f38c81342bb7e7ae1eee9b73cba51"),

				new GAVSO("org.apache.maven.resolver", "maven-resolver-impl", "2.0.0"),

				new GAVSO("jakarta.enterprise", "jakarta.enterprise.cdi-api", "4.0.1"),

				new GAVSO("org.lucee", "lucee", "6.1.0.235-RC"),

				new GAVSO("com.github.tjake", "jlama-core", "0.7.0"),

				new GAVSO("org.jboss.ejb3", "jboss-ejb3-api", "3.1.0"),

				new GAVSO("org.eclipse.sisu", "org.eclipse.sisu.plexus", "0.3.4"),

				new GAVSO("org.apache.commons", "commons-jexl3", "3.4.0"),

				new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4")

		};

		arr = new GAVSO[] {
				// groupID:com.github.tjake;artifactId:jlama-core;version:0.7.0

				// new GAVSO("com.github.tjake", "jlama-core", "0.7.0")
				// new GAVSO("org.jboss.ejb3", "jboss-ejb3-api", "3.1.0")
				new GAVSO("org.eclipse.sisu", "org.eclipse.sisu.plexus", "0.3.4")
				// new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4", null, null,
				// "sha1:d52b9abcd97f38c81342bb7e7ae1eee9b73cba51")
				// new GAVSO("org.apache.commons", "commons-jexl3", "3.4.0")

		};
		arr = new GAVSO[] {
				// groupID:com.github.tjake;artifactId:jlama-core;version:0.7.0

				// new GAVSO("com.github.tjake", "jlama-core", "0.7.0")
				// new GAVSO("org.jboss.ejb3", "jboss-ejb3-api", "3.1.0")
				new GAVSO("com.github.tjake", "jlama-core", "0.7.0")
				// new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4", null, null,
				// "sha1:d52b9abcd97f38c81342bb7e7ae1eee9b73cba51")
				// new GAVSO("org.apache.commons", "commons-jexl3", "3.4.0")

		};
		arr = new GAVSO[] {
				// groupID:com.github.tjake;artifactId:jlama-core;version:0.7.0

				// new GAVSO("com.github.tjake", "jlama-core", "0.7.0")
				// new GAVSO("org.jboss.ejb3", "jboss-ejb3-api", "3.1.0")
				new GAVSO("com.google.guava", "guava", "25.0-jre")
				// new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4", null, null,
				// "sha1:d52b9abcd97f38c81342bb7e7ae1eee9b73cba51")
				// new GAVSO("org.apache.commons", "commons-jexl3", "3.4.0")

		};
		// groupID:org.jboss.ejb3;artifactId:jboss-ejb3-api;version:3.1.0
		// groupID:org.eclipse.sisu;artifactId:org.eclipse.sisu.plexus;version:0.3.4
		/*
		 * new Artifact("org.hibernate.orm", "hibernate-core", "6.5.2.Final"), new Artifact("com.amazonaws",
		 * "aws-java-sdk-s3", "1.12.756"),
		 * 
		 * new Artifact("org.apache.maven", "maven-plugin-api", "3.9.8"),
		 * 
		 * new Artifact("org.apache.maven", "maven-core", "3.9.8"), // new Artifact(,,),new Artifact(,,),new
		 * Artifact(,,),new Artifact(,,), xception in thread "main" java.io.IOException: cannot resolve
		 * [${sisuVersion}] for [groupID:org.apache.maven;artifactId:maven-parent;version:40], available
		 * properties are [version.apache-rat-plugin, version.maven-help-plugin,
		 * version.maven-source-plugin, distMgmtReleasesUrl, version.maven-plugin-tools,
		 * distMgmtSnapshotsUrl, version.maven-ear-plugin, version.maven-deploy-plugin, surefire.version,
		 * sourceReleaseAssemblyDescriptor, organization.logo, version.maven-compiler-plugin, twitter,
		 * version.maven-shade-plugin, version.maven-gpg-plugin, project.build.sourceEncoding,
		 * version.maven-enforcer-plugin, version.maven-invoker-plugin, distMgmtSnapshotsName,
		 * assembly.tarLongFileMode, version.maven-scm-publish-plugin,
		 * version.maven-remote-resources-plugin, version.maven-war-plugin, version.apache-resource-bundles,
		 * distMgmtReleasesName, minimalJavaBuildVersion, maven.plugin.tools.version,
		 * version.checksum-maven-plugin, version.maven-fluido-skin, maven.compiler.source,
		 * version.maven-assembly-plugin, version.maven-resources-plugin, minimalMavenBuildVersion,
		 * project.reporting.outputEncoding, version.maven-jar-plugin, version.maven-scm-plugin,
		 * maven.compiler.target, version.maven-dependency-plugin, version.maven-clean-plugin,
		 * version.maven-javadoc-plugin, version.maven-site-plugin, project.build.outputTimestamp,
		 * version.maven-release-plugin, gpg.useagent, version.maven-antrun-plugin, version.maven-surefire,
		 * version.maven-install-plugin, version.maven-project-info-reports-plugin]
		 * 
		 * };
		 */
		/*
		 * for (Artifact a: examples) { print.e("------------ " + a); print.e(maven.download(a.groupId,
		 * a.artifactId, a.version, true, false)); }
		 */
		long start = System.currentTimeMillis();
		// ResourceUtil.deleteContent(dir, null);
		for (GAVSO gav: arr) {
			POM pom = POM.getInstance(dir, null, gav.g, gav.a, gav.v, null, null, gav.c, POM.SCOPE_NOT_TEST, POM.SCOPE_ALL, null);

			aprint.e("==========================================");
			aprint.e(pom.getName());
			aprint.e(pom.getChecksum());
			aprint.e(pom);
			aprint.e("==========================================");

			// print.e("--- properties ---");
			// print.e(pom.getAllParentsAsTree());
			// print.e(pom.getProperties());
			aprint.e("--- packaging ---");
			aprint.e(pom.getPackaging());

			aprint.e("--- path ---");
			aprint.e(pom.getPath());
			aprint.e("--- hash ---");
			aprint.e(pom.hash());

			aprint.e("--- artifact ---");
			aprint.e(pom.getArtifact());

			aprint.e("--- parents ---");
			// print.e(pom.getAllParentsAsTree());
			aprint.e(pom.getAllParents());

			aprint.e("--- repositories ---");
			// print.e(pom.getAllParentsAsTree());
			aprint.e(pom.getRepositories());

			aprint.e("--- dependencies management ---");
			aprint.e(pom.getDependencyManagement());

			aprint.e("--- all dependencies management ---");
			aprint.e(pom.getAllDependencyManagement());

			aprint.e("--- dependencies ---");
			// print.e(getDependenciesAsTrees(pom, true));
			aprint.e(pom.getAllDependencies(true));

			aprint.e("--- jars ---");
			// print.e(getDependenciesAsTrees(pom, true));
			aprint.e(pom.getJars());
			aprint.e(System.currentTimeMillis() - start);

			// pom.getScope();
			// print.e(pom.getDependencyManagement());

			// print.e(maven.getDependencies(groupId, artifactId, version, true, false, true));

			break;
		}
	}

}
