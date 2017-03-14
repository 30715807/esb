package spc.esb.converter;

import spc.esb.constant.ESBMsgLocalKey;
import spc.esb.data.IMessage;
import spc.esb.data.util.MessageUtil;
import spc.webos.util.StringX;

/**
 * �ں������˱���ת��������£���˷����������ı���ת��ģʽ�������ʺ�ͬ�������˷���ĳ���
 * 
 * @author chenjs
 * 
 */
public class OriginalBytesMsgConverter extends AbstractMsgConverter
{
	public IMessage deserialize(byte[] buf, IMessage reqmsg) throws Exception
	{
		reqmsg = getRequestMsg(buf, reqmsg);
		reqmsg.setOriginalBytes(buf);
		return reqmsg;
	}

	public byte[] serialize(IMessage msg) throws Exception
	{
		byte[] originalBytes = msg.getOriginalBytes();
		if (originalBytes != null) return originalBytes;
		// 400 chenjs 2013-07-29
		// ���ʹ�õ���HeaderXMLConverter����������ֻ������header���֣���Ҫ��ԭʼ�����г�ȡ
		originalBytes = MessageUtil.getOriginalBytes((byte[]) msg
				.getInLocal(ESBMsgLocalKey.LOCAL_ORIGINAL_REQ_BYTES)); // ���originalBytes���ڣ����ȡ
		return StringX.decodeBase64(originalBytes);
	}
}
