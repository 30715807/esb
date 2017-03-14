package spc.esb.common.service;

import java.util.Map;

import spc.esb.data.IMessage;
import spc.esb.model.EndpointPO;
import spc.esb.model.FvMappingPO;
import spc.esb.model.NodePO;
import spc.esb.model.NodeServicePO;
import spc.webos.endpoint.Endpoint;

/**
 * ESB��Ϣ����ӿ�
 * 
 * @author sunqian at 2010-5-24
 * 
 */
public interface ESBInfoService
{
	// 401_20131024 ��MQԶ�̶��й�����Server 2 Serverģʽ�¸���Զ�̶��й�������ȷ�����
	NodePO getNodeByQMgr(String qmgr);

	// 401_20131023 ͨ��msglocal��Ϣȷ�����
	void identifySndApp(IMessage msg);

	// 401 2013-10-10 �endpoint��Ϣ
	Endpoint getEndpoint(String location);

	EndpointPO getEndpointVO(String location);

	// 400 2013-05-09 �õ��ڵ����������Ϣ
	NodeServicePO getNodeService(String node, String msgCd);

	// ����ӳ�����Id����ӳ��������
	Map<String, FvMappingPO> getFvMapping(String fvMapId);

	FvMappingPO getFVM(String fvMapId, String sndNodeApp, String sndValue);

	// ͨ��fvֵ�����ͽڵ�ͷ��ͷ��ṩ��ֵ�ҵ�ESB��׼ֵ
	String getESBValueFVM(String fvMapId, String sndNodeApp, String sndValue);

	// ͨ��ESBֵ��ȡ���ܷ�ֵ
	String getRcvValueFVM(String fvMapId, String sndNodeApp, String esbValue);

	/**
	 * ����ӳ����򽫷��ͷ�ϵͳ��ĳ��ֵת��Ϊ���ܷ�ϵͳ��ָ��ֵ��������ܷ�rcvNodeAppΪ����ת��ΪESB��׼ֵ
	 * 
	 * @param fvMapId
	 * @param sndNodeApp
	 * @param rcvNodeApp
	 * @param sndValue
	 * @return
	 */
	String getFvMapping(String fvMapId, String sndNodeApp, String rcvNodeApp, String sndValue);

	// ���������ֵ�Id���������ֵ����
	// Object getDict(String dictId);

	// ���ؽ���ڵ����
	NodePO getNode(String nodeApp);

	// ͨ��Զ�̷��͵Ļ����ͱ��ط���˿�ȷ�����ͷ�ϵͳ������ǰ��ͳһFA
	NodePO getNodeByUriPortIP(int port, String remoteIP);

	Map<String, NodePO> getNodes();
}
