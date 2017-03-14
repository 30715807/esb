package spc.esb.core.service.impl;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import spc.esb.common.service.ESBInfoService;
import spc.esb.common.service.MsgDefService;
import spc.esb.constant.ESBCommon;
import spc.esb.constant.ESBConfig;
import spc.esb.constant.ESBMsgCode;
import spc.esb.constant.ESBMsgLocalKey;
import spc.esb.constant.ESBRetCode;
import spc.esb.core.NodeAttr;
import spc.esb.core.NodeServiceAttr;
import spc.esb.core.TagAttr;
import spc.esb.core.service.CoreService;
import spc.esb.core.service.SeqNoService;
import spc.esb.data.Array2Node2XML;
import spc.esb.data.CompositeNode;
import spc.esb.data.DefaultNode2XML;
import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.Message;
import spc.esb.data.MessageAttr;
import spc.esb.data.SchemaCompositeNode;
import spc.esb.data.converter.CoreMessageConverter;
import spc.esb.data.converter.SOAPConverter;
import spc.esb.data.util.MessageTranslator;
import spc.esb.data.util.MsgFTLUtil;
import spc.esb.data.validator.AbstractMessageValidator;
import spc.esb.data.validator.MessageErrors;
import spc.esb.data.xml.XMLConverterUtil;
import spc.esb.flowctrl.DefaultFCKey;
import spc.esb.flowctrl.FCKey;
import spc.esb.model.MessagePO;
import spc.esb.model.MsgSchemaPO;
import spc.esb.model.NodePO;
import spc.esb.model.NodeServicePO;
import spc.esb.model.ServicePO;
import spc.webos.config.AppConfig;
import spc.webos.constant.AppRetCode;
import spc.webos.constant.Common;
import spc.webos.endpoint.Executable;
import spc.webos.exception.AppException;
import spc.webos.exception.ArgsErrException;
import spc.webos.exception.Status;
import spc.webos.service.BaseService;
import spc.webos.service.resallocate.ResourcePoolService;
import spc.webos.util.CipherUtil;
import spc.webos.util.FTLUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;
import spc.webos.util.charset.EBCDUtil;
import spc.webos.util.tree.TreeNode;

/**
 * for IBM.MB & YC.MB
 * 
 * @author chenjs
 * 
 */
public class CoreServiceImpl extends BaseService implements CoreService
{
	// ����body����
	public ICompositeNode decryptBody(IMessage msg, NodePO node, byte[] body) throws Exception
	{
		byte[] nbody = CipherUtil.desDecrypt(body, node.getDesKey().getBytes());
		if (log.isDebugEnabled()) log.debug("Des key:" + node.getDesKey() + "\nnbody:"
				+ new String(nbody) + "\nbody:" + StringX.base64(body));
		else if ((Boolean) AppConfig.getInstance().getProperty(ESBConfig.SECURITY_trace, false))
			log.info("trace Des key:" + node.getDesKey() + "\nnbody:" + new String(nbody)
					+ "\nbody:" + StringX.base64(body));
		return SOAPConverter.getInstance().deserialize2composite(nbody);
	}

	// ����body����
	public byte[] encryptBody(IMessage msg, NodePO node, ICompositeNode body) throws Exception
	{
		byte[] buf = body.toXml("body", false).getBytes(Common.CHARSET_UTF8);
		byte[] nbody = CipherUtil.desEncrypt(buf, node.getDesKey().getBytes());
		if (log.isDebugEnabled()) log.debug("Des key:" + node.getDesKey() + "\nbody:"
				+ new String(buf) + "\nnbody:" + StringX.base64(nbody));
		else if ((Boolean) AppConfig.getInstance().getProperty(ESBConfig.SECURITY_trace, false))
			log.info("trace Des key:" + node.getDesKey() + "\nbody:" + new String(buf) + "\nnbody:"
					+ StringX.base64(nbody));
		return nbody;
	}

	protected String getBroadcastLocation(IMessage msg) throws Exception
	{
		String msgCd = msg.getMsgCd();
		NodeServicePO nodeServiceVO = esbInfoService.getNodeService(msg.getSndNodeApp(), msgCd);
		if (nodeServiceVO != null && !StringX.nullity(nodeServiceVO.getLocation()))
		{ // �����ͷ����������ķ����ַ��ϵ
			if (log.isInfoEnabled()) log.info("node service:" + nodeServiceVO.getLocation());
			return nodeServiceVO.getLocation();
		}
		ServicePO serviceVO = msgDefService.getService(msgCd);
		if (serviceVO != null && !StringX.nullity(serviceVO.getLocation()))
		{ // �����Զ����˶�̬��location
			if (log.isInfoEnabled()) log.info("service:" + serviceVO.getLocation());
			return serviceVO.getLocation();
		}
		return null;
	}

	public String[] getBroadcastService(IMessage msg) throws Exception
	{
		String location = getBroadcastLocation(msg);
		if (StringX.nullity(location)) return null;
		String services = StringX.trim(FTLUtil.freemarker(location, MsgFTLUtil.model(null, msg)))
				.replaceAll(" ", "");
		if (StringX.nullity(services))
		{
			log.warn("broadservices is null");
			return null;
		}
		return StringX.split(services, StringX.COMMA);
	}

	public MessageErrors validateHdr(IMessage msg) throws Exception
	{
		if (!isValidateHeader(msg))
		{
			log.info("validateHeader false");
			return null;
		}
		log.info("chk header");
		boolean isRequestMsg = msg.isRequestMsg();
		// String msgCd = msg.getMsgCd();
		// 1. check esb header
		TreeNode esbHdrSchema = msgDefService
				.getMsgSchema(isRequestMsg ? ESBMsgCode.MSGCD_ESBREQ : ESBMsgCode.MSGCD_ESBREP);
		MessageErrors errors = new MessageErrors(msg);
		AbstractMessageValidator.validate(msg, esbHdrSchema, IMessage.TAG_HEADER, msg.getHeader(),
				errors);
		if (errors.getErrorCount() > 0 && log.isInfoEnabled())
			log.info("check esb hdr errors:" + errors.toCNode()); // 452,
																	// У�������Ҫinfo�������
		if (errors.getErrorCount() > 0) throw new ArgsErrException(errors);
		return errors;
	}

	public MessageErrors validateBody(IMessage msg) throws Exception
	{
		if (!isValidateBody(msg))
		{
			log.info("validateBody false");
			return null;
		}
		boolean isRequestMsg = msg.isRequestMsg();
		String msgCd = msg.getMsgCd();
		MessagePO msgVO = msgDefService.getMessage(msgCd);
		if (msgVO == null) return null;
		MessageAttr attr = new MessageAttr(msgVO.getMsgAttr());
		if (attr.isIgnoreBody())
		{ // added by chenjs 2011-05-25 ���ڴ�͸���͵Ĵ��Ĳ�У��body�塣�������
			log.info("msg is IgnoreBody");
			return null;
		} // added by chenjs 2011-05-25 end
		log.info("chk body");
		MessageErrors errors = new MessageErrors(msg);
		// 2. check msg fix head
		ICompositeNode chkCNode = isRequestMsg ? msg.getRequest() : msg.getResponse();
		if (chkCNode == null || chkCNode.size() == 0)
		{
			log.warn("chk body is null, request: " + isRequestMsg);
			chkCNode = new CompositeNode(); // modifed by chenjs 2011-12-22
			// ���û����ʹ�ÿ�cnode����У��
			// return errors;
		}
		// modified by spc 2011-01-12, ������ϵͳ�ı���ͷ������Ϣ���뵽Node����
		String rcvNodeApp = isRequestMsg ? msg.getRcvNodeApp() : msg.getSndNodeApp();
		NodePO rcvNode = esbInfoService.getNode(rcvNodeApp);
		NodePO sndNode = esbInfoService.getNode(msg.getSndNodeApp());
		if (rcvNode == null) log.warn("canot find rcv node by " + rcvNodeApp);
		// modifed by chenjs 2011-09-28 ���̶�ͷ��ǩ���뵽ESB_MSG��֧��
		String hdrTag = getHdrTag(msgVO, rcvNode, isRequestMsg);
		String hdrSchema = getHdrSchema(msgVO, rcvNode, isRequestMsg);

		if (!StringX.nullity(hdrTag) && !StringX.nullity(hdrSchema))
		{
			TreeNode bodyHdrSchema = msgDefService.getMsgSchema(hdrSchema);
			ICompositeNode bodyHdrCNode = chkCNode.findComposite(hdrTag, null);
			if (bodyHdrCNode != null && bodyHdrSchema != null)
			{
				log.info("chk body fix hdr, hdrTag: {}, hdrSchema: {}", hdrTag, hdrSchema);
				AbstractMessageValidator.validate(msg, bodyHdrSchema, hdrTag, bodyHdrCNode, errors);
			}
			else log.warn("bodyHdrCNode is null or bodyHdrSchema is null!!!");
		}
		// 3. check msg
		TreeNode schema = msgDefService.getMsgSchema(msgCd);
		if (schema != null) AbstractMessageValidator.validate(msg, schema, null, chkCNode, errors);
		handleErrors(msg, errors, sndNode);
		return errors;
	}

	// ����ESBУ�����, �������������ж��Ƿ񵲻�
	protected void handleErrors(IMessage msg, MessageErrors errors, NodePO sndNode) throws Exception
	{
		if (errors.getErrorCount() <= 0) return;
		boolean isRequestMsg = msg.isRequestMsg();
		NodeAttr nodeAttr = new NodeAttr(sndNode.getAppAttr());
		// if (isRequestMsg) throw new MsgErrException(errors); // �������������ֱ���쳣
		// else log.warn("response msg is err: " +
		// errors.toCNode().toXml("errors", true)); // Ӧ�����򾯸�

		// modified by guodd 20120331 ����������Ĳ��ҷ��ͷ��ڵ���Ҫ�쳣�˳���ֱ���쳣 or
		// �����Ӧ���Ĳ��ҷ��ͷ��ڵ���Ҫ�쳣�˳���ֱ���쳣
		// 452, һ��У�������info������־���
		if (log.isInfoEnabled()) log.info((isRequestMsg ? "request" : "response") + " msg, exExit:"
				+ nodeAttr.isRequestExExit() + ", errors: "
				+ errors.toCNode().toXml("errors", true)); // Ӧ�����򾯸�
		if ((isRequestMsg && nodeAttr.isRequestExExit())
				|| (!isRequestMsg && nodeAttr.isResponseExExit()))
			throw new ArgsErrException(errors);
	}

	public void translator(IMessage msg) throws Exception
	{
		// adjust(msg); // ת����һ����ڵ�, ��ת��ʱ�������ڵ����
		boolean isRequestMsg = msg.isRequestMsg();
		String msgCd = msg.getMsgCd();

		// added by chenjs 2011-05-25 ���ڴ�͸���͵Ĵ��Ĳ�ת��body�塣�������
		MessagePO msgVO = msgDefService.getMessage(msgCd);
		if (msgVO == null) return; // û������esb_msg��ֱ������
		MessageAttr attr = new MessageAttr(msgVO.getMsgAttr());
		if (attr.isIgnoreBody())
		{
			log.info("translator IgnoreBody");
			return;
		}

		// added by chenjs 2011-10-14 ��ʽ��ESB����ͷtransaction/header�µı�ǩ˳��
		msg.setHeader(SchemaCompositeNode.getInstance(
				msgDefService.getMsgSchema(
						isRequestMsg ? ESBMsgCode.MSGCD_ESBREQ() : ESBMsgCode.MSGCD_ESBREP()),
				msg.getHeader()));

		// added by chenjs 2011-05-25 end
		ICompositeNode translatorCNode = isRequestMsg ? msg.getRequest() : msg.getResponse();
		// 2012-07-10 chenjs, ������ͱ���Ϊ��Ҳ��Ҫִ��ת�������Ա����Ĭ��ֵ
		if (translatorCNode == null) translatorCNode = new CompositeNode();
		// modified by chenjs 2012-08-06 ���������Ƿ��Զ��������ȼ�����ͨ�����ã������������Ϊ��Ҫ���������
		boolean autoFilterUndefinedTag = (attr.isAutoFilterUndefinedTag()
				|| isAutoFilterUndefinedTag(msg));
		ICompositeNode ntranslatorCNode = autoFilterUndefinedTag ? new CompositeNode()
				: new CompositeNode(translatorCNode);
		TreeNode schema = msgDefService.getMsgSchema(msgCd);
		TreeNode bodyHdrSchema = null;
		ICompositeNode nbodyHdrCNode = null;

		// 1. fix body hdr ��������ת��
		// MessageVO msgVO = msgDefService.getMessage(isRequestMsg ? msgCd :
		// msg.getRefMsgCd());
		// modified by spc 2011-01-12, ������ϵͳ�ı���ͷ������Ϣ���뵽Node����
		String rcvNodeApp = isRequestMsg ? msg.getRcvNodeApp() : msg.getSndNodeApp();
		NodePO rcvNode = esbInfoService.getNode(rcvNodeApp);
		if (rcvNode == null) log.warn("canot find node by " + rcvNodeApp);
		// modifed by chenjs 2011-09-28 ���̶�ͷ��ǩ���뵽ESB_MSG��֧��
		String hdrTag = getHdrTag(msgVO, rcvNode, isRequestMsg);
		String hdrSchema = getHdrSchema(msgVO, rcvNode, isRequestMsg);

		if (!StringX.nullity(hdrTag) && !StringX.nullity(hdrSchema))
		{
			bodyHdrSchema = msgDefService.getMsgSchema(hdrSchema);
			if (bodyHdrSchema == null) log.warn("schema is null for " + hdrSchema);
			ICompositeNode bodyHdrCNode = translatorCNode.findComposite(hdrTag, null);
			if (bodyHdrCNode != null && bodyHdrSchema != null)
			{
				nbodyHdrCNode = new CompositeNode();
				translator.translateMap(bodyHdrSchema, msg, bodyHdrCNode, nbodyHdrCNode,
						isRequestMsg);
				ntranslatorCNode.set(hdrTag, nbodyHdrCNode);
				log.info("translate body fix hdr, hdrTag: {}, hdrSchema: {}", hdrTag, hdrSchema);
			}
			else log.info("bodyHdrCNode is null or bodyHdrSchema is null!!!");
		}

		// 2. body ��������ת��
		if (schema == null && nbodyHdrCNode == null)
		{ // ��û��schema Ҳû��body header��ʱ���򷵻�
			log.info("body schema is null & nbodyHdrCNode is empty!!!");
			return;
		}
		if (schema != null)
		{
			log.info("translate body:: autoFilterUndefinedTag:{}", autoFilterUndefinedTag);
			translator.translateMap(schema, msg, translatorCNode, ntranslatorCNode, isRequestMsg,
					autoFilterUndefinedTag, StringX.EMPTY_STRING);
			log.debug("translate body end...");
		}

		// modified by sunqian 2010-01-11 ���Ӻ��ĵı��ı�ǩת������
		ntranslatorCNode = mbConvert(msg, schema, attr, ntranslatorCNode, isRequestMsg,
				nbodyHdrCNode, hdrTag);

		// added by chenjs 2011-10-14
		// ����Schema���ƣ��ﵽ��MB���reqeust/responseʱ�ǰ�schema˳��
		ntranslatorCNode = SchemaCompositeNode.getInstance(schema, ntranslatorCNode);
		if (log.isTraceEnabled())
			log.trace("after translator:: ntranslatorCNode: " + ntranslatorCNode.toXml(
					isRequestMsg ? "request" : "response", true, DefaultNode2XML.getInstance()));
		if (isRequestMsg) msg.setRequest(ntranslatorCNode);
		else msg.setResponse(ntranslatorCNode);
	}

	protected boolean isContainArray(IMessage msg)
	{ // �������Ƿ��������
		String msgCd = msg.getMsgCd();
		MessageAttr attr = msgDefService.getMsgAttr(msgCd);
		if (attr != null && attr.isContainArray())
		{
			log.info("{} contain array", msgCd);
			return true;
		}
		return false;
	}

	protected void adjust(IMessage msg)
	{ // ����б��İ������飬����ܵ�����һ����Ԫ�ص����
		if (!isContainArray(msg)) return;
		TreeNode schema = msgDefService.getMsgSchema(msg.getMsgCd());
		if (schema != null) msg.setBody(MessageTranslator.adjust(schema, msg.getBody()));
	}

	// 401_20131225 ��mb �仯xml���Ľṹ������Ϊһ������
	protected ICompositeNode mbConvert(IMessage msg, TreeNode schema, MessageAttr attr,
			ICompositeNode ntranslatorCNode, boolean isRequestMsg, ICompositeNode nbodyHdrCNode,
			String hdrTag) throws Exception
	{
		// modified by sunqian 2010-01-11 ���Ӻ��ĵı��ı�ǩת������
		if (schema == null || attr == null || !attr.isMbConvert()) return ntranslatorCNode;
		log.info("mb core converter...");
		ICompositeNode targetCNode = new CompositeNode();
		// modified by spc 2011-01-13 convertMapֻ���ṹת������������ת������Ϊ����ת���Ѿ�����
		// translator.processMap ��һ�����ڷ�������������������ת����xml�ṹת��
		XMLConverterUtil.convertMap(schema, msg, ntranslatorCNode, targetCNode, isRequestMsg);
		// translator.processMap(schema, msg, translatorCNode, targetCNode,
		// isRequestMsg);
		if (nbodyHdrCNode != null) targetCNode.set(hdrTag, nbodyHdrCNode);
		// ntranslatorCNode = targetCNode;
		return targetCNode;
		// modified by sunqian 2010-01-11 end
	}

	protected String getHdrSchema(MessagePO msgVO, NodePO rcvNode, boolean request)
	{
		if (!StringX.nullity(msgVO.getHdrSchema())) return msgVO.getHdrSchema();
		return null;
	}

	protected String getHdrTag(MessagePO msgVO, NodePO rcvNode, boolean request)
	{
		if (!StringX.nullity(msgVO.getHdrTag())) return msgVO.getHdrTag();
		return null;
	}

	public String getAdapterBeanId(IMessage msg, boolean request, boolean ba) throws Exception
	{
		if (ba)
		{ // added by chenjs 2011-12-29 �����BAһ�ˣ�����һ������ϵͳ�Ĳ�ͬ�������ò�ͬ��������bean id
			// added by chenjs 2012-01-01 ���������񷽿���ʹ�ñ��ĵ�babean, ������������ʵ��,
			// ����ʱ�������֪�����ı��
			MessagePO msgVO = msgDefService.getMessage(msg.getMsgCd());
			if (msgVO != null && !StringX.nullity(msgVO.getAdapterBean()))
				return msgVO.getAdapterBean();
		}
		NodePO node = esbInfoService.getNode(
				((!ba && request) || (ba && !request)) ? msg.getSndNodeApp() : msg.getRcvNodeApp());
		return node == null ? null : (ba ? node.getBaBeanId() : node.getFaBeanId());
	}

	public CoreMessageConverter getCoreMsgConverter(IMessage msg, boolean request, boolean ba)
			throws Exception
	{
		String beanId = getAdapterBeanId(msg, request, ba); // ��ȡfabeanId
		if (StringX.nullity(beanId)) return null;
		log.info("CMC: {}, request:{}, ba:{}", beanId, request, ba);
		CoreMessageConverter cmc = CoreMessageConverter.CORE_MSG_CVTERS.get(beanId);
		// 415_20141015 ����������mb spring xml����Ԥ������xml bean,
		// ͨ��esb_config����ж�̬����spring����
		if (cmc == null) cmc = (CoreMessageConverter) SpringUtil.getInstance().getBean(beanId,
				CoreMessageConverter.class);
		if (cmc == null) log.warn("No CMC by: " + beanId);
		return cmc;
	}

	public void genRcvAppSN(IMessage msg) throws Exception
	{
		if (!msg.isRequestMsg()) return;
		String rcvAppSN = msg.getRcvAppSN();
		if (!StringX.nullity(rcvAppSN)) return; // ����������Ѿ����ڽ��շ���ˮ����ʹ��ԭ������Ϣ

		// modified by chenjs 2012-01-01 ������ˮ���������÷���esb_service����
		ServicePO serviceVO = msgDefService.getService(msg.getMsgCd());
		String rcvSNBeanId = (serviceVO == null ? null : serviceVO.getRcvSNBeanId());
		if (StringX.nullity(rcvSNBeanId)) return;
		// 415_20141015 ����������mb spring xml����Ԥ������xml bean,
		// ͨ��esb_config����ж�̬����spring����
		SeqNoService seqNoService = (SeqNoService) SpringUtil.getInstance().getBean(rcvSNBeanId,
				SeqNoService.class);
		if (seqNoService == null)
		{
			log.warn("seqNoService is null for " + msg.getMsgCd() + ", beanId: " + rcvSNBeanId);
			return;
		}
		rcvAppSN = seqNoService.genSN("RCVAPPSN", msg, null);
		msg.setRcvAppSN(rcvAppSN);
		if (log.isInfoEnabled())
			log.info("rcvAppSN:[" + rcvAppSN + "], rcvSNBeanId:" + rcvSNBeanId);
	}

	public int priority(IMessage msg)
	{
		MessagePO msgVO = msgDefService.getMessage(msg.getMsgCd());
		NodePO sndNodeVO = esbInfoService.getNode(msg.getSndNodeApp());
		if (msgVO == null || sndNodeVO == null) return 0;
		MessageAttr attr = new MessageAttr(msgVO.getMsgAttr());
		int priority = attr.priority() + new NodeAttr(sndNodeVO.getAppAttr()).priority();
		return priority < 0 ? 0 : (priority > 9 ? 9 : priority); // ���ǰ�������ȼ�������0-9��
	}

	// msg��͸ģʽ��add by sunqian
	public boolean isPrompt(IMessage msg)
	{
		return false;
	}

	public boolean applyFCRes(IMessage msg)
	{
		if (fcResourcePoolService == null || fcLocation == null) return true;
		Status status = null;
		try
		{
			String msgCd = msg.getMsgCd();
			String key = fcKey.key(msg, msgDefService.getMessage(msgCd), priority(msg));
			String sn = msg.getMsgSn();
			if (key == null || !fcResourcePoolService.contain(key))
			{
				if (log.isDebugEnabled()) log.debug("apply.key[" + key + "] not in DB");
				return true;
			}
			log.info("start to apply FC...");
			MessageAttr attr = msgDefService.getMsgAttr(msgCd);
			StringBuffer xml = new StringBuffer(
					"<Envelope><Header><msgCd>fcapply</msgCd><sndAppCd>MB</sndAppCd><sndDt>");
			xml.append(msg.getSndDt());
			xml.append("</sndDt><seqNb>");
			xml.append(SpringUtil.JVM + "-" + msg.getSeqNb());
			xml.append("</seqNb></Header><Body><sn>");
			xml.append(sn);
			xml.append("</sn><key>" + key);
			xml.append("</key><holdTm>");
			xml.append(attr == null ? 60 : attr.timeout()); // 451,���ݽ��׵�Ԥ����
			xml.append("</holdTm></Body></Envelope>");

			Executable exe = new Executable();
			exe.request = xml.toString().getBytes();
			exe.timeout = 1; // 451, Ĭ�ϳ�ʱʱ��Ϊ1��

			esbInfoService.getEndpoint(fcLocation).execute(exe);
			if (log.isDebugEnabled())
				log.debug("FC response: " + new String(exe.response, Common.CHARSET_UTF8));
			IMessage repmsg = SOAPConverter.getInstance().deserialize(exe.response);
			status = repmsg.getStatus();
		}
		catch (Throwable t)
		{
			log.warn("fail to apply FC:" + t);
			log.debug("fail to apply FC:", t);
		}
		if (status != null && AppRetCode.RES_ALLOCATE_APPLY_FAIL.equals(status.getRetCd()))
			throw new AppException(AppRetCode.RES_ALLOCATE_APPLY_FAIL);
		return true;
	}

	public void releaseFCRes(IMessage msg)
	{
		if (fcResourcePoolService == null || fcLocation == null) return;
		try
		{
			Status status = msg.getStatus();
			if (status != null && AppRetCode.RES_ALLOCATE_APPLY_FAIL.equals(status.getRetCd()))
			{
				log.info("FC fail response msg");
				return;
			}
			String key = fcKey.key(msg, msgDefService.getMessage(msg.getRefMsgCd()), priority(msg));
			if (key == null || !fcResourcePoolService.contain(key))
			{
				if (log.isDebugEnabled()) log.debug("release.key[" + key + "] not in DB");
				return;
			}
			log.info("release FC");
			Executable exe = new Executable();
			String sn = msg.getRefMsgSn();
			StringBuffer xml = new StringBuffer(
					"<Envelope><Header><msgCd>fcrelease</msgCd><sndAppCd>MB</sndAppCd><sndDt>");
			xml.append(msg.getSndDt());
			xml.append("</sndDt><seqNb>");
			xml.append(SpringUtil.JVM + "-" + msg.getSeqNb());
			xml.append("</seqNb></Header><Body><sn>");
			xml.append(sn);
			xml.append("</sn><withoutReturn>true</withoutReturn></Body></Envelope>");
			exe.request = xml.toString().getBytes();
			exe.withoutReturn = true;
			exe.timeout = 1; // 451, �޷��ز������ó�ʱʱ��
			esbInfoService.getEndpoint(fcLocation).execute(exe);
		}
		catch (Throwable t)
		{
			log.warn("fail to release FC:" + t);
			log.debug("fail to release FC:", t);
		}
	}

	public byte[] toBytes(byte[] xml, String ccsid) throws Exception
	{
		if (ESBCommon.CCSID_EBCD.equals(ccsid))
		{
			log.info("It is a EBCD msg!!!!");
			return EBCDUtil.bcd2gbk(xml, 0, xml.length, false).getBytes(Common.CHARSET_UTF8);
		}
		if (ESBCommon.CCSID_GBK.equals(ccsid))
		{
			log.info("It is a GBK msg!!!!");
			return new String(xml, Common.CHARSET_GBK).getBytes(Common.CHARSET_UTF8);
		}
		log.warn("Undefined ccsid:" + ccsid);
		return xml;
	}

	// ����׼utf8�ֽڱ�ΪĿǰ�ַ���
	public byte[] toBytes(byte[] xml, NodeAttr attr) throws Exception
	{
		if (attr.isEBCD())
		{
			log.info("utf8 to EBCD msg!!!");
			return EBCDUtil.gbk2bcd(new String(xml, Common.CHARSET_UTF8), false);
		}
		if (attr.isGBK())
		{
			log.info("utf8 to GBK msg!!!");
			return new String(xml, Common.CHARSET_UTF8).getBytes(Common.CHARSET_GBK);
		}
		log.warn("Undefined charset:" + attr.getCharset());
		return xml;
	}

	// ���Ա���ʱɾ��������Ϣ�ֶ�
	public String msg2strWithoutSensitive(IMessage msg)
	{
		if (!(Boolean) AppConfig.getInstance().getProperty(ESBConfig.JOURNAL_withoutSensitive,
				false))
		{
			log.debug("withoutSensitive is false!!!");
			return msg.toXml(true);
		}
		try
		{
			TreeNode reqSchema = msg.isRequestMsg() ? msgDefService.getMsgSchema(msg.getMsgCd())
					: msgDefService.getMsgSchema(msg.getRefMsgCd());
			TreeNode repSchema = msg.isRequestMsg() ? null
					: msgDefService.getMsgSchema(msg.getMsgCd());
			Message logmsg = new Message(new CompositeNode(msg.getTransaction())); // ��֤������־���޸�ԭmsg��������
			logmsg.setBody(new CompositeNode());
			ICompositeNode request = msg.getRequest();
			if (request != null && request.size() > 0)
				logmsg.setRequest(SchemaCompositeNode.getInstance(reqSchema, request));
			ICompositeNode response = msg.getResponse();
			if (response != null && response.size() > 0)
				logmsg.setResponse(SchemaCompositeNode.getInstance(repSchema, response));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			logmsg.toXml(baos, true, new Array2Node2XML()
			{
				protected Object atom2obj(IAtomNode anode, String ns, String tag, boolean pretty,
						ICompositeNode root, ICompositeNode parent, List path, Map attribute,
						int index)
				{
					if (parent instanceof SchemaCompositeNode)
					{ // �����������schema�ڵ�
						SchemaCompositeNode scnode = (SchemaCompositeNode) parent;
						TreeNode tn = scnode.getSchema(tag);
						if (tn != null)
						{
							MsgSchemaPO schema = (MsgSchemaPO) tn.getTreeNodeValue();
							TagAttr attr = new TagAttr(schema.getTagAttr());
							String str = anode.stringValue();
							if (attr.isSensitive())
							{
								if (log.isDebugEnabled())
									log.debug("it's a sensitive tag(" + tag + ")!!!");
								return "@@" + str.length() + "@@";
							}
						}
					}
					return anode.getValue();
				}
			});
			return new String(baos.toByteArray(), Common.CHARSET_UTF8);
		}
		catch (Exception e)
		{
			log.warn("sensitive:", e);
			return msg.toXml(true);
		}
	}

	// ���ǰ��ֱ�����Ͷ����Ʊ��ġ���Ҫͨ��MQMD��ApplIdentityData�ж����
	public byte[] toESBXML(byte[] originalBytes, NodePO node, Map attr) throws Exception
	{
		byte[] msgId = (byte[]) attr.get("MsgId");
		String userId = (String) attr.get("UserId");
		ByteArrayOutputStream baos = new ByteArrayOutputStream(originalBytes.length + 1024);
		// baos.write(IMessage.XML_HEADER);
		baos.write("<Envelope><Header>".getBytes());
		if (!StringX.nullity(StringX.null2emptystr(node.getMbrCd()).trim()))
		{
			baos.write("<sndMbrCd>".getBytes());
			baos.write(node.getMbrCd().trim().getBytes());
			baos.write("</sndMbrCd>".getBytes());
		}
		baos.write("<sndAppCd>".getBytes());
		baos.write(node.getAppCd().getBytes());
		baos.write("</sndAppCd><seqNb>".getBytes());
		baos.write(msgId); // ǰ�˱�������asc���ʽ���ַ���
		baos.write("</seqNb></Header><originalBytes>".getBytes());
		baos.write(StringX.encodeBase64(originalBytes));
		baos.write("</originalBytes></Envelope>".getBytes());
		byte[] xml = baos.toByteArray();
		if (log.isDebugEnabled()) log.debug("xml:" + new String(xml));
		return xml;
	}

	public void handleUnvalidXMLChar(IMessage msg)
	{
		if (Common.YES
				.equals(StringX.null2emptystr(msg.getInLocal(ESBMsgLocalKey.LOCAL_UNVALID_XML))))
		{ // �����ǰ����XML���Ϸ�(������XMLƬ�ε������ܷ���)
			throw new AppException(ESBRetCode.MSG_UNVALIDCHAR,
					new String[] { msg.getMsgCd(), msg.getSndNodeApp() });
		}

		// ���û�зǷ�xml char
		if (!Common.YES.equals(
				StringX.null2emptystr(msg.getInLocal(ESBMsgLocalKey.LOCAL_UNVALID_XML_CHAR))))
			return;

		boolean ignoreUnvalidXMLChar = (Boolean) AppConfig.getInstance()
				.getProperty(ESBConfig.MB_ignoreUnvalidXMLChar, false);
		if (ignoreUnvalidXMLChar)
		{
			if (log.isInfoEnabled()) log.info("ignore unvalid xml char!!!");
			return;
		}
		if (!msg.isRequestMsg())
		{ // 2012-08-15 Ӧ���Ĳ�ʹ���쳣��ֻ���þ�����־��ʽ��¼
			log.warn("response msg contains unvalid xml char!!!");
			return;
		}
		// 2012-08-05 chenjs �����ǰ���ĺ��зǷ��ַ�����ƽ̨�������зǷ��ַ���ʹ���쳣
		throw new AppException(ESBRetCode.MSG_UNVALIDCHAR,
				new String[] { msg.getMsgCd(), msg.getSndNodeApp() });
	}

	@Autowired(required = false)
	protected MsgDefService msgDefService;
	@Autowired(required = false)
	protected ESBInfoService esbInfoService;
	@Autowired(required = false)
	protected MessageTranslator translator;
	@Autowired(required = false)
	protected ResourcePoolService fcResourcePoolService; // ����������Դ��
	protected String fcLocation = "tcp.flowctrl";
	@Autowired(required = false)
	protected FCKey fcKey = new DefaultFCKey();

	public void setTranslator(MessageTranslator translator)
	{
		this.translator = translator;
	}

	public void setMsgDefService(MsgDefService msgDefService)
	{
		this.msgDefService = msgDefService;
	}

	public void setEsbInfoService(ESBInfoService esbInfoService)
	{
		this.esbInfoService = esbInfoService;
	}

	public void setFcResourcePoolService(ResourcePoolService fcResourcePoolService)
	{
		this.fcResourcePoolService = fcResourcePoolService;
	}

	public void setFcLocation(String fcLocation)
	{
		this.fcLocation = fcLocation;
	}

	public boolean isValidateHeader(IMessage msg)
	{
		return (Boolean) AppConfig.getInstance().getProperty(ESBConfig.MB_validateHeader, true);
	}

	public boolean isValidateBody(IMessage msg)
	{
		NodeServicePO nodeServiceVO = msg.isRequestMsg()
				? esbInfoService.getNodeService(msg.getSndNodeApp(), msg.getMsgCd())
				: esbInfoService.getNodeService(msg.getRefSndNodeApp(), msg.getRefMsgCd());
		if (nodeServiceVO != null)
			return new NodeServiceAttr(nodeServiceVO.getAttr()).isValidateBody();
		return (Boolean) AppConfig.getInstance().getProperty(ESBConfig.MB_validateBody, true);
	}

	public boolean isAutoFilterUndefinedTag(IMessage msg)
	{
		NodeServicePO nodeServiceVO = msg.isRequestMsg()
				? esbInfoService.getNodeService(msg.getSndNodeApp(), msg.getMsgCd())
				: esbInfoService.getNodeService(msg.getRefSndNodeApp(), msg.getRefMsgCd());
		if (nodeServiceVO != null)
			return new NodeServiceAttr(nodeServiceVO.getAttr()).isAutoFilterUndefinedTag();
		return (Boolean) AppConfig.getInstance().getProperty(ESBConfig.MB_autoFilterUndefinedTag,
				false);
	}

	public void setFcKey(FCKey fcKey)
	{
		this.fcKey = fcKey;
	}

	public int getTimeout(IMessage msg)
	{
		try
		{
			return msgDefService.getMsgAttr(msg.getMsgCd()).getTimeout();
		}
		catch (Exception e)
		{
			if (log.isInfoEnabled()) log.info(
					"msgDefService.getMsgAttr(" + msg.getMsgCd() + "), use default timeout 60s!",
					e);
			return 60;
		}
	}
}
