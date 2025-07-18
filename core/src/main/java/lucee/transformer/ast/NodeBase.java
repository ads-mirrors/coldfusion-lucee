package lucee.transformer.ast;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Factory;
import lucee.transformer.Node;
import lucee.transformer.Position;

public abstract class NodeBase implements Node {

	private Position start;
	private Position end;
	private AstFactory factory;

	public NodeBase(AstFactory factory) {
		this.factory = factory;
	}

	@Override
	public final Position getStart() {
		return start;
	}

	@Override
	public final void setStart(Position start) {
		this.start = start;
	}

	@Override
	public final Position getEnd() {
		return end;
	}

	@Override
	public final void setEnd(Position end) {
		this.end = end;
	}

	@Override
	public final Factory getFactory() {
		return factory;
	}

	public final AstFactory getAstFactory() {
		return factory;
	}

	public void writeOut(Struct sct) {
		// start
		Struct sctStart = new StructImpl(Struct.TYPE_LINKED);
		sctStart.setEL(KeyConstants._line, start.line);
		sctStart.setEL(KeyConstants._column, start.column);
		sctStart.setEL(KeyConstants._offset, start.pos);
		sct.setEL(KeyConstants._start, sctStart);

		// end
		Struct sctEnd = new StructImpl(Struct.TYPE_LINKED);
		sctStart.setEL(KeyConstants._line, end.line);
		sctStart.setEL(KeyConstants._column, end.column);
		sctStart.setEL(KeyConstants._offset, end.pos);
		sct.setEL(KeyConstants._end, sctEnd);
	}

}
