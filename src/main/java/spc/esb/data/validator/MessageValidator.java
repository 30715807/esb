package spc.esb.data.validator;

import spc.esb.data.IMessage;

/**
 * ���ݱ��ı�Ż�ȡ��֤Դ��MessageValidator ֻУ��request ���ֺ� response����
 * 
 * @author spc
 * 
 */
public class MessageValidator extends AbstractMessageValidator
{
	public String[] getValidators(IMessage msg)
	{
		return new String[] { msg.getMsgCd() };
	}

	public MessageValidator()
	{
		name = "MSGCD";
	}
}
