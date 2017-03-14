package spc.esb.data.sig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spc.esb.data.IMessage;

/**
 * �ӱ����г�ȡ����ǩ����Ϣ����֯��ʽ, �е������߷ָ����ݣ��е��ö��ţ�����������ʽ�ָ�����
 * 
 * @author spc
 * 
 */
public interface MsgSigContent
{
	byte[] getSigCnts(IMessage msg, String nodeCd, List<Object[]> sigCnts, String charset)
			throws Exception;

	Map<String, MsgSigContent> SIG = new HashMap<>();
}
