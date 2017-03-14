package spc.esb.converter;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import spc.esb.common.service.ESBInfoService;
import spc.esb.common.service.MsgDefService;
import spc.esb.common.service.SignatureService;
import spc.esb.data.IMessage;
import spc.esb.data.MessageAttr;
import spc.esb.data.fixmsg.DefaultAtom2FixedLen;
import spc.esb.data.fixmsg.IAtom2FixedLenConverter;
import spc.esb.data.iso8583.IISO8583MessageConverter;
import spc.esb.data.iso8583.ISO8583MessageConverter;
import spc.esb.data.util.IAtomProcessor;
import spc.esb.data.util.MessageTranslator;
import spc.esb.data.xml.INodeProcessor;
import spc.esb.data.xml.ISchemaTargetXMLTag;
import spc.webos.constant.Common;
import spc.webos.service.seq.UUID;
import spc.webos.util.StringX;

public class BaseMsgConverter
{
	@Autowired(required = false)
	protected UUID uuid;
	@Resource
	protected MsgDefService msgDefService;
	@Resource
	protected ESBInfoService esbInfoService;
	@Autowired(required = false)
	protected SignatureService signatureService;
	protected String charset = Common.CHARSET_UTF8;
	@Autowired(required = false)
	protected IAtom2FixedLenConverter atom2FixedLen = DefaultAtom2FixedLen.getInstance();
	@Autowired(required = false)
	protected IAtomProcessor atomProcessor; // for xml -> xml' ģʽʹ��
	@Autowired(required = false)
	protected INodeProcessor nodeProcessor; // for xml -> xml' ģʽʹ��
	@Autowired(required = false)
	protected ISchemaTargetXMLTag schemaTargetXMLTag; // for xml -> xml' ģʽʹ��
	protected boolean rcvIgnore = true; // for xml -> xml' ģʽʹ��
	protected boolean emptyIgnore = true; // for xml -> xml' ģʽʹ��
	protected String appCd;
	protected String mbrCd;
	protected boolean bcd = false;
	protected boolean ba = true; // ��BAʹ�û���FAʹ��, Ĭ��ΪBA

	// �첽MQ��Ϣƥ��ʱ����
	protected boolean asynHeaderOnly = true; // �Ƿ�ֻ�ű���ͷ��Ϣ
	protected int asynExpireSeconds = 60; // ��Ϣ��Ĭ�ϳ�ʱʱ��(��)

	// added by chenjs 2011-10-03 ���������п�����Щ���ľ��й̶�ͷ�ṹ
	protected String esb2appHdrTag; // ESB����App����ʱͷ��Tag��ǩ
	protected String esb2appHdrSchema; // ESB����App����ʱͷ��schema
	protected String app2esbHdrTag; // App����ESB����ʱͷ��Tag��ǩ
	protected String app2esbHdrSchema; // App����ESB������ͷ��schema
	// added by chenjs 2011-12-09
	protected String esb2appErrSchema; // ������schema
	protected String app2esbErrSchema; // ������schema

	// protected IHandleArrayNodeService handleArrayNodeService; // �������е�����ڵ�
	protected IISO8583MessageConverter iso8583MsgConverter = new ISO8583MessageConverter();
	protected MessageTranslator translator; // added by chenjs 2011-07-06
	// ���ǵ�ĳЩ���������Ҫfa��������ת��
	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected String name;

	public String getContentType()
	{
		return null;
	}

	public boolean isContainArray(IMessage msg)
	{
		String msgCd = msg.getMsgCd();
		MessageAttr attr = msgDefService.getMsgAttr(msgCd);
		if (attr != null && attr.isContainArray())
		{
			if (log.isInfoEnabled()) log.info(msgCd + " contain array");
			return true;
		}
		return false;
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

	public void genSndInfo(IMessage msg)
	{
		String dt = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date());
		msg.setSndDt(dt.substring(0, 8));
		msg.setSndTm(dt.substring(8, 17));
		msg.setSeqNb(String.valueOf(uuid.uuid()));

		msg.setSndNode(mbrCd);
		msg.setSndAppCd(appCd);
		if (StringX.nullity(msg.getSndNode()) && !StringX.nullity(msg.getRcvNode()))
			msg.setSndNode(msg.getRcvNode());
		if (StringX.nullity(msg.getSndApp()) && !StringX.nullity(msg.getRcvApp()))
			msg.setSndAppCd(msg.getRcvApp());
	}

	/**
	 * �ǲ�ѯ���ף�����ԭʼ�������������
	 * 
	 * @param msg
	 * @param original
	 */
	public void setOriginalBytes(IMessage msg, byte[] original)
	{
		msg.setOriginalBytes(original);
	}

	// // ��BAģʽ�¸��ݷ���ϵͳ�ͷ��񷽱��ı�ŵõ�ESB���ı��
	// protected String getESBMsgCdByBA(String appCd, String appMsgCd)
	// {
	// MessageVO msgvo = msgDefService.getRcvMessage(appCd, appMsgCd); //
	// �����Ӧ��������Сдƥ��
	// if (msgvo == null)
	// {
	// log.warn("msgvo is null by appCd:" + appCd + ", appMsgCd: " + appMsgCd);
	// return null;
	// }
	// if (log.isDebugEnabled()) log.debug("appCd:" + appCd + ", appMsgCd:" +
	// appMsgCd
	// + ", esb msgcd: " + msgvo.getMsgCd());
	// return msgvo.getMsgCd();
	// }

	public void setMsgDefService(MsgDefService msgDefService)
	{
		this.msgDefService = msgDefService;
	}

	public void setEsbInfoService(ESBInfoService esbInfoService)
	{
		this.esbInfoService = esbInfoService;
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	public String getCharset()
	{
		return charset;
	}

	public void setAtom2FixedLen(IAtom2FixedLenConverter atom2FixedLen)
	{
		this.atom2FixedLen = atom2FixedLen;
	}

	public String getAppCd()
	{
		return appCd;
	}

	public void setAppCd(String appCd)
	{
		this.appCd = appCd;
	}

	public boolean isBcd()
	{
		return bcd;
	}

	public void setBcd(boolean bcd)
	{
		this.bcd = bcd;
	}

	public void setIso8583MsgConverter(IISO8583MessageConverter iso8583MsgConverter)
	{
		this.iso8583MsgConverter = iso8583MsgConverter;
	}

	public void setTranslator(MessageTranslator translator)
	{
		this.translator = translator;
	}

	public boolean isBa()
	{
		return ba;
	}

	public void setBa(boolean ba)
	{
		this.ba = ba;
	}

	public String getEsb2appHdrTag()
	{
		return esb2appHdrTag;
	}

	public void setEsb2appHdrTag(String esb2appHdrTag)
	{
		this.esb2appHdrTag = esb2appHdrTag;
	}

	public String getEsb2appHdrSchema()
	{
		return esb2appHdrSchema;
	}

	public void setEsb2appHdrSchema(String esb2appHdrSchema)
	{
		this.esb2appHdrSchema = esb2appHdrSchema;
	}

	public String getApp2esbHdrTag()
	{
		return app2esbHdrTag;
	}

	public void setApp2esbHdrTag(String app2esbHdrTag)
	{
		this.app2esbHdrTag = app2esbHdrTag;
	}

	public String getApp2esbHdrSchema()
	{
		return app2esbHdrSchema;
	}

	public void setApp2esbHdrSchema(String app2esbHdrSchema)
	{
		this.app2esbHdrSchema = app2esbHdrSchema;
	}

	public String getEsb2appErrSchema()
	{
		return esb2appErrSchema;
	}

	public void setEsb2appErrSchema(String esb2appErrSchema)
	{
		this.esb2appErrSchema = esb2appErrSchema;
	}

	public String getApp2esbErrSchema()
	{
		return app2esbErrSchema;
	}

	public void setApp2esbErrSchema(String app2esbErrSchema)
	{
		this.app2esbErrSchema = app2esbErrSchema;
	}

	public void setSignatureService(SignatureService signatureService)
	{
		this.signatureService = signatureService;
	}

	public void setAtomProcessor(IAtomProcessor atomProcessor)
	{
		this.atomProcessor = atomProcessor;
	}

	public void setSchemaTargetXMLTag(ISchemaTargetXMLTag schemaTargetXMLTag)
	{
		this.schemaTargetXMLTag = schemaTargetXMLTag;
	}

	public void setRcvIgnore(boolean rcvIgnore)
	{
		this.rcvIgnore = rcvIgnore;
	}

	public void setEmptyIgnore(boolean emptyIgnore)
	{
		this.emptyIgnore = emptyIgnore;
	}

	public void setNodeProcessor(INodeProcessor nodeProcessor)
	{
		this.nodeProcessor = nodeProcessor;
	}

	public void setAsynHeaderOnly(boolean asynHeaderOnly)
	{
		this.asynHeaderOnly = asynHeaderOnly;
	}

	public void setAsynExpireSeconds(int asynExpireSeconds)
	{
		this.asynExpireSeconds = asynExpireSeconds;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setMbrCd(String mbrCd)
	{
		this.mbrCd = mbrCd;
	}
}
