package lucee.transformer.bytecode.util;

import java.io.IOException;

import lucee.commons.digest.HashUtil;

public abstract class SimpleMethodSupport implements SimpleMethod {

	@Override
	public final String hash() {
		StringBuilder sb = new StringBuilder().append(getName()).append(';');

		try {
			// Parameters
			for (Class p: getParameterClasses()) {
				sb.append(p.getName()).append(';');
			}
			// return
			sb.append(getReturnClass().getName());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		// TODO Auto-generated method stub
		return HashUtil.create64BitHashAsString(sb.toString(), Character.MAX_RADIX);
	}
}