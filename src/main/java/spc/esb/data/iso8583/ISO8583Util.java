package spc.esb.data.iso8583;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.esb.data.CompositeNode;
import spc.esb.data.FixedMessage;
import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;
import spc.webos.util.StringX;
import spc.webos.util.tree.TreeNode;

public class ISO8583Util
{
	protected static Logger log = LoggerFactory.getLogger(ISO8583Util.class);
	public static int PROC_CODE_OFFSET = 22; // field3: ������
	public static int PROCCODE_LEN = 6; // field3: �����볤��

	// ��ñ�׼iso8583�Ľ�����, field 3: ����Ϊ6
	public static byte[] getProcCode(byte[] iso8583, int offset)
	{
		return getValue(iso8583, offset, PROC_CODE_OFFSET, PROCCODE_LEN);
	}

	public static byte[] getValue(byte[] iso8583, int offset, int offLen, int len)
	{
		BitMap bitmap = new BitMap(iso8583, offset);
		int start = bitmap.isValid(0) ? 16 : 8; // ��64λͼ����128λͼ
		start += offLen; // ��׼�ڶ�����Ϊ22�ֽڳ������ʺ�PRIMARY ACCOUNT NUMBER
		return FixedMessage.read(iso8583, offset, start, len); // �ʺ�Ϊ6���ֽ�
	}

	/**
	 * �������Ʊ��İ�תΪ�м��ISO8583Message���İ�
	 * 
	 * @param buf
	 * @param offset
	 * @param len
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	public static ISO8583Message deserialize2iso8583(IISO8583MessageConverter iso8583MsgConverter,
			byte[] iso8583, int offset, int len, IMessage msg, TreeNode schema) throws Exception
	{
		ISO8583Message iso8583msg = new ISO8583Message();
		List child = schema.getChildren();
		for (int i = 0; i < child.size(); i++)
		{ // ��ͨ��schema��Ϣ����8583λͼ����ģ��
			TreeNode tnode = (TreeNode) child.get(i);
			MsgSchemaPO schemaVO = (MsgSchemaPO) tnode.getTreeNodeValue();
			if (StringX.nullity(schemaVO.getIso8583()))
			{ // 2012-07-31 ���8583����Ϊ��������
				if (log.isDebugEnabled()) log.debug(schemaVO.getEsbName() + " iso8583 is null!!!");
				continue;
			}
			Field f = new Field(tnode, schemaVO);
			iso8583msg.setField(f);
		}
		iso8583MsgConverter.deserialize(iso8583msg, iso8583, offset);
		if (log.isDebugEnabled()) log.debug("deserialize2iso8583: " + iso8583msg);
		return iso8583msg;
	}

	public static ICompositeNode deserialize(ISO8583Message iso8583msg, TreeNode schema)
			throws Exception
	{
		return deserialize(DefaultField2NodeConverter.getInstance(), iso8583msg, schema);
	}

	public static ICompositeNode deserialize(IField2NodeConverter field2nodeConverter,
			ISO8583Message iso8583msg, TreeNode schema) throws Exception
	{
		CompositeNode cnode = new CompositeNode();
		List child = schema.getChildren();
		for (int i = 0; i < child.size(); i++)
		{
			TreeNode tnode = (TreeNode) child.get(i);
			MsgSchemaPO schemaVO = (MsgSchemaPO) tnode.getTreeNodeValue();
			if (StringX.nullity(schemaVO.getIso8583()))
			{ // 2012-07-31 ���8583����Ϊ��������
				if (log.isDebugEnabled()) log.debug("esbname(" + schemaVO.getEsbName()
						+ ")'s iso8583 is null!!!");
				continue;
			}
			int[] iso8583 = StringX.split2ints(schemaVO.getIso8583(), "|");
			// modified by chenjs 2011-11-10
			int no = iso8583[0]; // Integer.parseInt(schemaVO.getRcvName());
			Field f = iso8583msg.getField(no);
			if (f != null && f.enabled) cnode.set(schemaVO.getEsbName(),
					field2nodeConverter.field2node(f)); // f.value);
		}
		return cnode;
	}

	/**
	 * ��һ��8583���ĸ�����Ԥ�ڵı��Ľṹת��Ϊһ��cnode�ڵ�
	 * 
	 * @param iso8583
	 * @param offset
	 * @param len
	 * @param reqmsg
	 * @param schema
	 * @return
	 * @throws Exception
	 */
	public static ICompositeNode deserialize(IISO8583MessageConverter iso8583MsgConverter,
			byte[] iso8583, int offset, int len, IMessage msg, TreeNode schema) throws Exception
	{
		return deserialize(
				deserialize2iso8583(iso8583MsgConverter, iso8583, offset, len, msg, schema), schema);
	}

	public static ICompositeNode deserialize(IField2NodeConverter field2nodeConverter,
			IISO8583MessageConverter iso8583MsgConverter, byte[] iso8583, int offset, int len,
			IMessage msg, TreeNode schema) throws Exception
	{
		return deserialize(field2nodeConverter,
				deserialize2iso8583(iso8583MsgConverter, iso8583, offset, len, msg, schema), schema);
	}

	public static ICompositeNode deserialize(IISO8583MessageConverter iso8583MsgConverter,
			byte[] iso8583, TreeNode schema) throws Exception
	{
		return deserialize(iso8583MsgConverter, iso8583, 0, iso8583.length, null, schema);
	}

	public static ISO8583Message serialize2iso8583(ICompositeNode cnode, TreeNode schema)
			throws Exception
	{
		return serialize2iso8583(DefaultField2NodeConverter.getInstance(), cnode, schema);
	}

	/**
	 * ��һ�����ӽڵ�����䱨�Ľṹת��Ϊһ��8583�ṹ
	 * 
	 * @param cnode
	 * @param schema
	 * @return
	 * @throws Exception
	 */
	public static ISO8583Message serialize2iso8583(IField2NodeConverter field2nodeConverter,
			ICompositeNode cnode, TreeNode schema) throws Exception
	{
		if (field2nodeConverter == null) field2nodeConverter = DefaultField2NodeConverter
				.getInstance();
		ISO8583Message iso8583msg = new ISO8583Message();
		List child = schema.getChildren();
		for (int i = 0; i < child.size(); i++)
		{
			TreeNode tnode = (TreeNode) child.get(i);
			MsgSchemaPO schemaVO = (MsgSchemaPO) tnode.getTreeNodeValue();
			INode node = cnode.getNode(schemaVO.getEsbName());
			if (node == null || (node instanceof IAtomNode && StringX.nullity(node.toString())))
			{
				if (log.isDebugEnabled()) log.debug(schemaVO.getEsbName() + " is null...");
				continue;
			}
			if (StringX.nullity(schemaVO.getIso8583()))
			{ // 2012-07-31 ���8583����Ϊ��������
				if (log.isDebugEnabled()) log.debug(schemaVO.getEsbName() + " iso8583 is null!!!");
				continue;
			}
			// Field f = new Field(schemaVO);
			// f.setValue(anode.stringValue());
			Field f = field2nodeConverter.node2field(new Field(tnode, schemaVO), node);
			iso8583msg.setField(f);
			if (log.isDebugEnabled()) log.debug(schemaVO.getEsbName() + " F:" + f.no + ","
					+ new String(f.value));
		}
		if (log.isDebugEnabled()) log.debug("serialize2iso8583:" + iso8583msg);
		return iso8583msg;
	}

	public static byte[] serialize(IField2NodeConverter field2nodeConverter,
			IISO8583MessageConverter iso8583MsgConverter, ICompositeNode cnode, TreeNode schema)
			throws Exception
	{
		ISO8583Message iso8583msg = serialize2iso8583(field2nodeConverter, cnode, schema);
		return iso8583MsgConverter.serialize(iso8583msg);
	}

	public static byte[] serialize(IISO8583MessageConverter iso8583MsgConverter,
			ICompositeNode cnode, TreeNode schema) throws Exception
	{
		return serialize(null, iso8583MsgConverter, cnode, schema);
	}
}
