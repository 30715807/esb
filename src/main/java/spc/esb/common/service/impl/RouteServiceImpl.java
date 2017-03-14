package spc.esb.common.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import spc.esb.common.service.ESBInfoService;
import spc.esb.common.service.MsgDefService;
import spc.esb.common.service.MsgRoute;
import spc.esb.common.service.Route;
import spc.esb.common.service.RouteService;
import spc.esb.constant.ESBCommon;
import spc.esb.constant.ESBConfig;
import spc.esb.constant.ESBMsgCode;
import spc.esb.data.IMessage;
import spc.esb.data.MessageAttr;
import spc.esb.data.util.MsgFTLUtil;
import spc.esb.model.MessagePO;
import spc.esb.model.NodePO;
import spc.esb.model.NodeServicePO;
import spc.webos.config.AppConfig;
import spc.webos.service.BaseService;
import spc.webos.util.FTLUtil;
import spc.webos.util.StringX;

/**
 * 2011-09-29 ȡ���㲥���ױ���������ã�ʹ��ͬһ��ESB_MSG or ESB_SERVICE���qname & routerule����
 * 
 * @author
 * 
 */
@Service("esbRouteService")
public class RouteServiceImpl extends BaseService implements RouteService
{
	// 400 2012-05-09 �ϳ� specialRoute��ʹ��
	@Autowired(required = false)
	protected MsgDefService msgDefService;
	@Autowired(required = false)
	protected ESBInfoService esbInfoService;
	protected String reqQueueFix = ESBCommon.REQFIX; // ���������ǰ׺REQ.
	protected String repQueueFix = ESBCommon.REPFIX; // Ӧ�������ǰ׺REP.
	protected String broadcastQueueFix = ESBCommon.REQFIX; // chenjs 2013-04-18
															// ����㲥�����Բ�ͬ����ǰ׺
	protected String mbrCdCfgKey; // ��esb_config�������õ��к�ӳ��mdlֵ���Ƽ�ΪmbrCd

	// ���ݳ�Ա��źͱ��ı�Ż�ȡĿ�Ķ��У�ESB_MBRDISTRIB���������õĻ���ȡ���е�QNAMEֵ��û�е�ʹ��ESB_MSG���е�QNAMEֵ
	public String getQname(IMessage msg) throws Exception
	{
		if (!msg.isRequestMsg()) return getRepQname(msg);
		String rcvMbrCd = mappingMbrCd(msg.getRcvNode()); // ����ж��м���ӳ��
		String rcvAppCd = msg.getRcvApp();
		String msgCd = msg.getMsgCd();
		MessageAttr msgAttr = msgDefService.getMsgAttr(msgCd);

		// ������ͨģʽ·�ɣ����ݾ�̬���߶�̬����
		String qname = route(getRoute(msg), msg);
		// modified by chenjs 2011-10-02 ���·����ϢΪ�գ����ʾ��rcvappcd��Ϊ·�ɶ���
		// ncc���Ĺ淶�ǲ����������壬���Ƿ���ָ������Ϊͬһ�����ı�ſ���·�ɸ���ͬ��
		if (StringX.nullity(qname))
		{
			if (log.isInfoEnabled()) log.info(
					"qname is null, route by rcvAppCd: " + rcvAppCd + ", rcvMbrCd: " + rcvMbrCd);
			return genReqQname(rcvAppCd, rcvMbrCd, rcvAppCd);
		}

		// �ǹ㲥���ױ���, ������ճ�Ա��Ϊ�����Զ������ճ�Ա���
		if (!msgAttr.isBroadcast()) return genReqQname(qname, rcvMbrCd, rcvAppCd);

		// �㲥���ױ���ʹ��qname or routerule���, �����Զ�������ճ�Ա
		if (StringX.nullity(broadcastQueueFix)) return qname; // û�й̶�ǰ׺��ֱ�ӷ���,
		// 2011-09-30 chenjs
		StringBuffer broadCastQ = new StringBuffer();
		List broadCastQs = StringX.split2list(qname, StringX.COMMA);
		for (int j = 0; j < broadCastQs.size(); j++)
		{
			if (broadCastQ.length() > 0) broadCastQ.append(StringX.COMMA);
			String q = broadCastQs.get(j).toString(); // �������·���а�����ǰ׺��������ǰ׺
			broadCastQ.append(q.startsWith(broadcastQueueFix) ? q : broadcastQueueFix + q);
		}
		return broadCastQ.toString();
	}

	/**
	 * ����·�ɹ��򣬱��ģ����ܳ�Ա��������·��
	 * 
	 * @param route
	 * @param msg
	 * @param mbrCd
	 * @return
	 * @throws Exception
	 */
	protected String route(Route route, IMessage msg) throws Exception
	{
		if (!StringX.nullity(route.getRouteBeanName()))
		{ // adde chenjs 2011-12-20 ���qname & ftl���޷�������ʹ��ע��java�ӿڷ�ʽ
			if (log.isDebugEnabled()) log.debug("routeBeanName: " + route.getRouteBeanName());
			MsgRoute msgRoute = (MsgRoute) MsgRoute.ROUTES.get(route.getRouteBeanName());
			if (msgRoute != null) return msgRoute.route(route, msg);
			log.warn("IMsgRoute is null by: " + route.getRouteBeanName());
		}

		if (!StringX.nullity(route.getFtlRule()))
		{ // ��̬���ݱ������ݽ���·��
			Map root = new HashMap();
			root.put("route", route);
			MsgFTLUtil.model(root, msg);
			String qname = StringX.trim(FTLUtil.freemarker(route.getFtlRule(), root));
			if (!StringX.nullity(qname))
			{
				if (log.isInfoEnabled()) log.info("route by rule, des. queue: " + qname);
				return qname;
			}
		}
		return route.getQname();
	}

	protected Route getRoute(IMessage msg)
	{
		String node = msg.getSndNode();
		String msgCd = msg.getMsgCd();

		// 1. �����������ϵ������ѡ��·��, 400 2013-05-09
		NodeServicePO nodeServiceVO = esbInfoService.getNodeService(node, msgCd);
		if (nodeServiceVO != null && nodeServiceVO.isValidRoute()) return nodeServiceVO;

		// 2. ��service��·�ɹ������·�ɣ����û���� ���ٰ�msg��·�ɹ������
		return msgDefService.getService(msgCd);
	}

	/**
	 * Ӧ����·��Ӧ��
	 * 
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	public String getRepQname(IMessage msg) throws Exception
	{
		// 401_20130925 chenjs �����첽Ӧ��������������û������ʹ��replyToQ���ԣ���ʹ�ô�ͳ��rep.nbs.asyn
		// 404_20140525 chenjs ���replyToQǰ����д��������ΪreplyToQ������Чû��
		String replyToQ = msg.getReplyToQ();
		if (!isValidReplyToQ(msg, replyToQ))
		{
			log.warn("replyToQ: " + replyToQ + " is unvalid!!!");
			replyToQ = null;
		}
		if (!StringX.nullity(replyToQ)) return replyToQ;

		boolean syn = true; // Ĭ��ͬ��Ӧ��
		String callType = msg.getCallType(); // 400 2013-06-01 ʹ��callType�ֶ�
		if (IMessage.CALLTYP_SYN.equalsIgnoreCase(callType)
				|| ESBMsgCode.MSGCD_COMM_RECEIPT().equals(msg.getMsgCd()))
			syn = true;
		// modified by guodd 20110622
		else if (IMessage.CALLTYP_ASYN.equalsIgnoreCase(callType)) syn = false;
		else
		{ // ������ط�û����д�ο����÷�ʽ������д��SYN��ASYN����ԭ���ĵ�Ĭ�ϵ�������
			String refMsgCd = msg.getRefMsgCd();
			MessagePO msgVO = msgDefService.getMessage(refMsgCd);
			if (msgVO == null) log.warn("cannot find MessageVO by " + refMsgCd);
			else syn = !new MessageAttr(msgVO.getMsgAttr()).isAsyn();
			if (log.isInfoEnabled()) log.info("callType is not SYN/ASYN, using refmsgcd attr: "
					+ syn + ", refMsgCd:" + refMsgCd);
		}

		if (!syn)
		{ // 401_20131022 chenjs �첽Ӧ���������ָ������������ʹ��
			NodeServicePO nodeServiceVO = esbInfoService.getNodeService(msg.getRefSndNodeApp(),
					msg.getRefMsgCd());
			if (nodeServiceVO != null && !StringX.nullity(nodeServiceVO.getAsynRepQName()))
				return nodeServiceVO.getAsynRepQName();
		}

		String refSndNode = mappingMbrCd(msg.getRefSndNode());
		String refSndAppCd = msg.getRefSndApp();

		return repQueueFix + refSndAppCd
				+ (StringX.nullity(refSndNode) ? StringX.EMPTY_STRING
						: ESBCommon.QSPLIT + refSndNode)
				+ (syn ? StringX.EMPTY_STRING : ESBCommon.QSPLIT + IMessage.CALLTYP_ASYN);
	}

	// �ж��Ƿ��ǷǷ���replyToQ����
	public boolean isValidReplyToQ(IMessage msg, String replyToQ)
	{
		if (StringX.nullity(replyToQ)) return true;

		String replyToQPrefix = (String) AppConfig.getInstance()
				.getProperty(ESBConfig.MB_replyToQPrefix, "REP.");
		if (log.isDebugEnabled())
			log.debug("replyToQPrefix:" + replyToQPrefix + ", replyToQ:" + replyToQ);
		if (replyToQ.startsWith(replyToQPrefix)) return true;

		NodePO node = esbInfoService
				.getNode(msg.isRequestMsg() ? msg.getSndNodeApp() : msg.getRefSndNodeApp());
		if (node != null && !StringX.nullity(node.getReplyToQ()))
		{ // ����ָ����Ӧ�𷵻ض��е���Ч����
			return StringX.contain(StringX.split(node.getReplyToQ(), StringX.COMMA), replyToQ,
					true);
		}
		return false;
	}

	// added by chenjs 2011-08-20, ����12�кſ���Ҫӳ��Ϊ4λ���������·�ɶ��м���
	protected String mappingMbrCd(String mbrCd)
	{
		if (StringX.nullity(mbrCdCfgKey) || StringX.nullity(mbrCd)) return mbrCd;
		return (String) AppConfig.getInstance().getProperty(mbrCdCfgKey + '.' + mbrCd, mbrCd);
	}

	protected String genReqQname(String qname, String rcvMbrCd, String rcvAppCd)
	{
		// modified by chenjs 2011-08-20.
		// ���ǵ�rcvMbrCd����ʹ�������кţ�������һ�������кŵ�ҵ����ͬһ�����С�
		// Note: ��ǰ·�ɶ���ΪREQ.2400.CBS, ���ڱ��ΪREQ.CBS.2400 or CBS.2400, ����Ա��Ϣ����
		return reqQueueFix
				+ (StringX.nullity(rcvMbrCd) ? qname : qname + ESBCommon.QSPLIT + rcvMbrCd);
	}

	public void setEsbInfoService(ESBInfoService esbInfoService)
	{
		this.esbInfoService = esbInfoService;
	}

	public void setMsgDefService(MsgDefService msgDefService)
	{
		this.msgDefService = msgDefService;
	}

	public void setReqQueueFix(String reqQueueFix)
	{
		this.reqQueueFix = reqQueueFix;
	}

	public void setRepQueueFix(String repQueueFix)
	{
		this.repQueueFix = repQueueFix;
	}

	public void setBroadcastQueueFix(String broadcastQueueFix)
	{
		this.broadcastQueueFix = broadcastQueueFix;
	}
}
