package spc.esb.core.service;

import java.util.Map;

import spc.esb.core.NodeAttr;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.converter.CoreMessageConverter;
import spc.esb.data.validator.MessageErrors;
import spc.esb.model.NodePO;

/**
 * ����ͨ�÷���
 * 
 * @author chenjs
 * 
 */
public interface CoreService
{
	// ����body����
	public ICompositeNode decryptBody(IMessage msg, NodePO node, byte[] body) throws Exception;

	// ����body����
	public byte[] encryptBody(IMessage msg, NodePO node, ICompositeNode body) throws Exception;

	String[] getBroadcastService(IMessage msg) throws Exception;

	// �������ɺ�̨����ϵͳ��ˮ��. chenjs 2011-03-11
	void genRcvAppSN(IMessage msg) throws Exception;

	// ��֤ESB����ͷ
	MessageErrors validateHdr(IMessage msg) throws Exception;

	MessageErrors validateBody(IMessage msg) throws Exception;

	// ���ĶԱ��Ľ�������ת��
	void translator(IMessage msg) throws Exception;

	// ���������Ҫ��FAת�����򷵻���FAת����spring bean id, point Ϊ��־��0,1,2,3
	String getAdapterBeanId(IMessage msg, boolean request, boolean ba) throws Exception;

	CoreMessageConverter getCoreMsgConverter(IMessage msg, boolean request, boolean ba)
			throws Exception;

	// ���㵱ǰ�����ĺ����������ȼ�
	int priority(IMessage msg);

	// ��������������Դ
	boolean applyFCRes(IMessage msg);

	// �ͷ�����������Դ
	void releaseFCRes(IMessage msg) throws Exception;

	// ���������MQ ccsid��MQ��Ϣ�ı��utf8
	byte[] toBytes(byte[] xml, String ccsid) throws Exception;

	byte[] toBytes(byte[] xml, NodeAttr attr) throws Exception;

	// ����ȫ����ʱ����ӡ�����ֶ���Ϣ
	String msg2strWithoutSensitive(IMessage msg);

	// ʹ��ԭʼ���������Ʊ���(������8583��)�� ���ݷ��ͷ�ϵͳ��Ϣ�������ESB�淶��XML����
	byte[] toESBXML(byte[] originalBytes, NodePO node, Map attr) throws Exception;

	// �������еķǷ�xml�ַ�
	void handleUnvalidXMLChar(IMessage msg);

	// ��ȡ���ĵĳ�ʱʱ��
	int getTimeout(IMessage msg);

}
