package spc.esb.core.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;

import spc.esb.common.service.AuthService;
import spc.esb.common.service.ESBInfoService;
import spc.esb.common.service.JournalService;
import spc.esb.common.service.MsgDefService;
import spc.esb.common.service.SignatureService;
import spc.esb.constant.ESBCommon;
import spc.esb.constant.ESBMsgCode;
import spc.esb.constant.ESBMsgLocalKey;
import spc.esb.core.NodeAttr;
import spc.esb.core.service.CoreService;
import spc.esb.core.service.ESBService;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.Message;
import spc.esb.data.converter.CoreMessageConverter;
import spc.esb.data.converter.MessageConverter;
import spc.esb.data.converter.NodeConverterFactory;
import spc.esb.data.converter.SOAPConverter;
import spc.esb.data.util.MessageUtil;
import spc.esb.model.MessagePO;
import spc.esb.model.NodePO;
import spc.webos.advice.log.LogTrace;
import spc.webos.constant.AppRetCode;
import spc.webos.endpoint.Endpoint;
import spc.webos.endpoint.EndpointFactory;
import spc.webos.endpoint.Executable;
import spc.webos.exception.AppException;
import spc.webos.exception.Status;
import spc.webos.service.BaseService;
import spc.webos.service.seq.UUID;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

public class ESBServiceImpl extends BaseService implements ESBService
{
	@Override
	@LogTrace
	public IMessage sync(IMessage msg)
	{
		String dt = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date());
		if (StringX.nullity(msg.getSeqNb())) msg.setSeqNb(String.valueOf(uuid.uuid()));
		if (StringX.nullity(msg.getSndDt())) msg.setSndDt(dt.substring(0, 8));
		if (StringX.nullity(msg.getSndTm())) msg.setSndTm(dt.substring(8, 17));

		log.info("sync sn:{}, msgCd:{}", msg.getMsgSn(), msg.getMsgCd());
		try
		{
			byte[] buf = request(msg);
			call(msg, buf);
			response(msg);
		}
		catch (Exception e)
		{
			log.info("fail to sync:" + msg.getMsgSn(), e);
			msg.setStatus(SpringUtil.ex2status("", e));
			if (req2rep(msg))
			{ // gen snd info
				genSndInf(msg);
				msg.setSndAppCd("ESB");
				msg.setMsgCd("ESB.00000001.01");
			}
			msg.setBody(null); // ���body����
		}
		Status status = msg.getStatus(); // ����������������;�����־
		if (status != null && !status.success()) journalService.sendAlarm(msg);
		journalService.sendLog(msg, ESBCommon.REP_OUT_POINT);
		log.info("sync over sn:{}, retcd:{}", msg.getMsgSn(),
				status == null ? "" : status.getRetCd());
		return msg;
	}

	protected Executable call(IMessage msg, byte[] buf) throws Exception
	{
		String sn = msg.getMsgSn();
		String location = msgDefService.getLocation(msg);
		log.info("call endpoint:{}, len:{}", location, (buf == null ? 0 : buf.length));
		// �п���endpoint�ǳ�����
		try (Endpoint endpoint = location.indexOf(':') > 0
				? EndpointFactory.getInstance().getEndpoint(location)
				: esbInfoService.getEndpoint(location))
		{
			Executable exe = new Executable(sn, buf);
			exe.reqmsg = msg;
			endpoint.execute(exe);
			msg.setOriginalBytes(exe.response);
			msg.setInLocal(ESBMsgLocalKey.LOCAL_EXECUTABLE, exe);
			if (req2rep(msg))
			{ // ��������Ƕ������ģ�������ǰ���Ļ���һ����������Ҫת��ΪӦ����
				String rcvAppCd = msg.getRcvApp();
				if (!StringX.nullity(rcvAppCd)) msg.setSndAppCd(rcvAppCd);
				genSndInf(msg);
			}
			if (StringX.nullity(msg.getSeqNb())) msg.setSeqNb(String.valueOf(uuid.uuid()));
			return exe;
		}
	}

	@Override
	@LogTrace
	public byte[] request(final IMessage msg) throws Exception
	{
		log.info("request:{}", msg.getMsgSn());
		log.debug("request start:{}", msg);
		byte[] original = msg.getOriginalBytes();
		CoreMessageConverter cmc = coreService.getCoreMsgConverter(msg, true, false);
		if (cmc != null) cmc.app2esb(msg, true);
		// 0:��־��
		journalService.sendLog(msg, ESBCommon.REQ_IN_POINT);
		msg.setOriginalBytes(null);
		MessagePO msgPO = msgDefService.getMessage(msg.getMsgCd());
		if (msgPO != null) msg.setRcvAppCd(msgPO.getRcvAppCd());
		authService.isAuth(msg);
		coreService.validateHdr(msg);
		coreService.validateBody(msg);
		coreService.translator(msg);
		msg.setSignature(null);

		log.debug("request end:{}", msg);
		cmc = coreService.getCoreMsgConverter(msg, true, true);
		byte[] buf = null;
		if (cmc != null) buf = cmc.esb2app(msg, true);
		msg.setOriginalBytes(buf);
		journalService.sendLog(msg, ESBCommon.REQ_OUT_POINT);
		return buf;
	}

	@Override
	@LogTrace
	public byte[] response(IMessage msg) throws Exception
	{
		log.info("response:{}", msg.getRefMsgSn());
		byte[] original = msg.getOriginalBytes();
		String signature = msg.getSignature();

		CoreMessageConverter cmc = coreService.getCoreMsgConverter(msg, false, true);
		if (cmc != null) cmc.app2esb(msg, false);
		else if (msg.isRequestMsg())
		{ // �����������������
			msg.setTransaction(converter.deserialize(original).getTransaction());
			msg.setOriginalBytes(original);
		}
		log.debug("response start:{}", msg);
		journalService.sendLog(msg, ESBCommon.REP_IN_POINT);
		msg.setOriginalBytes(null);
		msg.setSignature(null);
		msg.setRcvAppCd(null);

		coreService.validateHdr(msg);
		coreService.validateBody(msg);
		coreService.translator(msg);
		log.debug("response end:{}", msg);

		cmc = coreService.getCoreMsgConverter(msg, false, false);
		byte[] buf = null;
		if (cmc != null) buf = cmc.esb2app(msg, false);
		msg.setOriginalBytes(buf);
		// journalService.sendLog(msg, ESBCommon.REP_OUT_POINT);
		return buf;
	}

	@LogTrace
	public <T> T call(String msgCd, Object request, T response)
	{
		log.info("call:{}", msgCd);
		Message msg = new Message();
		genSndInf(msg);
		msg.setSndAppCd("DUBBO");
		msg.setMsgCd(msgCd);
		msg.setBody((ICompositeNode) NodeConverterFactory.getInstance().unpack(request, null));
		IMessage rep = sync(msg);
		Status s = rep.getStatus();
		if (s.fail()) throw new AppException(s);
		rep.getBody().toObject(response);
		return response;
	}

	protected boolean req2rep(IMessage msg)
	{
		if (!msg.isRequestMsg()) return false;

		if (StringX.nullity(msg.getRefMsgCd())) msg.setRefMsgCd(msg.getMsgCd());
		if (StringX.nullity(msg.getRefSndNode()))
			msg.setRefSndNode(StringX.nullity(msg.getSndNode()) ? null : msg.getSndNode());
		if (StringX.nullity(msg.getRefSndApp())) msg.setRefSndApp(msg.getSndApp());
		if (StringX.nullity(msg.getRefSndDt())) msg.setRefSndDt(msg.getSndDt());
		if (StringX.nullity(msg.getRefSeqNb())) msg.setRefSeqNb(msg.getSeqNb());
		return true;
	}

	protected void genSndInf(IMessage msg)
	{
		String dt = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date());
		msg.setSeqNb(String.valueOf(uuid.uuid()));
		msg.setSndDt(dt.substring(0, 8));
		msg.setSndTm(dt.substring(8, 17));
	}

	protected void unsig(IMessage msg, NodePO nodeVO, String signature, byte[] original)
			throws Exception
	{
		NodeAttr nodeAttr = new NodeAttr(nodeVO.getAppAttr());
		if (nodeAttr.isNotUnsig() || (!nodeAttr.isBodySig() && !nodeAttr.isElementSig()))
		{ // added by chenjs 2011-06-02 ����ڵ�sigmode������0/1���ʾ�˽ڵ㲻����ǩ��
			log.info("sig mode is not in (0, 1) or is not usig");
			return;
		}

		byte[] body = null; // ����ǻ���body��ǩ���ݣ����ȡԭbody��ǩ������Ϣ
		if (nodeAttr.isBodySig())
		{ // 2012-06-12 chenjs SigPre�з������ȫ����
			String xmlbase64 = null;
			if (!StringX.nullity(xmlbase64))
				body = MessageUtil.getBody(StringX.decodeBase64(xmlbase64.getBytes()));
			else log.warn("xml is null!!!");
		}
		boolean unsigOK = signatureService.unsig(msg, nodeVO.getAppCd(), body, signature);
		if (!unsigOK)
		{ // ��֤ǩ��ʧ��
			log.warn("unsig: false, node:{}, sig:{}, original.base64:{}:", nodeVO, signature,
					StringX.base64(original));
			AppException ae = new AppException(AppRetCode.SIG_DECODE,
					new String[] { StringX.null2emptystr(msg.getSndApp()) });
			if (!msg.isRequestMsg())
			{
				msg.setMsgCd(ESBMsgCode.MSGCD_REQERR());
				Status status = SpringUtil.ex2status(msg.getFixedErrDesc(), ae);
				msg.setStatus(status);
			}
			else throw ae;
		}
	}

	protected String sig(IMessage msg, NodePO nodeVO, String signature, byte[] original)
			throws Exception
	{
		NodeAttr nodeAttr = new NodeAttr(nodeVO.getAppAttr());
		if (nodeAttr.isNotSig())
		{ // ��ǰ���սڵ㲻��Ҫ��ǩ
			log.info("not sig!!!");
			return null;
		}
		else if (nodeAttr.isBodySig())
		{ // �Ա�����bodyǩ��
			log.debug("sig by body...");
			// ��ȡ���뱨�ĵ�byte[]
			byte[] msgBytes = null;
			byte[] body = MessageUtil.getBody(msgBytes);
			String sigStr = signatureService.sig(msg, nodeVO.getAppCd(), body);
			// modified by chenjs 2011-05-31 �ڲ���Ҫ��ȡǩ����Ϣʱ��ֱ����ԭ���������������������
			if (!StringX.nullity(sigStr))
			{
				if (log.isInfoEnabled()) log.info("sig by body, sig:[" + sigStr + "]");
				byte[] afterSigXML = MessageUtil.addSignature2(msgBytes, sigStr.getBytes());
				if (log.isDebugEnabled()) log.debug(
						"afterSigXML.base64:" + new String(StringX.decodeBase64(afterSigXML)));
			}
			else
			{
				log.info("sig by body, sigStr is empty!!!");
			}
		}
		else if (nodeAttr.isElementSig())
		{ // ������ǩ��
			log.debug("sig by the element...");
			String sigStr = signatureService.sig(msg, nodeVO.getAppCd(), null);
			// modified by chenjs 2011-05-31 �ڲ���Ҫ��ȡǩ����Ϣʱ��ֱ����ԭ���������������������
			if (!StringX.nullity(sigStr))
			{
				if (log.isInfoEnabled()) log.info("sig by ele, sig:[" + sigStr + "]");
				msg.setSignature(sigStr);
			}
			else
			{
				log.info("sig by ele, sigStr is empty!!!");
			}
		}
		else
		{ // added by chenjs 2011-06-02 ����ڵ�����ǩ��ģʽ��Ϊ0/1���ʾ��ϵͳ��֧��ǩ��
			log.info("sig mode is not in (0, 1)");
		}
		return null;
	}

	@Resource
	protected UUID uuid;
	@Resource
	protected CoreService coreService;
	@Resource
	protected AuthService authService;
	@Resource
	protected ESBInfoService esbInfoService;
	@Resource
	protected MsgDefService msgDefService;
	@Autowired(required = false)
	protected SignatureService signatureService;
	@Autowired(required = false)
	protected JournalService journalService;
	protected MessageConverter converter = new SOAPConverter();

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	public void setCoreService(CoreService coreService)
	{
		this.coreService = coreService;
	}

	public void setAuthService(AuthService authService)
	{
		this.authService = authService;
	}

	public void setEsbInfoService(ESBInfoService esbInfoService)
	{
		this.esbInfoService = esbInfoService;
	}

	public void setMsgDefService(MsgDefService msgDefService)
	{
		this.msgDefService = msgDefService;
	}

	public void setJournalService(JournalService journalService)
	{
		this.journalService = journalService;
	}

	public void setConverter(MessageConverter converter)
	{
		this.converter = converter;
	}
}
