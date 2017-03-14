package spc.esb.data.util;

import java.util.List;

import spc.esb.data.AtomNode;
import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.model.MsgSchemaPO;
import spc.webos.util.StringX;

/**
 * �����������Ժ�tag��ǩ�����໥ת���� ��ʱESB��׼schema�ṹ������ģʽ������ϵͳ�Ǳ�ǩģʽ 2011-12-30
 * 
 * @author chenjs
 * 
 */
public class Attr2TagAtomConverter extends AtomConverter
{
	public IAtomNode converter(IMessage msg, IAtomNode src, MsgSchemaPO schema, boolean esb2rcv,
			ICompositeNode pnode, String path, ICompositeNode tpnode) throws Exception
	{
		return esb2rcv ? attr2tag(msg, src, schema, esb2rcv, pnode, path, tpnode) : tag2attr(msg,
				src, schema, esb2rcv, pnode, path, tpnode);
	}

	// ��ǰ���Ľڵ㺬�����ԣ�Ŀ��ʹ�ñ�ǩ
	public IAtomNode attr2tag(IMessage msg, IAtomNode src, MsgSchemaPO schema, boolean esb2rcv,
			ICompositeNode pnode, String path, ICompositeNode tpnode) throws Exception
	{
		List<MsgSchemaPO> attrSchemas = schema.getAttributes();// �õ���ǰԭ�ӽڵ��������õ������б�
		if (attrSchemas == null || attrSchemas.size() == 0 || src.getExt() == null
				|| src.getExt().size() == 0) return src;

		for (int i = 0; i < attrSchemas.size(); i++)
		{
			MsgSchemaPO attrSchema = attrSchemas.get(i);
			String attrValue = (String) src.getExt(attrSchema.getEsbName());
			if (StringX.nullity(attrValue)) continue;
			if (log.isDebugEnabled()) log.debug("attr2tag:attr:" + attrSchema.getEsbName()
					+ ",tag.ext1: " + attrSchema.getExt1() + ",val:" + attrValue);
			tpnode.set(attrSchema.getExt1(), attrValue);
		}
		return new AtomNode(src.stringValue());
	}

	// ��ǰ���Ľڵ�ʹ�õ��Ǳ�ǩ, ��Ҫ�󶨵�һ��������
	public IAtomNode tag2attr(IMessage msg, IAtomNode src, MsgSchemaPO schema, boolean esb2rcv,
			ICompositeNode pnode, String path, ICompositeNode tpnode) throws Exception
	{
		List<MsgSchemaPO> attrSchemas = schema.getAttributes();// �õ���ǰԭ�ӽڵ��������õ������б�
		if (attrSchemas == null || attrSchemas.size() == 0) return src;
		IAtomNode atom = new AtomNode(src.stringValue());
		for (int i = 0; i < attrSchemas.size(); i++)
		{
			MsgSchemaPO attrSchema = attrSchemas.get(i);
			IAtomNode attrValue = pnode.findAtom(attrSchema.getExt1(), null); // ͨ��ext1���õ�ֵ���ڵ�ǰ���ڵ����ҵ����Ա�ǩֵ
			if (attrValue == null) continue;
			if (log.isDebugEnabled()) log.debug("tag2attr:attr:" + attrSchema.getEsbName()
					+ ",tag.ext1: " + attrSchema.getExt1() + ",val:" + attrValue);
			atom.setExt(attrSchema.getEsbName(), attrValue.stringValue()); // ����ǰԭ�ӱ�ǩ��������ֵ
		}
		return atom;
	}

	public Attr2TagAtomConverter()
	{
		name = "attr2tag";
	}
}
