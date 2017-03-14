package spc.esb.data.util;

import spc.esb.data.AtomNode;
import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;
import spc.webos.util.StringX;

/**
 * �����������Ժ�tag��ǩ�����໥ת���� ��ʱESB��׼schema�ṹ�Ǳ�ǩģʽ������ϵͳ������ģʽ 2011-12-30
 * 
 * @author chenjs
 * 
 */
public class Tag2AttrAtomConverter extends AtomConverter
{
	public IAtomNode converter(IMessage msg, IAtomNode src, MsgSchemaPO schema, boolean esb2rcv,
			ICompositeNode pnode, String path, ICompositeNode tpnode) throws Exception
	{
		return esb2rcv ? tag2attr(msg, src, schema, esb2rcv, pnode, path, tpnode) : attr2tag(msg,
				src, schema, esb2rcv, pnode, path, tpnode);
	}

	// ��ǰ���Ľڵ㺬�����ԣ�Ŀ��ʹ�ñ�ǩ
	public IAtomNode attr2tag(IMessage msg, IAtomNode src, MsgSchemaPO schema, boolean esb2rcv,
			ICompositeNode pnode, String path, ICompositeNode tpnode) throws Exception
	{
		// ext1 ����ģʽΪamt:Ccy, amt�ڵ��Ccy����
		String attrNodeNm = null;
		String attrNm = null; // ������
		if (schema.getExt1().indexOf(':') > 0)
		{
			String[] nodeattr = StringX.split(schema.getExt1(), ":");
			attrNodeNm = nodeattr[0]; // �ڸ��ڵ������Խڵ���
			attrNm = nodeattr[1]; // ������
		}
		else attrNm = schema.getExt1(); // ���ext1���治����:, ���ʾ��ǰ�������ڸ��ڵ�
		INode attrNode = StringX.nullity(attrNodeNm) ? tpnode : tpnode.getNode(attrNodeNm);
		if (attrNode == null)
		{
			if (log.isDebugEnabled()) log.debug("attrNode is null by:" + attrNodeNm + ",tpnode:"
					+ tpnode);
			return null;
		}
		Object attr = attrNode.getExt(attrNm);
		return attr == null ? null : new AtomNode(StringX.null2emptystr(attr));
	}

	// ��ǰ���Ľڵ�ʹ�õ��Ǳ�ǩ, ��Ҫ�󶨵�һ��������
	public IAtomNode tag2attr(IMessage msg, IAtomNode src, MsgSchemaPO schema, boolean esb2rcv,
			ICompositeNode pnode, String path, ICompositeNode tpnode) throws Exception
	{
		if (src == null) return null;
		// ext1 ����ģʽΪamt:Ccy, amt�ڵ��Ccy����
		String attrNodeNm = null;
		String attrNm = null; // ������
		if (schema.getExt1().indexOf(':') > 0)
		{
			String[] nodeattr = StringX.split(schema.getExt1(), ":");
			attrNodeNm = nodeattr[0]; // �ڸ��ڵ������Խڵ���
			attrNm = nodeattr[1]; // ������
		}
		else attrNm = schema.getExt1(); // ���ext1���治����:, ���ʾ��ǰ�������ڸ��ڵ�
		INode attrNode = StringX.nullity(attrNodeNm) ? tpnode : tpnode.getNode(attrNodeNm);
		if (attrNode == null)
		{
			if (log.isDebugEnabled()) log.debug("attrNode is null by:" + attrNodeNm + ",tpnode:"
					+ tpnode);
			return null; // ����null��ʾ�˽ڵ�δ�����ܷ���tpnode
		}
		attrNode.setExt(attrNm, src.stringValue());
		if (log.isDebugEnabled()) log.debug("tag2attr:attrNodeNm:" + attrNodeNm + ",attrNm:"
				+ attrNm);
		return null; // ����null��ʾ�˽ڵ�δ�����ܷ���tpnode
	}

	public Tag2AttrAtomConverter()
	{
		name = "tag2attr";
	}
}
