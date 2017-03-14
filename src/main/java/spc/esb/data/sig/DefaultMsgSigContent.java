package spc.esb.data.sig;

import java.util.List;

import spc.esb.data.IAtomNode;
import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;
import spc.webos.util.StringX;

/**
 * ��ǩ��Ҫ�أ���|�߷ָ����һ������ҵ��Ҫ����
 * 
 * @author chenjs
 *
 */
public class DefaultMsgSigContent extends AbstractMsgSigContent
{
	protected boolean containLastDelim = true; // ��������ǩ������ʱ�������һ���ָ���
	protected String delim = "|"; // �ָ���
	protected boolean ignoreBlankSigCnt = true; // ���ǩ������Ϊ�գ������ǩ����

	public DefaultMsgSigContent()
	{
	}

	public DefaultMsgSigContent(String delim, boolean containLastDelim, boolean ignoreBlankSigCnt)
	{
		this.delim = delim;
		this.containLastDelim = containLastDelim;
		this.ignoreBlankSigCnt = ignoreBlankSigCnt;
	}

	// ƴ���ǩ��,����"|"�ָ�
	public byte[] getSigCnts(IMessage msg, String nodeCd, List<Object[]> sigCnts, String charset)
			throws Exception
	{
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < sigCnts.size(); i++)
		{
			Object[] items = (Object[]) sigCnts.get(i);
			String sigCnt = sigCnt(msg, nodeCd, (INode) items[0], (MsgSchemaPO) items[1]); // ��ǰ�ڵ��schema������Ϣ
			if (sigCnt == null) continue;
			if (StringX.nullity(sigCnt) && ignoreBlankSigCnt) continue; // ���ǩ������Ϊ�հף������
			buf.append(sigCnt);
			if (i < sigCnts.size() - 1 || containLastDelim) buf.append(delim);
		}
		if (log.isDebugEnabled())
			log.debug("nodeCd:" + nodeCd + ", charset:" + charset + ", sigCnts:[[" + buf + "]]");
		return buf.toString().getBytes(charset);
	}

	protected String sigCnt(IMessage msg, String nodeCd, INode value, MsgSchemaPO schema)
			throws Exception
	{
		AtomNode2SigContent ansc = AtomNode2SigContent.SIGS.get(schema.getSig());
		
		// Ĭ�ϸ���Ԫ�ص�ԭʼֵ���ǩ��Ҫ��
		if (ansc == null) return value == null ? StringX.EMPTY_STRING
				: StringX.trim(((IAtomNode) value).stringValue());

		return ansc.sigCnt(msg, nodeCd, value, schema);
	}

	public boolean isContainLastDelim()
	{
		return containLastDelim;
	}

	public void setContainLastDelim(boolean containLastDelim)
	{
		this.containLastDelim = containLastDelim;
	}

	public String getDelim()
	{
		return delim;
	}

	public void setDelim(String delim)
	{
		this.delim = delim;
	}

	public boolean isIgnoreBlankSigCnt()
	{
		return ignoreBlankSigCnt;
	}

	public void setIgnoreBlankSigCnt(boolean ignoreBlankSigCnt)
	{
		this.ignoreBlankSigCnt = ignoreBlankSigCnt;
	}
}
