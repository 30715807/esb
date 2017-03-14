package spc.esb.common.service;

import java.util.Map;

import spc.esb.data.IMessage;
import spc.esb.data.MessageAttr;
import spc.esb.data.MessageSchema;
import spc.esb.model.FAMessagePO;
import spc.esb.model.MessagePO;
import spc.esb.model.ServicePO;
import spc.webos.util.tree.TreeNode;

/**
 * ���Ķ������ӿ�
 * 
 * @author spc
 * 
 */
public interface MsgDefService extends MessageSchema
{
	Map<String, ServicePO> getServices(); // 415_20141015

	// 401_20131105 ͨ�����Ļ�ķ����location��ַ
	String getLocation(IMessage msg);

	// adde by chenjs 2011-09-05 for ��������
	ServicePO getService(String msgCd);

	ServicePO getService(String serviceId, String ver);

	// ���ݱ��ı�Ż�ȡ���Ķ���
	MessagePO getMessage(String msgCd);

	String getESBMsgCdByBA(String appCd, String appMsgCd);

	// ���ݷ��񷽱��ı�Ż�ȡ���ر��Ķ���
	MessagePO getRcvMessage(String rcvAppCd, String rcvMsgCd);

	// ���ݱ��ı�ŷ��ر�������
	MessageAttr getMsgAttr(String msgCd);

	// for FA start...
	// ͨ�����ͽڵ�ź�ESB���ı�Ż�ȡ��ǰ���ͽڵ�ŵı���������Ϣ�����綨����Ϣ��
	FAMessagePO getFAMessage(String sndNode, String esbMsgCd);

	// ͨ�����ı�źͷ��ͷ��ڵ�����ȡ���Ľṹ, ����֧����ǰ������
	TreeNode getMsgSchemaByFA(String sndNode, String esbMsgCd);

	// ͨ������ϵͳ�ķ����źͷ���ϵͳ��ŵõ�ESB��׼������
	String getESBMsgCdByFA(String sndNode, String sndMsgCd);

	String getSndMsgCdByFA(String sndNode, String esbMsgCd);

	// for FA end...
}
