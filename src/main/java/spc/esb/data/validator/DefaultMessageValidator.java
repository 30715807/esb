package spc.esb.data.validator;

import spc.esb.data.IMessage;

/**
 * �������ݿ��������Ϣ��֤�����еĸ��ֶ�, ������Errors��Ϣ�� ��ȫ�Ľṹ����֤
 * 
 * @author spc
 * 
 */
public class DefaultMessageValidator extends AbstractMessageValidator
{
	String[] validators;

	public String[] getValidators(IMessage msg)
	{
		return validators;
	}

	public void setValidators(String[] validators)
	{
		this.validators = validators;
	}
}
