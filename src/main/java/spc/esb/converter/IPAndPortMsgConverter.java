package spc.esb.converter;

import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

import spc.esb.constant.ESBMsgLocalKey;
import spc.esb.data.IMessage;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

/**
 * ͨ�õ�ǰ�˱��Ľ���ת�����������ܵĶ����Ʒ���msg��ͬʱ���ݽ���ip�Ͷ˿ڲ��ҷ��ͷ�ϵͳ��ŷ���sndappcd�С� ��
 * DefaultCoreMessageConverter ���ʹ��
 * 
 * @author chenjs
 * 
 */
public class IPAndPortMsgConverter extends AbstractMsgConverter
{
	public IMessage deserialize(byte[] buf, IMessage msg) throws Exception
	{
		msg.setOriginalBytes(buf);
		String remoteIP = (String) msg.getInLocal(ESBMsgLocalKey.ACCEPTOR_REMOTE_HOST);
		Integer localPort = (Integer) msg.getInLocal(ESBMsgLocalKey.ACCEPTOR_LOCAL_PORT);

		// ����uri, localport, remoteIP��ȡ�����Ϣ
		msg.setSndAppCd(StringX.nullity(sndAppCd)
				? esbInfoService.getNodeByUriPortIP(localPort, remoteIP).getAppCd() : sndAppCd);
		// ͬ������ģʽ��, �ӱ����л�ȡsnddt, seqnb�ֶ�
		String sndDt = FastDateFormat.getInstance("yyyyMMdd").format(new Date());
		if (!StringX.nullity(sndDt)) msg.setSndDt(sndDt);
		String seqNb = SpringUtil.random(randomSeqNbLen);
		if (!StringX.nullity(seqNb)) msg.setSeqNb(seqNb);
		if (log.isInfoEnabled()) log.info("ip:" + remoteIP + ", port:" + localPort + ", header: "
				+ msg.getHeader().toXml(IMessage.TAG_HEADER, false));
		return msg;
	}

	public byte[] serialize(IMessage msg) throws Exception
	{
		byte[] response = msg.getOriginalBytes();
		if (response == null)
		{ // ���original bytesΪ�գ����ʾ����MQ Call�����г����쳣
			if (log.isInfoEnabled()) log.info(
					"default response:" + (defaultResponse != null ? defaultResponse.length : 0)
							+ ", header:" + msg.getHeader().toXml(IMessage.TAG_HEADER, true));
			if (defaultResponse != null) return defaultResponse;
		}
		return response;
	}

	protected String sndAppCd; // ���ָ�����巢��ϵͳ
	protected int randomSeqNbLen = 15;
	protected byte[] defaultResponse; // ����������Ĭ�Ϸ���ǰ�˵��ֽڣ�ʹ��base64ע��

	public void setSndAppCd(String sndAppCd)
	{
		this.sndAppCd = sndAppCd;
	}

	public void setRandomSeqNbLen(int randomSeqNbLen)
	{
		this.randomSeqNbLen = randomSeqNbLen;
	}

	public byte[] getDefaultResponse()
	{
		return defaultResponse;
	}

	public void setDefaultResponse(byte[] defaultResponse)
	{
		this.defaultResponse = defaultResponse;
	}

	public void setDefaultResponse(String defaultResponse)
	{
		this.defaultResponse = StringX.decodeBase64(defaultResponse);
	}
}
