package lucee.runtime.functions.security;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecretProvider;
import lucee.runtime.security.SecretProviderFactory;

public final class GetSecret extends BIF {

	private static final long serialVersionUID = -2880816702728323010L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 3) throw new FunctionException(pc, "GetSecret", 1, 2, args.length);

		String key = Caster.toString(args[0]);
		String name = args.length > 1 ? Caster.toString(args[1]) : null;

		// check all of them
		if (name == null) {
			for (SecretProvider sp: ((ConfigPro) pc.getConfig()).getSecretProviders().values()) {
				if (sp.getSecret(key, null) != null) return new SecretProviderFactory.Ref(sp, key);
			}
			throw new ApplicationException("no secret provider found that provides the key [" + key + "]");
		}

		SecretProvider sp = ((ConfigPro) pc.getConfig()).getSecretProvider(name);
		return new SecretProviderFactory.Ref(sp, key).touch();
	}

}