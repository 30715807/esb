package spc.esb.data.sig;

import spc.esb.data.IAtomNode;
import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;
import spc.webos.util.StringX;

/**
 * �������ж���֧��Ҫ�󣬽������л���������Ϊǩ��Ҫ�� <amt Ccy="CNY">100.00</amt> ǩ������Ϊ: CNY100.00
 * 
 * @author chenjs
 * 
 */
public class CCYAtomNode2SigContent extends AbstractAtomNode2SigContent
{
	public CCYAtomNode2SigContent()
	{
		name = "CCY";
	}

	public String sigCnt(IMessage msg, String nodeCd, INode value, MsgSchemaPO schema)
	{
		if (value == null) return StringX.EMPTY_STRING;
		String val = StringX.trim(((IAtomNode) value).stringValue());
		String ccy = StringX.null2emptystr(value.getExt("Ccy"), "CNY");
		// ͳһʹ��RMB
		return StringX.nullity(val) ? StringX.EMPTY_STRING : ccy + val;
	}
}
