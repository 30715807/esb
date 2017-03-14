package spc.esb.data.sig;

import java.util.HashMap;
import java.util.Map;

import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;

/**
 * ԭ�ӽڵ��ǩ������ʽ�ӿ�
 * 
 * @author spc
 * 
 */
public interface AtomNode2SigContent
{
	String sigCnt(IMessage msg, String nodeCd, INode value, MsgSchemaPO schema);

	Map<String, AtomNode2SigContent> SIGS = new HashMap<>(); // ��ǰjvm����ע�������sig
}
