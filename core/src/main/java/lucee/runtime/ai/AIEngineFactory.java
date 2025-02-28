package lucee.runtime.ai;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleException;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class AIEngineFactory {

	private final ClassDefinition<? extends AIEngine> cd;
	private final Struct properties;
	private final String _default;
	private final String name;
	private String id;

	private static final Map<String, SoftReference<AIEngine>> instances = new ConcurrentHashMap<>();

	public AIEngineFactory(ClassDefinition<? extends AIEngine> cd, Struct properties, String name, String _default) {
		this.cd = cd;
		this.properties = properties == null ? new StructImpl() : properties;
		this.name = name.trim();
		this._default = StringUtil.isEmpty(_default, true) ? null : _default.trim();
	}

	public String getDefault() {
		return _default;
	}

	public String getName() {
		return name;
	}

	public ClassDefinition<? extends AIEngine> getClassDefinition() {
		return cd;
	}

	public Struct getProperties() {
		return properties;
	}

	public String getId() {
		if (id == null) id = createId();
		return id;
	}

	private String createId() {
		Key[] keys = properties.keys();
		Arrays.sort(keys, new Comparator<Key>() {
			@Override
			public int compare(Key k1, Key k2) {
				return k1.getUpperString().compareTo(k2.getUpperString());
			}
		});

		StringBuilder sb = new StringBuilder()

				.append(name).append(';')

				.append(cd.toString()).append(';');

		for (Key k: keys) {
			sb.append(k).append(':').append(properties.get(k, "")).append(';');
		}
		return HashUtil.create64BitHashAsString(sb.toString());
	}

	public static AIEngineFactory load(Config config, ClassDefinition<? extends AIEngine> cd, Struct custom, String name, String _default, boolean validate)
			throws ClassException, BundleException {
		// validate class
		if (validate) cd.getClazz();
		return new AIEngineFactory(cd, custom, name, _default);
	}

	public static AIEngineFactory load(Config config, String name, Struct data) throws PageException, ClassException, BundleException {

		ClassDefinition<AIEngine> cd;

		cd = ConfigFactoryImpl.getClassDefinition(data, "", config.getIdentification());
		if (cd.hasClass()) {

			Struct custom = Caster.toStruct(data.get(KeyConstants._custom, null), null);
			if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._properties, null), null);
			if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._arguments, null), null);
			String _default = Caster.toString(data.get(KeyConstants._default, null), null);
			return load(config, cd, custom, name, _default, false);
		}
		throw new ApplicationException("class defintion is invalid");
	}

	public static AIEngine getInstance(Config config, AIEngineFactory factory) throws PageException, ClassException, BundleException {
		AIEngine aie = getExistingInstance(factory.getId(), null);
		if (aie != null) return aie;

		aie = (AIEngine) ClassUtil.loadInstance(factory.cd.getClazz());
		LogUtil.logx(config, Log.LEVEL_TRACE, "ai-factory", "create AI instance [" + factory.cd.toString() + "]", "ai", "application");
		aie.init(factory, factory.properties);
		instances.put(factory.getId(), new SoftReference<AIEngine>(aie));
		return aie;
	}

	public static AIEngine getExistingInstance(String id, AIEngine defaultValue) {
		AIEngine aie;
		SoftReference<AIEngine> ref = instances.get(id);
		if (ref != null) {
			aie = ref.get();
			if (aie != null) return aie;
		}

		return defaultValue;
	}
}
