package spc.esb.data.util;

import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.model.MsgSchemaPO;

/**
 * ����ԭ�������ǩ�� �� ת���ܽӿ�
 */
public interface IAtomProcessor
{
	IAtomNode process(IMessage srcmsg, IAtomNode src, MsgSchemaPO schema, boolean esb2rcv,
			ICompositeNode pnode, String path, ICompositeNode tpnode) throws Exception;
}
