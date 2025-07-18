package lucee.transformer.cfml.evaluator.impl;

import lucee.commons.lang.ExceptionUtil;
import lucee.transformer.TransformerException;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.statement.tag.Tag;

public final class TagTimeout extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag tagLibTag, FunctionLib flibs) throws EvaluatorException {
		lucee.transformer.bytecode.statement.tag.TagTimeout tt = (lucee.transformer.bytecode.statement.tag.TagTimeout) tag;
		try {
			tt.init();
		}
		catch (TransformerException te) {
			EvaluatorException ee = new EvaluatorException(te.getMessage());
			ExceptionUtil.initCauseEL(ee, te);
			throw ee;
		}
	}
}