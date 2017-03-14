package spc.esb.common.service;

import spc.esb.data.IMessage;

/**
 * ����·�ɷ���ӿ�
 * 
 * @author sunqian at 2010-5-24
 * 
 */
public interface RouteService
{
	// ����Message��·�ɶ���QNAMEֵ
	public String getQname(IMessage msg) throws Exception;

	boolean isValidReplyToQ(IMessage msg, String replyToQ);

	// ����Message�Ĺ㲥·�ɶ����б�QNAMESֵ
	// public String getBroadcastQnames(IMessage msg) throws Exception;
}
