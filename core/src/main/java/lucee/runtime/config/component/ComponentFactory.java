/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.config.component;

import lucee.commons.io.res.Resource;
import lucee.runtime.config.ConfigFactory;
import lucee.runtime.config.Constants;

public final class ComponentFactory {

	/**
	 * this method deploy all components for org.lucee.cfml
	 * 
	 * @param dir components directory
	 * @param doNew redeploy even the file exist, this is set to true when a new version is started
	 */
	public static void deploy(Resource dir, boolean doNew) {
		String path = "/resource/component/" + (Constants.DEFAULT_PACKAGE.replace('.', '/')) + "/";
		deploy(dir, path, doNew, "HelperBase", "Feed", "Ftp", "Http", "Mail", "Query", "Result", "Administrator", "Component");

		// orm
		{
			Resource ormDir = dir.getRealResource("orm");
			String ormPath = path + "orm/";
			deploy(ormDir, ormPath, doNew, "IEventHandler", "INamingStrategy");
		}
		// test
		{
			Resource testDir = dir.getRealResource("test");
			String testPath = path + "test/";
			deploy(testDir, testPath, doNew, "LuceeTestSuite", "LuceeTestSuiteRunner", "LuceeTestCase", "LuceeTestCaseParallel");
		}
	}

	private static void deploy(Resource dir, String path, boolean doNew, String... names) {
		if (!dir.exists()) {
			dir.mkdirs();
			doNew = true; // in case the dir does not exist, the file also don't, so no check needed
		}

		Resource f;
		for (String name: names) {
			f = dir.getRealResource(name + ".cfc");
			if (doNew || !f.exists()) {
				ConfigFactory.createFileFromResourceEL(path + name + ".cfc", f);
			}
		}

	}

	private static void delete(Resource dir, String name) {
		Resource f = dir.getRealResource(name + ".cfc");
		if (f.exists()) f.delete();
	}
}