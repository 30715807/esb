package spc.esb.converter;

import spc.esb.constant.ESBMsgLocalKey;
import spc.esb.data.IMessage;

/**
 * ר����̨ϵͳ��tcp/http�� �����Ĺ淶ȴ��ESB xml, ESB soap����ʹ��
 * 
 * @author chenjs
 * 
 */
public class BAESBXMLMsgConverter extends AbstractMsgConverter
{
	public IMessage deserialize(byte[] buf, IMessage reqmsg) throws Exception
	{ // ������ŷ��ص�esb xml or esb soap���뵽local������
		reqmsg.setInLocal(ESBMsgLocalKey.LOCAL_REP_BYTES, buf);
		return reqmsg;
	}

	public byte[] serialize(IMessage msg) throws Exception
	{ // ֱ�ӷ������񷽴�MQ�����л�ȡ��esb xml or esb soap�������ݷ�����̨ϵͳ
		return (byte[]) msg.getInLocal(ESBMsgLocalKey.LOCAL_ORIGINAL_REQ_BYTES);
	}
}
