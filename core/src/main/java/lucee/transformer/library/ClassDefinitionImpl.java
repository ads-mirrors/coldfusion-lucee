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
package lucee.transformer.library;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Map;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.commons.digest.HashUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.Identification;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.mvn.MavenUtil;
import lucee.runtime.mvn.MavenUtil.GAVSO;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.wrap.MapAsStruct;

public class ClassDefinitionImpl<T> implements ClassDefinition<T>, Externalizable {

	/**
	 * do not use to load class!!!
	 */
	private String className;
	private String name;
	private Version version;

	private Identification id;
	private boolean versionOnlyMattersWhenDownloading = false;
	private String maven;

	private transient Class<T> clazz;
	private transient GAVSO[] gavsos;

	public ClassDefinitionImpl(String className, String name, String version, Identification id) {
		this.className = className == null ? null : className.trim();
		this.name = StringUtil.isEmpty(name, true) ? null : name.trim();
		this.version = OSGiUtil.toVersion(version, null);
		this.id = id;
	}

	public ClassDefinitionImpl(String className, String maven) {
		this.className = className == null ? null : className.trim();
		this.maven = maven;
	}

	public ClassDefinitionImpl(String className) {
		this.className = className == null ? null : className.trim();
		this.name = null;
		this.version = null;
		this.id = null;
	}

	public ClassDefinitionImpl(Class<T> clazz) {
		this.className = clazz.getName();
		this.clazz = clazz;
		ClassLoader cl = clazz.getClassLoader();
		if (cl instanceof BundleClassLoader) {
			BundleClassLoader bcl = (BundleClassLoader) cl;
			Bundle b = bcl.getBundle();
			this.name = b.getSymbolicName();
			this.version = b.getVersion();
		}
		else {
			this.name = null;
			this.version = null;
		}

		this.id = null;
	}

	public static ClassDefinitionImpl toClassDefinitionImpl(Struct sct, String prefix, boolean strict, Identification id) {
		prefix = improvePrefix(prefix);

		String cl = toClassName(sct, prefix);

		// OSGi?
		String bn = toBundleName(sct, prefix, strict);
		String bv = toBundleVersion(sct, prefix, strict);
		if (!StringUtil.isEmpty(bn)) {
			return new ClassDefinitionImpl(cl, bn, bv, id);
		}

		// Maven?
		String maven = toMaven(sct, prefix);
		if (!StringUtil.isEmpty(maven, true)) {
			return new ClassDefinitionImpl(cl, maven);
		}

		return new ClassDefinitionImpl(cl, null, null, id);
	}

	public static ClassDefinition toClassDefinition(Map<String, ?> map, boolean strict, Identification id) {
		return toClassDefinitionImpl(MapAsStruct.toStruct(map, false), null, strict, id);
	}

	private static String improvePrefix(String prefix) {
		if (prefix != null) {
			prefix = prefix.trim();
			if (StringUtil.isEmpty(prefix)) prefix = null;
			else if (prefix.endsWith("-")) prefix = prefix.substring(0, prefix.length() - 1);
		}

		return prefix;
	}

	public static String toClassName(Struct sct, String prefix) {
		if (sct == null) return null;
		prefix = improvePrefix(prefix);

		String className = Caster.toString(sct.get(prefix != null ? KeyImpl.init(prefix + "class") : KeyConstants._class, null), null);
		if (StringUtil.isEmpty(className)) className = Caster.toString(sct.get(prefix != null ? KeyImpl.init(prefix + "classname") : KeyConstants._classname, null), null);
		if (StringUtil.isEmpty(className)) className = Caster.toString(sct.get(prefix != null ? KeyImpl.init(prefix + "-class-name") : KeyImpl.init("class-name"), null), null);
		if (StringUtil.isEmpty(className)) return null;
		return className;
	}

	public static String toMaven(Struct sct, String prefix) {
		if (sct == null) return null;
		prefix = improvePrefix(prefix);

		String maven = Caster.toString(sct.get(prefix != null ? KeyImpl.init(prefix + "maven") : KeyConstants._maven, null), null);
		if (StringUtil.isEmpty(maven)) return null;
		return maven;
	}

	public static String toBundleName(Struct sct, String prefix, boolean strict) {
		if (sct == null) return null;
		prefix = improvePrefix(prefix);

		String name = Caster.toString(sct.get(prefix != null ? KeyImpl.init(prefix + "bundleName") : KeyConstants._bundleName, null), null);
		if (StringUtil.isEmpty(name)) name = Caster.toString(sct.get(prefix != null ? KeyImpl.init(prefix + "-bundle-name") : KeyImpl.init("bundle-name"), null), null);
		if (!strict && StringUtil.isEmpty(name)) name = Caster.toString(sct.get(prefix != null ? KeyImpl.init(prefix + "name") : KeyConstants._name, null), null);
		if (StringUtil.isEmpty(name)) return null;
		return name;
	}

	public static String toBundleVersion(Struct sct, String prefix, boolean strict) {
		if (sct == null) return null;

		prefix = improvePrefix(prefix);

		String version = Caster.toString(sct.get(prefix != null ? KeyImpl.init(prefix + "bundleVersion") : KeyConstants._bundleVersion, null), null);
		if (StringUtil.isEmpty(version)) version = Caster.toString(sct.get(prefix != null ? KeyImpl.init(prefix + "-bundle-version") : KeyImpl.init("bundle-version"), null), null);
		if (!strict && StringUtil.isEmpty(version)) version = Caster.toString(sct.get(prefix != null ? KeyImpl.init(prefix + "version") : KeyConstants._version, null), null);
		if (StringUtil.isEmpty(version)) return null;
		return version;
	}

	/**
	 * only used by deserializer!
	 */
	public ClassDefinitionImpl() {
	}

	public ClassDefinitionImpl<T> setVersionOnlyMattersWhenDownloading(boolean versionOnlyMattersWhenDownloading) {
		this.versionOnlyMattersWhenDownloading = versionOnlyMattersWhenDownloading;
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(className);
		out.writeObject(name);
		out.writeObject(version == null ? null : version.toString());
		out.writeObject(id);
		out.writeObject(maven);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		className = (String) in.readObject();
		name = (String) in.readObject();
		String tmp = (String) in.readObject();
		if (tmp != null) this.version = OSGiUtil.toVersion(tmp, null);
		id = (Identification) in.readObject();
		maven = (String) in.readObject();
	}

	@Override
	public Class<T> getClazz() throws ClassException, BundleException {
		return getClazz(false);
	}

	public Class<T> getClazz(boolean forceLoadingClass) throws ClassException, BundleException {
		if (!forceLoadingClass && clazz != null) return clazz;

		// OSGi
		if (name != null) {
			return clazz = ClassUtil.loadClassByBundle(className, name, version, id, JavaSettingsImpl.getBundleDirectories(null), versionOnlyMattersWhenDownloading);
		}

		// Maven
		if (maven != null) {
			ConfigPro config = (ConfigPro) ThreadLocalPageContext.getConfig();
			try {
				return clazz = (Class<T>) config.getRPCClassLoader(false, JavaSettingsImpl.getInstance(config, getMaven()), null).loadClass(className);
			}
			catch (Exception e) {
				ClassException ce = new ClassException("Failes to load class [" + className + "]");
				ExceptionUtil.initCauseEL(ce, e);
				throw ce;
			}
		}

		// regular class definition
		try {
			return clazz = ClassUtil.loadClass(ThreadLocalPageContext.getRPCClassLoader(forceLoadingClass), className);
		}
		catch (IOException e) {
			ClassException ce = new ClassException(e.getMessage());
			ExceptionUtil.initCauseEL(ce, e);
			throw ce;
		}

	}

	@Override
	public Class<T> getClazz(Class<T> defaultValue) {
		try {
			return getClazz(false);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	@Override
	public boolean hasClass() {
		return !StringUtil.isEmpty(className, true);
	}

	@Override
	public boolean isBundle() {
		return !StringUtil.isEmpty(name, true);
	}

	public boolean isMaven() {
		return maven != null;
	}

	public GAVSO[] getMaven() throws ApplicationException {
		if (gavsos == null) {
			List<GAVSO> gavsos = MavenUtil.toGAVSOs(maven);
			if (gavsos != null && gavsos.size() > 0) this.gavsos = gavsos.toArray(new GAVSO[gavsos.size()]);
		}
		return gavsos;
	}

	@Override
	public boolean hasVersion() {
		return version != null;
	}

	@Override
	public boolean isClassNameEqualTo(String otherClassName) {
		return isClassNameEqualTo(otherClassName, false);
	}

	@Override
	public boolean isClassNameEqualTo(String otherClassName, boolean ignoreCase) {
		if (otherClassName == null) return false;
		otherClassName = otherClassName.trim();
		return ignoreCase ? otherClassName.equalsIgnoreCase(className) : otherClassName.equals(className);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClassDefinition)) return false;
		return toString().equals(obj.toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isBundle()) {
			sb.append("class:").append(className).append(";name:").append(name).append(";version:").append(version).append(";");
		}
		else if (isMaven()) {
			String maven = this.maven;
			try {
				maven = MavenUtil.toString(getMaven());
			}
			catch (Exception e) {
			}
			sb.append("class:").append(className).append(";maven:").append(maven).append(";");
		}
		else {
			return className;
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public static ClassDefinition toClassDefinition(String className, Identification id, Map<String, String> attributes) {
		if (StringUtil.isEmpty(className, true)) return null;

		String bn = null, bv = null;
		if (attributes != null) {
			// name
			bn = attributes.get("name");
			if (StringUtil.isEmpty(bn)) bn = attributes.get("bundle-name");

			// version
			bv = attributes.get("version");
			if (StringUtil.isEmpty(bv)) bv = attributes.get("bundle-version");
		}
		return new ClassDefinitionImpl(className, bn, bv, id);
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Version getVersion() {
		return version;
	}

	@Override
	public String getVersionAsString() {
		return version == null ? null : version.toString();
	}

	@Override
	public String getId() {
		return HashUtil.create64BitHashAsString(toString());
	}
}