package spc.esb.common.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import spc.esb.common.service.AuthService;
import spc.esb.common.service.ESBInfoService;
import spc.esb.common.service.MsgDefService;
import spc.esb.constant.ESBConfig;
import spc.esb.constant.ESBMsgLocalKey;
import spc.esb.constant.ESBRetCode;
import spc.esb.core.NodeAttr;
import spc.esb.core.NodeServiceAttr;
import spc.esb.data.IMessage;
import spc.esb.data.Message;
import spc.esb.data.util.MsgFTLUtil;
import spc.esb.model.NodePO;
import spc.esb.model.NodeServicePO;
import spc.esb.model.ServicePO;
import spc.webos.config.AppConfig;
import spc.webos.exception.AppException;
import spc.webos.service.BaseService;
import spc.webos.util.FTLUtil;
import spc.webos.util.JsonUtil;
import spc.webos.util.RegExp;
import spc.webos.util.StringX;;

/**
 * ������Ȩ����
 * 
 * @author sunqian at 2010-05-24
 */
@Service("esbAuthService")
public class AuthServiceImpl extends BaseService implements AuthService
{
	protected List<String> defValidServiceStatus = (List<String>) JsonUtil.json2obj("['2','5']");
	@Autowired(required = false)
	protected ESBInfoService esbInfoService;
	@Autowired(required = false)
	protected MsgDefService msgDefService;

	// ������ͨ���ĵ���Ȩ״̬
	public boolean isAuth(IMessage msg) throws Exception
	{
		if (!checkAuth(msg)) throw new AppException(ESBRetCode.SERVICE_UNAUTH,
				new String[] { msg.getMsgCd(), msg.getSndNodeApp() });
		checkChannel(msg); // �������/����ϵͳ�Ƿ�Ϸ�״̬��ָ��ʱ�䴰��
		checkService(msg); // �������Ƿ�Ϸ�״̬��ָ��ʱ�䴰��
		return true;
	}

	// �ж�����ʱ�䴰�ں�״̬
	protected boolean checkChannel(IMessage msg)
	{
		// 1. ��鷢��������״̬.
		NodePO sndNodeVO = esbInfoService.getNode(msg.getSndNodeApp());
		NodeAttr sndNodeattr = new NodeAttr(sndNodeVO.getAppAttr());
		if (sndNodeattr.isUnvalidChannel())
			throw new AppException(ESBRetCode.SERVICE_UNAUTH, "channel is unvalid!!!");

		// 2. ��鷢��������ʱ�䴰��
		String currentDt = new SimpleDateFormat("yyyyMMdd").format(new Date());
		if ((!StringX.nullity(sndNodeVO.getPublishDt())
				&& currentDt.compareTo(sndNodeVO.getPublishDt()) < 0)
				|| (!StringX.nullity(sndNodeVO.getEffectDt())
						&& currentDt.compareTo(sndNodeVO.getEffectDt()) > 0))
		{
			log.warn("currentDt: " + currentDt + ", publishDt:" + sndNodeVO.getPublishDt()
					+ ", effectDt" + sndNodeVO.getEffectDt());
			throw new AppException(ESBRetCode.SERVICE_UNAUTH, "channel not in valid date!!!");
		}

		// 3. ������ϵͳ״̬
		NodePO rcvNodeVO = esbInfoService.getNode(msg.getRcvNodeApp());
		// 503_20150214, �㲥���׼����Ȩʱ��û����д���ĵ�rcv��Ϣ�����������鲻��rcv��Ϣ��ͨ��
		if (rcvNodeVO == null)
		{
			log.debug("No RcvNode:" + msg.getRcvNodeApp());
			return true;
		}
		// if (rcvNodeVO == null) throw new
		// AppException(ESBRetCode.FLOW_UNAUTH(),
		// "RcvNode inexistence(" + msg.getRcvNodeApp() + ")");
		NodeAttr rcvNodeattr = new NodeAttr(rcvNodeVO.getAppAttr());
		if (rcvNodeattr.isUnvalidServer())
			throw new AppException(ESBRetCode.SERVICE_UNAUTH, "server is unvalid!!!");

		return true;
	}

	// �жϷ���ʱ�䴰�ں�״̬
	protected boolean checkService(IMessage msg) throws Exception
	{
		String msgCd = msg.getMsgCd();
		ServicePO serviceVO = msgDefService.getService(msgCd);
		if (serviceVO == null)
		{
			log.warn("NO Service by " + msgCd);
			return true;
		}

		// 1. �жϷ���״̬�Ƿ���� T���������״̬��O����������״̬
		// ��ȡesb_config������, Ĭ��Ϊ2��5��Ч
		ArrayList validServiceStatusList = (ArrayList) AppConfig.getInstance()
				.getProperty(ESBConfig.MB_validServiceStatus, defValidServiceStatus);
		if (!StringX.nullity(serviceVO.getStatus()) && !StringX.contain(
				(String[]) validServiceStatusList
						.toArray(new String[validServiceStatusList.size()]),
				serviceVO.getStatus(), true))
		{
			log.warn("service status:" + serviceVO.getStatus() + " not in "
					+ validServiceStatusList);
			throw new AppException(ESBRetCode.SERVICE_UNAUTH, "status of service is unvalid!!!");
		}

		// 2. �жϷ���ʱ�䴰�ڣ����ûָ��ʱ�䴰������Ϊ������
		String currentDt = new SimpleDateFormat("yyyyMMdd").format(new Date());
		if ((!StringX.nullity(serviceVO.getPublishDt())
				&& currentDt.compareTo(serviceVO.getPublishDt()) < 0)
				|| (!StringX.nullity(serviceVO.getEffectDt())
						&& currentDt.compareTo(serviceVO.getEffectDt()) > 0))
		{
			log.warn("currentDt: " + currentDt + ", publishDt:" + serviceVO.getPublishDt()
					+ ", effectDt" + serviceVO.getEffectDt());
			throw new AppException(ESBRetCode.SERVICE_UNAUTH, "service not in valid date!!!");
		}

		// 3. ������������˹���У����ʹ�ù����ж�
		if (!StringX.nullity(serviceVO.getValidRule()))
		{
			Map root = new HashMap();
			Message hmsg = new Message();
			hmsg.setHeader(msg.getHeader()); // ֻ���ݱ���ͷ�ж�
			MsgFTLUtil.model(root, hmsg);
			String errMsg = StringX.trim(FTLUtil.freemarker(serviceVO.getValidRule(), root));
			if (!StringX.nullity(errMsg)) throw new AppException(ESBRetCode.SERVICE_UNAUTH, errMsg);
		}

		// 4. �������Ƿ񳬹��涨������ĳ���
		String msgLen = StringX.null2emptystr(msg.getInLocal(ESBMsgLocalKey.MSG_LENGTH));
		if (!StringX.nullity(msgLen) && serviceVO.getMaxLen() != null && serviceVO.getMaxLen() >= 0
				&& (Integer.parseInt(msgLen) > serviceVO.getMaxLen()))
		{
			log.warn("msgLen: " + msgLen + ", maxLen:" + serviceVO.getMaxLen());
			throw new AppException(ESBRetCode.SERVICE_UNAUTH, "msg is too long!!!");
		}
		return true;
	}

	// �жϷ�����Ȩ״̬
	protected boolean checkAuth(IMessage msg)
	{
		String mbrCd = StringX.null2emptystr(msg.getSndNode()).trim();
		String appCd = msg.getSndApp();
		String msgCd = msg.getMsgCd();

		// 40 2013-05-09 chenjs ������Node������������ʽ��Ȩ
		NodePO node = esbInfoService.getNode(mbrCd + appCd);
		if (node != null && !StringX.nullity(node.getAuthMsgCd())
				&& ("*".equals(node.getAuthMsgCd())
						|| RegExp.match(msgCd, Pattern.compile(node.getAuthMsgCd()))))
			return true;

		// 2. 400�汾���������esb_nodeservice, ������ʹ�ô�������Ϣ
		NodeServicePO nodeServiceVO = esbInfoService.getNodeService(mbrCd + appCd, msgCd);
		if (nodeServiceVO != null) return new NodeServiceAttr(nodeServiceVO.getAttr()).isAuth();
		return false;
	}

	public void setEsbInfoService(ESBInfoService esbInfoService)
	{
		this.esbInfoService = esbInfoService;
	}

	public void setMsgDefService(MsgDefService msgDefService)
	{
		this.msgDefService = msgDefService;
	}

	public void setDefValidServiceStatus(String[] defValidServiceStatus)
	{
		this.defValidServiceStatus = new ArrayList<>();
		for (String s : defValidServiceStatus)
			this.defValidServiceStatus.add(s);
	}
}
