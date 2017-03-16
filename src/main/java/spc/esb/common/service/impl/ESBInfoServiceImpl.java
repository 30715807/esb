package spc.esb.common.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import spc.esb.common.service.ESBInfoService;
import spc.esb.constant.ESBCommon;
import spc.esb.constant.ESBConfig;
import spc.esb.constant.ESBMsgLocalKey;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.model.EndpointPO;
import spc.esb.model.FvMappingPO;
import spc.esb.model.NodePO;
import spc.esb.model.NodeServicePO;
import spc.webos.endpoint.Endpoint;
import spc.webos.endpoint.EndpointFactory;
import spc.webos.persistence.jdbc.datasource.SwitchDS;
import spc.webos.service.BaseService;
import spc.webos.util.StringX;

/**
 * ESB�����ֵ䡢ӳ����򡢽�������Ϣ����
 * 
 * @author sunqian at 2010-05-24
 */
@Service("esbInfoService")
public class ESBInfoServiceImpl extends BaseService implements ESBInfoService
{
	protected volatile Map<String, NodePO> nodeMap = new HashMap<>();// �ڵ���Ϣ
	// ����uri, ip, port���Node��Ϣ
	protected volatile Map<String, NodePO> uriPortIPQMgrNodeMap = new HashMap<>();
	protected volatile Map<String, Map<String, FvMappingPO>> fvMappingMap = new HashMap<>();// �ֶ�ֵӳ�����
	protected volatile Map<String, NodeServicePO> nodeServiceMap = new HashMap<>();
	protected volatile Map<String, Object[]> endpointMap = new HashMap<>(); // ��ǰ���������endpoint��Ϣ

	public ESBInfoServiceImpl()
	{
		versionKey = ESBConfig.DB_esbInfoVerDt;
	}

	public NodePO getNodeByQMgr(String qmgr)
	{
		if (StringX.nullity(qmgr)) return null;
		return uriPortIPQMgrNodeMap.get(qmgr.toLowerCase());
	}

	public void identifySndApp(IMessage msg)
	{
		String sndAppCd = msg.getSndApp();
		if (!StringX.nullity(sndAppCd)) return;
		ICompositeNode msgLocal = msg.getMsgLocal();
		if (msgLocal == null)
		{
			log.info("msg local is null!!!");
			return;
		}
		String uri = StringX.null2emptystr(msgLocal.getNode(ESBMsgLocalKey.HTTP_URI));
		String port = StringX.null2emptystr(msgLocal.getNode(ESBMsgLocalKey.LOCAL_PORT), "0");
		String ip = StringX.null2emptystr(msgLocal.getNode(ESBMsgLocalKey.REMOTE_IP));
		NodePO node = getNodeByUriPortIP(Integer.parseInt(port), ip);
		if (log.isInfoEnabled()) log.info("uri:" + uri + ",ip:" + ip + ",port:" + port + ","
				+ (node == null ? "" : node.getAppCd()));
		if (node == null) return; // 410_20140611 ��ֹ���������ָ��
		// 451, ��ֹoracle���ݿ���mbrcdΪ�ո�����
		if (!StringX.nullity(StringX.null2emptystr(node.getMbrCd()).trim()))
			msg.setSndNode(node.getMbrCd());
		msg.setSndAppCd(node.getAppCd());
	}

	public Endpoint getEndpoint(String location)
	{
		if (StringX.nullity(location)) return null;
		Object[] obj = (Object[]) endpointMap.get(location);
		return obj == null ? null : (Endpoint) obj[1];
	}

	public EndpointPO getEndpointVO(String location)
	{
		if (StringX.nullity(location)) return null;
		Object[] obj = (Object[]) endpointMap.get(location);
		return obj == null ? null : (EndpointPO) obj[0];
	}

	public Map<String, NodePO> getNodes()
	{
		return nodeMap;
	}

	// ���ݳ�Ա��ź�Ӧ��ϵͳ��ŷ��ؽڵ���Ϣ����
	public NodePO getNode(String nodeApp)
	{
		if (StringX.nullity(nodeApp)) return null; // 503_20150214
		NodePO node = (NodePO) nodeMap.get(nodeApp);
		if (node == null) log.warn("node is null for " + nodeApp + " !!!");
		return node;
	}// ����nodeMap

	public NodeServicePO getNodeService(String node, String msgCd)
	{
		// log.info("node:"+node+",msgcd:"+msgCd+" "+nodeServiceMap);
		Map nodeMap = (Map) nodeServiceMap.get(node);
		if (nodeMap == null) return null;
		// log.info(nodeMap.get(msgCd))
		return (NodeServicePO) nodeMap.get(msgCd);
	}

	public FvMappingPO getFVM(String fvMapId, String sndNodeApp, String sndValue)
	{
		sndValue = sndValue.trim();
		Map fvmap = getFvMapping(fvMapId);
		if (fvmap == null)
		{
			if (log.isInfoEnabled()) log.info("fvmap is null for id: " + fvMapId);
			return null;
		}
		String sndKey = sndNodeApp + '$' + sndValue;
		return (FvMappingPO) fvmap.get(sndKey);
	}

	// ͨ��fvֵ�����ͽڵ�ͷ��ͷ��ṩ��ֵ�ҵ�ESB��׼ֵ
	public String getESBValueFVM(String fvMapId, String sndNodeApp, String sndValue)
	{
		FvMappingPO fvMappingVO = getFVM(fvMapId, sndNodeApp, sndValue);
		String esbValue = sndValue; // �����ֵ��׼��esb�ֵ�ֵ
		if (fvMappingVO != null) esbValue = fvMappingVO.getEsbFv(); // �������ϵͳ��Ҫ�����ֵ�ת��
		return esbValue;
	}

	// ͨ��ESBֵ��ȡ���ܷ�ֵ
	public String getRcvValueFVM(String fvMapId, String sndNodeApp, String esbValue)
	{
		Map fvmap = getFvMapping(fvMapId);
		if (fvmap == null)
		{
			if (log.isInfoEnabled()) log.info("fvmap is null for id: " + fvMapId);
			return esbValue;
		}
		FvMappingPO fvMappingVO = (FvMappingPO) fvmap.get('$' + sndNodeApp + '$' + esbValue);// �������ϵͳ��Ҫ�����ֵ�ת��
		return fvMappingVO != null ? fvMappingVO.getAppFv() : esbValue;
	}

	public String getFvMapping(String fvMapId, String sndNodeApp, String rcvNodeApp,
			String sndValue)
	{
		Map fvmap = getFvMapping(fvMapId);
		if (fvmap == null)
		{
			log.warn("fvmap is null for id: " + fvMapId);
			return sndValue;
		}
		String esbValue = getESBValueFVM(fvMapId, sndNodeApp, sndValue);
		return getRcvValueFVM(fvMapId, rcvNodeApp, esbValue);
	}

	// ����ӳ�����Id����ӳ��������
	public Map<String, FvMappingPO> getFvMapping(String fvMapId)
	{
		return fvMappingMap.get(fvMapId);
	}// ����fvMappingMap

	public Map<String, Map<String, FvMappingPO>> getFvMappingMap()
	{
		return fvMappingMap;
	}

	public NodePO getNodeByUriPortIP(int port, String ip)
	{
		NodePO nodeVO = uriPortIPQMgrNodeMap.get(String.valueOf(port));
		return nodeVO != null ? nodeVO : uriPortIPQMgrNodeMap.get(ip);
	}

	// ����ESB_NODE������nodeMap<mbrCd+appCd,nodeVO>
	private void loadNodeMap(Map nodeMap, Map uriPortIPQMgrNodeMap)
	{
		NodePO nodeVO = new NodePO();
		List list = persistence.get(nodeVO);
		for (int i = 0; i < list.size(); i++)
		{
			nodeVO = (NodePO) list.get(i);
			nodeMap.put(StringX.null2emptystr(nodeVO.getMbrCd()).trim()
					+ StringX.null2emptystr(nodeVO.getAppCd()).trim(), nodeVO);
			String host = StringX.null2emptystr(nodeVO.getHost());
			String localPort = StringX.null2emptystr(nodeVO.getLocalPort());
			// ���Ϊ��ǰ�ڵ�ָ����http uri
//			if (!StringX.nullity(nodeVO.getHttpURI()))
//				uriPortIPQMgrNodeMap.put(nodeVO.getHttpURI().trim().toLowerCase(), nodeVO);
			if (!StringX.nullity(localPort))
			{
				String[] localPorts = StringX.split(localPort, StringX.COMMA);
				for (int j = 0; j < localPorts.length; j++)
					uriPortIPQMgrNodeMap.put(localPorts[j], nodeVO);
			}
			if (!StringX.nullity(host))
			{
				String[] hosts = StringX.split(host, StringX.COMMA);
				for (int j = 0; j < hosts.length; j++)
					uriPortIPQMgrNodeMap.put(hosts[j], nodeVO);
			}
		}
		if (log.isInfoEnabled()) log.info("load NODE:" + nodeMap.size() + ", db size: "
				+ list.size() + ", nodes: " + nodeMap.keySet());
		if (log.isDebugEnabled()) log.debug("nodeMap: " + nodeMap);
	}

	// ����ESB_FVMAPPING������fvMappingMap<fvMapId,fvMappingVO>
	private void loadFvMappingMap(Map fvMappingMap)
	{
		FvMappingPO fvMappingVO = new FvMappingPO();
		List list = persistence.get(fvMappingVO);
		for (int i = 0; i < list.size(); i++)
		{
			fvMappingVO = (FvMappingPO) list.get(i);
			String id = fvMappingVO.getFvMapId();
			Map map = (Map) fvMappingMap.get(id);
			if (map == null)
			{
				map = new HashMap();
				fvMappingMap.put(id, map);
			}
			String esb2appKey = '$' + StringX.null2emptystr(fvMappingVO.getMbrCd())
					+ fvMappingVO.getAppCd() + '$' + fvMappingVO.getEsbFv();

			String app2esbKey = StringX.null2emptystr(fvMappingVO.getMbrCd())
					+ fvMappingVO.getAppCd() + '$' + fvMappingVO.getAppFv();
			map.put(esb2appKey, fvMappingVO);
			map.put(app2esbKey, fvMappingVO);
		}
		if (log.isInfoEnabled()) log.info(
				"load FVMAPPING:" + fvMappingMap.size() + ", fv keys:" + fvMappingMap.keySet());
		if (log.isDebugEnabled()) log.debug("fvMappingMap: " + fvMappingMap);
	}

	// ˢ��CacheService�ķ��������´����ݿ��м�����Ϣ
	public void refresh() throws Exception
	{
		try (SwitchDS ds = new SwitchDS(ESBCommon.ESB_DS))
		{
			log.info("refresh ds:{}", ESBCommon.ESB_DS);
			Map uriPortIPNodeMap = new HashMap();
			loadNodeMap(nodeMap, uriPortIPNodeMap);
			Map fvMappingMap = new HashMap();
			loadFvMappingMap(fvMappingMap);
			Map nodeServiceMap = loadNodeService(new HashMap());
			// ������ɺ�ͳһ����ԭ��Map���Ա�֤ˢ�µ�������
			this.nodeMap = nodeMap;
			this.uriPortIPQMgrNodeMap = uriPortIPNodeMap;
			this.fvMappingMap = fvMappingMap;
			this.nodeServiceMap = nodeServiceMap;

			// List<Endpoint> destroy = new ArrayList(); // �ȴ����ٵ�endpoint
			// this.endpointMap = loadEndpoints(new HashMap(endpointMap),
			// destroy);
			// for (int i = 0; i < destroy.size(); i++)
			// destroy.get(i).destroy(); // for webos 900, destory -> destroy
		}
	}

	private Map loadEndpoints(Map map, List<Endpoint> destroy) throws Exception
	{
		List list = null;
		try
		{
			list = persistence.get(new EndpointPO());
		}
		catch (Exception e)
		{
			log.warn("Fail to load endpoint info. " + e.toString());
			return map;
		}
		for (int i = 0; list != null && i < list.size(); i++)
		{
			EndpointPO endpointVO = (EndpointPO) list.get(i);
			Object[] obj = (Object[]) map.get(endpointVO.getLocation());
			if (obj == null
					|| !((EndpointPO) obj[0]).getUri().equalsIgnoreCase(endpointVO.getUri()))
			{
				if (obj != null && log.isInfoEnabled())
					log.info("endpoint change: from " + obj[0] + "  TO " + endpointVO);
				if (obj != null) destroy.add((Endpoint) obj[1]); // ׼������
				map.put(endpointVO.getLocation(), new Object[] { endpointVO,
						EndpointFactory.getInstance().getEndpoint(endpointVO.getUri()) });
			}
			else if (log.isDebugEnabled()) log.debug("no change: " + endpointVO);
		}
		if (log.isInfoEnabled()) log.info("endpoint: " + map.keySet());
		return map;
	}

	private Map loadNodeService(Map nodeService)
	{
		NodeServicePO nodeServiceVO = new NodeServicePO();
		List list = null;
		try
		{
			list = persistence.get(nodeServiceVO);
		}
		catch (Exception e)
		{
			log.warn("Fail to load node service info. " + e.toString());
			return nodeService;
		}
		for (int i = 0; list != null && i < list.size(); i++)
		{
			nodeServiceVO = (NodeServicePO) list.get(i);
			String node = StringX.null2emptystr(nodeServiceVO.getMbrCd()).trim()
					+ nodeServiceVO.getAppCd();
			Map nodeMap = (Map) nodeService.get(node);
			if (nodeMap == null) nodeService.put(node, nodeMap = new HashMap());
			nodeMap.put(nodeServiceVO.getMsgCd(), nodeServiceVO);
		}
		if (log.isInfoEnabled()) log.info("load node service size:" + list.size());
		return nodeService;
	}
}
