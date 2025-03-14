/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
package lucee.runtime.script;

import java.lang.reflect.Constructor;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class CFMLScriptEngineFactory implements ScriptEngineFactory {

	private ScriptEngineFactory instance;

	public CFMLScriptEngineFactory() {
		try {
			// Load the class via reflection
			Class<?> factoryClass = Class.forName("lucee.runtime.script.CFMLScriptEngineFactoryOld");

			// Get the no-arg constructor
			Constructor<?> constructor = factoryClass.getDeclaredConstructor();

			// Ensure it's accessible (in case it's not public)
			// constructor.setAccessible(true);

			// Create a new instance
			instance = (ScriptEngineFactory) constructor.newInstance();

			System.out.println("Successfully loaded CFMLScriptEngineFactoryOld via reflection");
		}
		catch (Throwable e) {
			System.err.println("ERROR in CFMLScriptEngineFactory constructor: " + e.getClass().getName());
			System.err.println("Message: " + e.getMessage());
			e.printStackTrace();

			// Log the complete stack trace of the cause if there is one
			Throwable cause = e.getCause();
			if (cause != null) {
				System.err.println("Caused by:");
				cause.printStackTrace();
			}
			throw new RuntimeException(e);
		}

	}

	@Override
	public String getEngineName() {
		return instance.getEngineName();
	}

	@Override
	public String getEngineVersion() {
		return instance.getEngineVersion();
	}

	@Override
	public List<String> getExtensions() {
		return instance.getExtensions();
	}

	@Override
	public List<String> getMimeTypes() {
		return instance.getMimeTypes();
	}

	@Override
	public List<String> getNames() {
		return instance.getNames();
	}

	@Override
	public String getLanguageName() {
		return instance.getLanguageName();
	}

	@Override
	public String getLanguageVersion() {
		return instance.getLanguageVersion();
	}

	@Override
	public Object getParameter(String key) {
		return instance.getParameter(key);
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		return instance.getMethodCallSyntax(obj, m, args);
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return instance.getOutputStatement(toDisplay);
	}

	@Override
	public String getProgram(String... statements) {
		return instance.getProgram(statements);
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return instance.getScriptEngine();
	}
}