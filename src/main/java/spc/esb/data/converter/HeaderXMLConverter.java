package spc.esb.data.converter;

import spc.esb.constant.ESBMsgLocalKey;
import spc.esb.data.Array2Node2XML;
import spc.esb.data.IMessage;
import spc.esb.data.Message;
import spc.esb.data.util.MessageUtil;
import spc.webos.util.StringX;

/**
 * ��־ģ�����XML������Ϣ��Ϊ�����ESB�����ܣ�����Ҫ����xml���ĵ�body���֣�ֻ����ͷ����
 * 
 * @author spc
 * 
 */
public class HeaderXMLConverter extends SOAPConverter
{
	public HeaderXMLConverter()
	{
		node2xml = Array2Node2XML.getInstance();
	}

	public IMessage deserialize(byte[] buf, IMessage reqmsg) throws Exception
	{
		IMessage msg = new Message();
		// 711_20140725 ����ǰ�˶�json��ʽ����
		if (isJSON(buf)) msg = deserializeJSON(buf, reqmsg);
		else msg.setTransaction(deserialize2composite(MessageUtil.removeBody(buf)));
		msg.setInLocal(ESBMsgLocalKey.LOCAL_ORIGINAL_REQ_BYTES, buf);
		return msg;
	}

	public byte[] serialize(IMessage msg) throws Exception
	{
		String originalBytes = msg.getOriginalBytesPlainStr();
		if (!StringX.nullity(originalBytes))
		{ // ��ǰ����fixmsg����ʱ��AsynESBCall�ڵ�ʹ�ô����л����������л�ʱ��Ҫ��ȫ���ķ��뵽REQ����
			log.info("originalBytes.len=" + originalBytes.length());
			return super.serialize(msg);
		}
		byte[] xml = (byte[]) msg.getInLocal(ESBMsgLocalKey.LOCAL_ORIGINAL_REQ_BYTES);
		if (xml != null) return xml;
		log.info("cannot find LOCAL_ORIGINAL_REQ_BYTES !!!");
		return super.serialize(msg);
	}

	static HeaderXMLConverter HXC = new HeaderXMLConverter();

	public static SOAPConverter getInstance()
	{
		return HXC;
	}
}
