package spc.esb.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import spc.esb.common.service.ESBInfoService;
import spc.esb.common.service.MsgDefService;
import spc.esb.constant.ESBCommon;
import spc.esb.constant.ESBConfig;
import spc.esb.core.TagAttr;
import spc.esb.data.IMessage;
import spc.esb.data.MessageAttr;
import spc.esb.model.FAMessagePO;
import spc.esb.model.MessagePO;
import spc.esb.model.MsgSchemaPO;
import spc.esb.model.NodePO;
import spc.esb.model.NodeServicePO;
import spc.esb.model.ServicePO;
import spc.webos.constant.Common;
import spc.webos.persistence.jdbc.datasource.SwitchDS;
import spc.webos.service.BaseService;
import spc.webos.util.StringX;
import spc.webos.util.tree.ITreeCreator;
import spc.webos.util.tree.TreeNode;

/**
 * ESB���Ķ������
 * 
 * @author sunqian at 2010-05-24
 */
@Service("esbMsgDefService")
public class MsgDefServiceImpl extends BaseService implements MsgDefService
{
	protected volatile Map<String, ServicePO> serviceMap = new HashMap<>(); // �������Ϣ����
	// �������Ϣ����,�Է���Id&verΪkey
	protected volatile Map<String, ServicePO> serviceIdVerMap = new HashMap<>();
	protected boolean ba = true; // �Ƿ���BA��Ϣ��Ҫ����
	protected volatile Map<String, MessagePO> messageMap = new HashMap<>(); // ������Ϣ����,���������и���msgCd��ȡ������Ϣ
	protected volatile Map<String, MessagePO> rcvMsgMap = new HashMap<>(); // ������Ϣ����,��Ӧ�����и���rcvAppCd��rcvMsgCd��ȡ������Ϣ
	protected volatile Map<String, TreeNode> msgStructMap = new HashMap<>(); // ���Ľṹ��Ϣ��msgStructMap��ֻ����rcvAppCds�������ı��ġ�
	// protected Map msgAttrMap = new HashMap(); // ��������
	protected volatile Map<String, TreeNode> metaDataSchemaMap = new HashMap<>(); // ����Ԫ������Ϣ
	protected String metaDataSqlId = "esb_metadata";
	protected String metaDataSchemaSqlId = "esb_mdschema";
	protected String msgListSqlId = "esb_msglist";
	protected String structSqlId = "esb_msgstruct";

	protected boolean fa = false; // �Ƿ���FA��Ϣ��Ҫ����
	protected volatile Map<String, TreeNode> faMsgSchemaMap = new HashMap<>();
	protected volatile Map<String, FAMessagePO> faESBMsgCdMap = new HashMap<>(); // ͨ�����ͽڵ��ESB���ı�Ż�ȡ���ͷ����ı��
	protected volatile Map<String, FAMessagePO> faSndMsgCdMap = new HashMap<>(); // ͨ�����ͽڵ�ͷ��ͷ����ı�Ż�ȡESB��׼���ı��
	protected String faMsgListSqlId = "esb_famsglist";
	protected String faStructSqlId = "esb_famsgstruct";

	@Resource
	protected ESBInfoService esbInfoService;
	protected static final String FA_FLG = "$";

	public Map<String, ServicePO> getServices()
	{
		return this.serviceMap;
	}

	public String getLocation(IMessage msg)
	{
		String location = msg.getLocation();
		if (!StringX.nullity(location))
		{
			log.info("loc in msg: {}", location);
			return location;
		}
		if (msg.isRequestMsg())
		{
			String msgCd = msg.getMsgCd();
			NodeServicePO nodeServiceVO = esbInfoService.getNodeService(msg.getSndNodeApp(), msgCd);
			if (nodeServiceVO != null && !StringX.nullity(nodeServiceVO.getLocation()))
			{ // �����ͷ����������ķ����ַ��ϵ
				log.info("node service:{}", nodeServiceVO.getLocation());
				return nodeServiceVO.getLocation();
			}

			ServicePO serviceVO = getService(msgCd);
			if (serviceVO != null && !StringX.nullity(serviceVO.getLocation()))
			{ // �����Զ����˶�̬��location
				log.info("service:{}", serviceVO.getLocation());
				return serviceVO.getLocation();
			}

			NodePO nodeVO = esbInfoService.getNode(msg.getRcvNodeApp());
			if (nodeVO != null && !StringX.nullity(nodeVO.getLocation()))
			{ // ����ϵͳ�Զ����˶�̬��location
				log.info("node: {}", nodeVO.getLocation());
				return nodeVO.getLocation();
			}
		}
		else
		{ // �첽Ӧ��
			NodeServicePO nodeServiceVO = esbInfoService.getNodeService(msg.getRefSndNodeApp(),
					msg.getRefMsgCd());
			if (nodeServiceVO != null && !StringX.nullity(nodeServiceVO.getAsynRepLocation()))
			{ // �����ͷ����������ķ����ַ��ϵ
				log.info("asyn rep node service: {}", nodeServiceVO.getAsynRepLocation());
				return nodeServiceVO.getAsynRepLocation();
			}
		}
		log.info("no location!!!");
		return null;
	}

	public ServicePO getService(String msgCd)
	{
		return (ServicePO) serviceMap.get(msgCd);
	}

	public ServicePO getService(String serviceId, String ver)
	{
		return (ServicePO) serviceIdVerMap.get(serviceId + '$' + ver);
	}

	// for FA start...
	public FAMessagePO getFAMessage(String sndNode, String esbMsgCd)
	{
		String key = esbMsgCd + FA_FLG + sndNode;
		FAMessagePO msgVO = (FAMessagePO) faESBMsgCdMap.get(key);
		if (msgVO == null) log.info("cannot get snd msg vo by " + key);
		return msgVO;
	}

	public String getSndMsgCdByFA(String sndNode, String esbMsgCd)
	{
		FAMessagePO faMsgVO = getFAMessage(sndNode, esbMsgCd);
		return faMsgVO == null ? StringX.EMPTY_STRING : faMsgVO.getRcvMsgCd();
	}

	// ���ݷ��ͷ��ڵ�ͱ��ı�Ż�ȡESBMsgCd
	public String getESBMsgCdByFA(String sndNode, String sndMsgCd)
	{
		String key = sndMsgCd + FA_FLG + sndNode.toLowerCase(); // chenjs
		// 2012-01-19���ڴ���ʹ��Сд
		FAMessagePO faMsgVO = (FAMessagePO) faSndMsgCdMap.get(key);
		if (faMsgVO == null)
		{
			log.info("cannot get snd msg vo by " + key);
			return null;
		}
		// modified by chenjs 2012-01-19
		return StringX.nullity(faMsgVO.getEsbMsgCd()) ? faMsgVO.getMsgCd() : faMsgVO.getEsbMsgCd();
		// String esbMsgCd = faMsgVO == null ? null : faMsgVO.getMsgCd();
		// if (StringX.nullity(esbMsgCd)) log.info("cannot get esb msg cd by " +
		// key);
		// return esbMsgCd;
	}

	// ���ݷ��ͽڵ��ESB���ı�Ż�ȡFAMsgchema
	public TreeNode getMsgSchemaByFA(String sndNode, String esbMsgCd)
	{
		// modified by chenjs 2012-01-19
		FAMessagePO faMsgVO = getFAMessage(sndNode, esbMsgCd);
		// Ϊ�˼�����ũ�����ڵ�ʹ�÷�ʽ��faMsgVOΪ��ʱ��ʹ��esbMsgCdƴ��keyֵ
		String key = sndNode + '$' + (faMsgVO == null ? esbMsgCd : faMsgVO.getMsgCd());
		// String key = sndNode + '$' + esbMsgCd;
		TreeNode schema = (TreeNode) faMsgSchemaMap.get(key);
		if (schema == null) log.info("cannot get fa msgschema by: " + key + ", sndNode:" + sndNode
				+ ", esbMsgCd:" + esbMsgCd);
		return schema;
	}

	// fo FA end...

	// ���ݱ��ı�ŷ��ر��Ķ���
	public MessagePO getMessage(String msgCd)
	{
		MessagePO vo = (MessagePO) messageMap.get(msgCd);
		if (vo == null) log.info("cannot find msg definition by " + msgCd);
		return vo;
	}

	public String getESBMsgCdByBA(String appCd, String appMsgCd)
	{
		MessagePO msgvo = getRcvMessage(appCd, appMsgCd); // �����Ӧ��������Сдƥ��
		if (msgvo == null)
		{
			log.warn("msgvo is null by appCd:" + appCd + ", appMsgCd: " + appMsgCd);
			return null;
		}
		if (log.isDebugEnabled()) log.debug(
				"appCd:" + appCd + ", appMsgCd:" + appMsgCd + ", esb msgcd: " + msgvo.getMsgCd());
		return msgvo.getMsgCd();
	}

	public Map getMessageMap()
	{
		return messageMap;
	}

	// ���ݱ��ı�ŷ��ر��Ķ���
	public MessagePO getRcvMessage(String rcvAppCd, String rcvMsgCd)
	{
		MessagePO vo = (MessagePO) rcvMsgMap.get(rcvAppCd + rcvMsgCd);
		if (vo == null)
			log.warn("getRcvMessage is null for rcvAppCd:" + rcvAppCd + ", rcvMsgCd:" + rcvMsgCd);
		return vo;
	}

	public Map getRcvMsgMap()
	{
		return rcvMsgMap;
	}

	// ���ݱ��ı�ŷ��ر�������
	public MessageAttr getMsgAttr(String msgCd)
	{
		MessagePO msgVO = getMessage(msgCd);
		return msgVO == null ? null : new MessageAttr(msgVO.getMsgAttr());
	}

	// public Map getMsgAttrMap()
	// {
	// return msgAttrMap;
	// }

	// ���ݱ��ı�ŷ������α��Ľṹ����
	public TreeNode getMsgSchema(String msgCd)
	{
		TreeNode schema = null;
		MessagePO msgVO = getMessage(msgCd);
		// ��ǰ���ı���Ƿ����schemaӳ�䱨��. modified by chenjs 2010-10-09
		if (msgVO != null && !StringX.nullity(msgVO.getSchemaMsgCd()))
			schema = (TreeNode) msgStructMap.get(msgVO.getSchemaMsgCd());
		else schema = (TreeNode) msgStructMap.get(msgCd);
		// added by chenjs. 2010-9-30. ����һ��������Ϣ
		if (schema == null) log.info("msgCd(" + msgCd + ")'s schema is null!!!");
		return schema;
	}

	// ���ݱ��ı�ŵõ�Ԫ����schema�ṹ
	public TreeNode getMetaDataSchema(String msgCd)
	{
		return (TreeNode) metaDataSchemaMap.get(msgCd);
	}

	public Map getMsgStructMap()
	{
		return msgStructMap;
	}

	// ����ESB_FAMSG��,�õ�faMsgMap
	private void loadFAMessageMap(Map faESBMsgCdMap, Map faSndMsgCdMap)
	{
		FAMessagePO faMsgVO = new FAMessagePO();
		List list = persistence.get(faMsgVO);
		for (int i = 0; i < list.size(); i++)
		{
			faMsgVO = (FAMessagePO) list.get(i);
			// modified by chenjs 2012-01-19
			// �µ�esb_famsg��������esbmsgcd�ֶΣ����û����Ĭ�ϼ�����ũ�̰汾��ʹ��msgcd�ֶ�
			String esbMsgCd = StringX.nullity(faMsgVO.getEsbMsgCd()) ? faMsgVO.getMsgCd()
					: faMsgVO.getEsbMsgCd();
			faESBMsgCdMap.put(esbMsgCd + FA_FLG + faMsgVO.getRcvAppCd(), faMsgVO);

			// 2012-01-19 Ϊʲôֻ���������ģ��ѵ���Ϊ�������ټ���һ����ô����������
			// ��Ϊ����ǰ��ϵͳһ�����ı�Ŷ�Ӧ����ESB���ı��(����/Ӧ��)
			// Ϊ�˼�����ũ��, ����esb_msg������������Ʊ���һ�£�Ҳ��������appcd�Ĵ�Сд,
			// ���������´���������ı�ų���.��ȫ���أ�����ֻ����0��β

			// if (faMsgVO.getMsgCd().endsWith(Common.ESB_REQMSG_END))
			// faSndMsgCdMap.put(
			// faMsgVO.getRcvMsgCd() + FA_FLG + faMsgVO.getRcvAppCd(), faMsgVO);

			// chenjs 2012-01-19 ���ص��ڴ��� ʹ��Сд
			if (esbMsgCd
					.indexOf(
							'.') > 0
					|| esbMsgCd
							.endsWith(Common.ESB_REQMSG_END))
				faSndMsgCdMap.put(
						faMsgVO.getRcvMsgCd() + FA_FLG + (esbMsgCd.indexOf('.') > 0
								? faMsgVO.getRcvAppCd() : faMsgVO.getRcvAppCd().toLowerCase()),
						faMsgVO);
		}
		if (log.isInfoEnabled()) log.info(
				"load faESBMsgCdMap:" + faESBMsgCdMap.size() + ", keys:" + faESBMsgCdMap.keySet());
		if (log.isInfoEnabled()) log.info(
				"load faSndMsgCdMap: " + faSndMsgCdMap.size() + ", keys:" + faSndMsgCdMap.keySet());
	}

	// ����ESB_MSG������messageMap<msgCd,messageVO>h��rcvMsgMap��msgAttrMap
	private void loadMessageMap(Map messageMap, Map rcvMsgMap)
	{
		MessagePO messageVO = new MessagePO();
		List list = persistence.get(messageVO);
		for (int i = 0; i < list.size(); i++)
		{
			messageVO = (MessagePO) list.get(i);
			messageMap.put(messageVO.getMsgCd(), messageVO);
			// msgAttrMap.put(messageVO.getMsgCd(), new
			// MessageAttr(messageVO.getMsgAttr()));
			if (!StringX.nullity(messageVO.getRcvMsgCd()))
				rcvMsgMap.put(messageVO.getRcvAppCd() + messageVO.getRcvMsgCd(), messageVO);
		}
		if (log.isInfoEnabled()) log.info("load MSG:" + messageMap.size());
		if (log.isDebugEnabled()) log.debug(",keys:" + messageMap.keySet());
	}

	protected List loadBAMsgCd()
	{
		return (List) persistence.execute(msgListSqlId, null);
	}

	protected List loadBAMsgSchema()
	{
		return (List) persistence.execute(structSqlId, null);
	}

	protected List loadFAMsgCd()
	{
		return (List) persistence.execute(faMsgListSqlId, null);
	}

	protected List loadFAMsgSchema()
	{
		return (List) persistence.execute(this.faStructSqlId, null);
	}

	protected List loadMDMsgCd()
	{
		return (List) persistence.execute(this.metaDataSqlId, null);
	}

	protected List loadMDMsgSchema()
	{
		return (List) persistence.execute(this.metaDataSchemaSqlId, null);
	}

	// ����ESB_MSGSCHEMA������msgStructMap<msgCd,treeNode>
	// 510_20160522, ֧�ִ��ļ�ϵͳ���ر��Ľṹ
	protected void loadMsgSchema(Map msgSchemaMap, List msgList, List structList)
	{
		// Map param = new HashMap();
		// List msgList = (List) persistence.execute(msgListSqlId, param);//
		// ��ѯ���ж��ٱ��ı����Ҫ���ر��Ľṹ
		// List structList = (List) persistence.execute(structSqlId, param);//
		// ��ѯ���������ĵ�ȫ�����Ľṹ��Ϣ

		List temp = new ArrayList();
		for (int i = 0; i < msgList.size(); i++)
		{ // ��ÿ��������������treeNode
			String msgCd = msgList.get(i).toString();
			msgSchemaMap.put(msgCd, createTreeNode(structList, msgCd, temp));
		}
		if (log.isInfoEnabled())
			log.info("msgCd.size: " + msgList.size() + ", schema.size: " + structList.size());
	}

	// ˢ��CacheService�ķ��������´����ݿ��м�����Ϣ
	public void refresh() throws Exception
	{
		try (SwitchDS ds = new SwitchDS(ESBCommon.ESB_DS))
		{
			log.info("refresh ds:{}, version:{}_{}", ESBCommon.ESB_DS, ESBCommon.VERSION,
					ESBCommon.VERSION_DATE);
			// ���ر�����Ϣ�ͷ�������Ϣ��������
			Map messageMap = new HashMap();
			Map rcvMsgMap = new HashMap();
			// Map msgAttrMap = new HashMap();
			loadMessageMap(messageMap, rcvMsgMap);
			// ������ɺ�ͳһ����ԭ��Map���Ա�֤ˢ�µ�������
			this.messageMap = messageMap;
			this.rcvMsgMap = rcvMsgMap;
			// this.msgAttrMap = msgAttrMap;

			if (ba)
			{ // ���ر��Ľṹ��Ϣ
				log.info("loading ba info....");
				Map metaDataSchemaMap = new HashMap();
				Map msgStructMap = new HashMap();
				if (persistence.contain(metaDataSqlId) && persistence.contain(metaDataSchemaSqlId))
					loadMsgSchema(metaDataSchemaMap, loadMDMsgCd(), loadMDMsgSchema()); // �ȼ���Ԫ������Ϣ
				else log.warn("canot find meta data sql id: (" + metaDataSqlId + ", "
						+ metaDataSchemaSqlId + ")!!!");
				this.metaDataSchemaMap = metaDataSchemaMap; // ��ҪԤ������, ���ر��Ľṹʱʹ��

				loadMsgSchema(msgStructMap, this.loadBAMsgCd(), this.loadBAMsgSchema());
				this.msgStructMap = msgStructMap;
				if (log.isInfoEnabled())
					log.info("BA meta data:" + metaDataSchemaMap.size() + ", meta data keys:"
							+ metaDataSchemaMap.keySet() + ", MsgSchema:" + msgStructMap.size());
				if (log.isDebugEnabled()) log.debug("ba schema keys: " + msgStructMap.keySet());
			}
			if (fa)
			{ // ����FA��Ϣ
				log.info("loading fa info....");
				Map faESBMsgCdMap = new HashMap();
				Map faSndMsgCdMap = new HashMap();
				loadFAMessageMap(faESBMsgCdMap, faSndMsgCdMap);
				Map faMsgSchemaMap = new HashMap();
				loadMsgSchema(faMsgSchemaMap, this.loadFAMsgCd(), this.loadFAMsgSchema());
				this.faSndMsgCdMap = faSndMsgCdMap;
				this.faESBMsgCdMap = faESBMsgCdMap;
				this.faMsgSchemaMap = faMsgSchemaMap;
				if (log.isInfoEnabled()) log.info("fa schema:" + faMsgSchemaMap.size());
				if (log.isDebugEnabled()) log.debug("fa schema keys: " + faMsgSchemaMap.keySet());
			}
			log.info("loading service info....");
			Map serviceMap = new HashMap();
			Map serviceIdVerMap = new HashMap();
			try
			{ // ����������mapping�е�service��Ϣ��������û�����¼����쳣
				List services = persistence.get(new ServicePO());
				for (int i = 0; i < services.size(); i++)
				{
					ServicePO service = (ServicePO) services.get(i);
					if (serviceMap.containsKey(service.getReqMsgCd()))
						log.warn("service repeat reqmsgcd: " + service.getReqMsgCd() + "!!!");
					serviceMap.put(service.getReqMsgCd(), service);
					serviceIdVerMap.put(service.getServiceId() + '$' + service.getVer(), service);
				}
				this.serviceMap = serviceMap;
				this.serviceIdVerMap = serviceIdVerMap;
				if (log.isInfoEnabled()) log.info("services size: " + serviceMap.size());
				if (log.isDebugEnabled()) log.debug("services keys: " + serviceMap.keySet());
			}
			catch (Exception e)
			{
				log.warn("fail to load service info: " + e);
				log.debug("fail to load service info", e);
			}
			log.info("refresh over...");
		}
	}

	// ΪmsgStructs�е�ÿ��msgCd���ļ������νṹ
	private TreeNode createTreeNode(List msgSchemas, String msgCd, List temp)
	{
		temp.clear();
		MsgSchemaPO msgSchemaVO = new MsgSchemaPO();
		msgSchemaVO.setSeq(new Integer(0));
		temp.add(msgSchemaVO);
		for (int i = 0; i < msgSchemas.size(); i++)
		{
			msgSchemaVO = (MsgSchemaPO) msgSchemas.get(i);
			if (msgSchemaVO.getMsgCd().equals(msgCd))
			{
				temp.add(msgSchemaVO);
				// added by chenjs 2012-03-09 ��ȫ����ɾ������ߺ�������Ч��
				msgSchemas.remove(i);
				i--;
			}
		}
		if (temp.size() <= 1)
		{ // ��ǰ���ı��û��schema����
			log.warn("msgCd:" + msgCd + " has no schema data!!!");
			return null;
		}
		int schemaNum = temp.size() - 1;
		TreeNode root = new TreeNode();
		root.createTree(temp, new SchemaTreeCreator(this));
		if (root.getChildren() == null || root.getChildren().size() == 0)
		{
			log.warn("No children for msgCd:" + msgCd + ", list:" + temp + "!!!");
			return null;
		}
		if (log.isDebugEnabled()) log.debug("msgCd: " + msgCd + ", schema num: " + schemaNum
				+ ", first level child num:" + root.getChildren().size());
		return root;
	}

	public List getMsgValidator(String s)
	{
		return null;
	}

	public void setMsgListSqlId(String msgListSqlId)
	{
		this.msgListSqlId = msgListSqlId;
	}

	public void setStructSqlId(String structSqlId)
	{
		this.structSqlId = structSqlId;
	}

	public void setFaMsgListSqlId(String faMsgListSqlId)
	{
		this.faMsgListSqlId = faMsgListSqlId;
	}

	public void setFaStructSqlId(String faStructSqlId)
	{
		this.faStructSqlId = faStructSqlId;
	}

	public void setFa(boolean fa)
	{
		this.fa = fa;
	}

	public void setBa(boolean ba)
	{
		this.ba = ba;
	}

	public MsgDefServiceImpl()
	{
		versionKey = ESBConfig.DB_msgDefVerDt;
	}

	public void setEsbInfoService(ESBInfoService esbInfoService)
	{
		this.esbInfoService = esbInfoService;
	}

	public void setMetaDataSqlId(String metaDataSqlId)
	{
		this.metaDataSqlId = metaDataSqlId;
	}

	public void setMetaDataSchemaSqlId(String metaDataSchemaSqlId)
	{
		this.metaDataSchemaSqlId = metaDataSchemaSqlId;
	}
}

class SchemaTreeCreator implements ITreeCreator
{
	MsgDefServiceImpl msgDefService;
	protected Logger log = LoggerFactory.getLogger(getClass());

	public SchemaTreeCreator(MsgDefServiceImpl msgDefService)
	{
		this.msgDefService = msgDefService;
	}

	public void insertChild(TreeNode parent, TreeNode current)
	{
		MsgSchemaPO cschema = (MsgSchemaPO) current.getTreeNodeValue();
		TagAttr tagAttr = new TagAttr(cschema.getTagAttr());
		if (tagAttr.isMetaData())
		{ // �˽ڵ��ǩ��һ��Ԫ�������ýڵ�
			// String metaData = StringX.nullity(cschema.getMetaData()) ?
			// cschema.getExt1() : cschema
			// .getMetaData();
			String metaData = cschema.getMetaData();
			TreeNode metaSchema = msgDefService.getMetaDataSchema(metaData);
			if (metaSchema == null) log.warn(
					"cannot load meta data by " + metaData + ", esbname:" + cschema.getEsbName());
			else
			{
				current.setChildren(metaSchema.getChildren());
				current.setLeaf(false);
			}
		}
		if (tagAttr.isTagExtAttr())
		{ // ��ǰ��ǩ��һ����չ���Ա�ǩ��������ͨ���ӱ�ǩ����
			MsgSchemaPO pschema = (MsgSchemaPO) parent.getTreeNodeValue();
			pschema.addAttribute(cschema);
		}
		else parent.insertChild(current);
	}
}
