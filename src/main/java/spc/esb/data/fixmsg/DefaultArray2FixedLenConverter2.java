package spc.esb.data.fixmsg;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.esb.data.IArrayNode;
import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;
import spc.webos.util.FTLUtil;
import spc.webos.util.StringX;
import spc.webos.util.tree.TreeNode;

public class DefaultArray2FixedLenConverter2 implements IArray2FixedLenConverter2
{
	public int pack(ICompositeNode root, ICompositeNode pnode, byte[] fixedLen, int offset,
			IArrayNode value, TreeNode schema, IAtom2FixedLenConverter atom2fixedLen, String charset)
			throws Exception
	{
		if (value == null || value.size() == 0) return 0;
		MsgSchemaPO schemaVO = (MsgSchemaPO) schema.getTreeNodeValue();
		String fixmsg = schemaVO.getFixmsg(); // getRcvName(); modifed by chenjs
												// 2011-11-10
		if (fixmsgOrRcvName && StringX.nullity(fixmsg)) fixmsg = schemaVO.getRcvName();
		if (StringX.nullity(fixmsg))
		{
			if (log.isDebugEnabled()) log.debug("fixmsg is null for " + schemaVO.getEsbName());
			return 0;
		}
		// �������Ľ������ֶ�������Ϊ�ڶ��������е���ʼλ�� �ͳ��ȣ��м���.�ָ�
		fixmsg = fixmsg.replace('-', '|').replace('.', '|');
		int index = fixmsg.indexOf('|');
		if (index < 0)
		{
			if (log.isDebugEnabled()) log.debug("fixmsg unvalid for " + schemaVO.getEsbName()
					+ ", " + fixmsg);
			return 0;
		}
		String[] fix = StringX.split(fixmsg, "|");
		int start = Integer.parseInt(fix[0]); // �����������ô�������ʼλ��
		int arrayLen = Integer.parseInt(fix[1]); // ����ڵ��rcvname��ʾ�ܳ���
		// int start = Integer.parseInt(fixmsg.substring(0, index)); //
		// �����������ô�������ʼλ��
		// int arrayLen = Integer.parseInt(fixmsg.substring(index + 1)); //
		// ����ڵ��rcvname��ʾ�ܳ���

		offset += start;
		for (int i = 0; i < value.size(); i++)
		{
			INode item = value.getNode(i);
			if (item instanceof IAtomNode) atom2fixedLen.pack(fixedLen, offset, (IAtomNode) item,
					schemaVO, charset); // д�������е�ԭ�ӽڵ�
			else if (item instanceof ICompositeNode) CNode2FixedLenBytesUtil.pack2(
					(ICompositeNode) item, fixedLen, offset, schema, atom2fixedLen, this, charset); // д�������еĸ��ӽڵ�
			else log.warn("cannot support array node!!!!");
			offset += arrayLen; // �ۼƶ���λ��ƫ����
		}
		return arrayLen * value.size(); // ����ռ�ö������ܳ���
	}

	public int unpack(ICompositeNode rootCNode, ICompositeNode pnode, byte[] fixedLen, int offset,
			IArrayNode value, TreeNode schema, IAtom2FixedLenConverter atom2fixedLen, String charset)
			throws Exception
	{
		MsgSchemaPO schemaVO = (MsgSchemaPO) schema.getTreeNodeValue();
		String fixmsg = schemaVO.getFixmsg(); // getRcvName(); modifed by chenjs
												// 2011-11-10
		if (fixmsgOrRcvName && StringX.nullity(fixmsg)) fixmsg = schemaVO.getRcvName();
		if (StringX.nullity(fixmsg))
		{
			if (log.isDebugEnabled()) log.debug("fixmsg is null for " + schemaVO.getEsbName());
			return 0;
		}
		// �������Ľ������ֶ�������Ϊ�ڶ��������е���ʼλ�� �ͳ��ȣ��м���.�ָ�
		fixmsg = fixmsg.replace('-', '|').replace('.', '|');
		int index = fixmsg.indexOf('|');
		if (index < 0)
		{
			if (log.isDebugEnabled()) log.debug("fixmsg unvalid for " + schemaVO.getEsbName()
					+ ", " + fixmsg);
			return 0;
		}
		String[] fix = StringX.split(fixmsg, "|");
		int start = Integer.parseInt(fix[0]); // �����������ô�������ʼλ��
		int arrayLen = Integer.parseInt(fix[1]); // ����ڵ��rcvname��ʾ�ܳ���
		// int start = Integer.parseInt(fixmsg.substring(0, index)); //
		// �����������ô�������ʼλ��
		// int arrayLen = Integer.parseInt(fixmsg.substring(index + 1)); //
		// ����ڵ��rcvname��ʾ�ܳ���
		offset += start;

		byte type = (byte) schemaVO.getFtyp().charAt(1);
		int rows = (fixedLen.length - offset) / arrayLen;
		// ���û������ext1���ԣ����ʾʣ�µ���������ȫΪ����
		if (!StringX.nullity(schemaVO.getExt1()))
		{
			if (log.isDebugEnabled()) log.debug(schemaVO.getEsbName() + "'s ext1: "
					+ schemaVO.getExt1());
			Map root = new HashMap();
			root.put("pnode", pnode);
			root.put("root", rootCNode);
			rows = new Integer(StringX.trim(FTLUtil.freemarker(schemaVO.getExt1(), root)))
					.intValue();
			// rows = pnode.findAtom(schemaVO.getExt1(), null).intValue();
			// ����ڵ��ext1λ�÷���ⶨ������ʱ�ĸ��Ѿ��������ֶ��Ǳ�ʾ���������������
		}
		if (log.isDebugEnabled()) log.debug("offset: " + offset + ", fixedLen.len:"
				+ fixedLen.length + ", arrayLen:" + arrayLen + ", rows:" + rows);
		for (int i = 0; i < rows; i++)
		{
			if (type == INode.TYPE_MAP) value.add(CNode2FixedLenBytesUtil.unpack2(fixedLen, offset,
					schema, atom2fixedLen, this, charset));
			else value.add(atom2fixedLen.unpack(fixedLen, offset, schemaVO, charset));
			offset += arrayLen; // �ۼƶ���λ��ƫ����
		}
		return arrayLen * rows;
	}

	public DefaultArray2FixedLenConverter2()
	{
	}

	public DefaultArray2FixedLenConverter2(boolean fixmsgOrRcvName)
	{
		this.fixmsgOrRcvName = fixmsgOrRcvName;
	}

	protected boolean fixmsgOrRcvName = true;
	protected Logger log = LoggerFactory.getLogger(getClass());

	public boolean isFixmsgOrRcvName()
	{
		return fixmsgOrRcvName;
	}

	public void setFixmsgOrRcvName(boolean fixmsgOrRcvName)
	{
		this.fixmsgOrRcvName = fixmsgOrRcvName;
	}
}
