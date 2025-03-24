package lucee.runtime.functions.mvn;

import java.io.IOException;
import java.util.List;

import lucee.print;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.mvn.MavenUtil;
import lucee.runtime.mvn.POM;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class MavenInfo implements Function {

	private static final long serialVersionUID = -3515451010781896377L;

	public static Query call(PageContext pc, String groupId, String artifactId) throws PageException {
		return call(pc, groupId, artifactId, null, null, true);
	}

	public static Query call(PageContext pc, String groupId, String artifactId, String version) throws PageException {
		return call(pc, groupId, artifactId, version, null, true);
	}

	public static Query call(PageContext pc, String groupId, String artifactId, String version, Array arrScopes) throws PageException {
		return call(pc, groupId, artifactId, version, arrScopes, true);
	}

	public static Query call(PageContext pc, String groupId, String artifactId, String version, Array arrScopes, boolean includeOptional) throws PageException {

		// validate groupId
		if (!StringUtil.isEmpty(groupId, true)) groupId = groupId.trim();
		else throw new FunctionException(pc, "MavenInfo", 1, "groupId", "argument is empty");

		// validate artifactId
		if (!StringUtil.isEmpty(artifactId, true)) artifactId = artifactId.trim();
		else throw new FunctionException(pc, "MavenInfo", 2, "artifactId", "argument is empty");

		// validate version
		if (StringUtil.isEmpty(version, true)) version = null;
		else version = version.trim();

		// validate scope
		int scopes;
		if (arrScopes != null && arrScopes.size() > 0) {
			try {
				scopes = MavenUtil.toScopes(ListUtil.toStringArray(arrScopes));
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}
		else scopes = POM.SCOPE_NOT_TEST;

		// validate returntype

		/*
		 * boolean asQuery = true; if (StringUtil.isEmpty(returntype, true)) asQuery = true; else if
		 * ("tree".equalsIgnoreCase(returntype.trim())) asQuery = false; else if
		 * ("query".equalsIgnoreCase(returntype.trim())) asQuery = true; else throw new
		 * FunctionException(pc, "MavenInfo", 5, "returntype", "valid values are [query,tree]");
		 */

		Log log = LogUtil.getLog(pc.getConfig(), "mvn", "application");
		POM root = POM.getInstance(((ConfigPro) pc.getConfig()).getMavenDir(), groupId, artifactId, version, scopes, log);

		try {
			// as Query
			// if (asQuery) {

			List<POM> deps = root.getAllDependencies(includeOptional);
			Query qry = new QueryImpl(new Key[] { KeyConstants._groupId, KeyConstants._artifactId, KeyConstants._version, KeyConstants._scope, KeyConstants._optional,
					KeyConstants._checksum, KeyConstants._url, KeyConstants._path }, 0, "dependencies");
			addRow(qry, root, false);
			for (POM pom: deps) {
				addRow(qry, pom, true); // depencies are "compile" by default
			}
			return qry;

			// }
			// FUTURE add tree
			// as tree
			/*
			 * List<TreeNode<POM>> tree = root.getAllDependenciesAsTrees(); for (TreeNode<POM> node: tree) {
			 * node. }
			 */

		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	private static void addRow(Query qry, POM pom, boolean dependency) throws PageException, IOException {

		if (!pom.getPackaging().equals("jar")) return;
		int row = qry.addRow();

		// scope
		String scope = pom.getScopeAsString();
		if (StringUtil.isEmpty(scope)) {
			scope = dependency ? "compile" : "";
		}

		// optional
		String strOptional = pom.getOptionalAsString();
		Boolean optional;
		if (StringUtil.isEmpty(strOptional)) {
			optional = dependency ? Boolean.FALSE : null;
		}
		else {
			optional = Caster.toBoolean(strOptional);
		}

		// local resource
		Resource jar = pom.getArtifact("jar");

		// checksum
		String checksum = pom.getChecksum();
		if (StringUtil.isEmpty(checksum)) {
			checksum = jar.isFile() ? MavenUtil.createChecksum(jar, "md5") : "";
		}

		qry.setAt(KeyConstants._groupId, row, pom.getGroupId());
		qry.setAt(KeyConstants._artifactId, row, pom.getArtifactId());
		qry.setAt(KeyConstants._version, row, pom.getVersion());
		qry.setAt(KeyConstants._scope, row, scope);
		qry.setAt(KeyConstants._optional, row, optional);
		qry.setAt(KeyConstants._checksum, row, checksum);
		qry.setAt(KeyConstants._url, row, pom.getArtifactAsURL("jar").toExternalForm());
		qry.setAt(KeyConstants._path, row, jar.toString());

		print.e(pom.getPackaging());

	}
}
