package lucee.runtime.listener;

import lucee.runtime.type.Struct;

public interface ISerializationSettings {

	public static int SERIALIZE_AS_UNDEFINED = 0;
	public static int SERIALIZE_AS_ROW = 1;
	public static int SERIALIZE_AS_COLUMN = 2;
	public static int SERIALIZE_AS_STRUCT = 4;

	public boolean getPreserveCaseForStructKey();

	public boolean getPreserveCaseForQueryColumn();

	public int getSerializeQueryAs();

	public Struct toStruct();
}
