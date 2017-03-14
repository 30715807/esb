package spc.esb.data.validator;

import java.text.MessageFormat;

import org.springframework.validation.Errors;

import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.model.MsgValidatorPO;
import spc.webos.util.StringX;

/**
 * Ϊ���Ӷ�ڵ�֮�����֤�ṩ�ӱ����л�ȡ����ֵ��ģ��
 * 
 * @author spc
 * 
 */
public abstract class AbstractCompositeValidator implements ICompositeValidator
{
	public void init() throws Exception
	{
		if (name != null) VALIDATOR.put(name, this);
	}

	/**
	 * ���ǵ����ɴ�����Ϣ�Ķ�����, ������װһ������������������Ϣ����
	 * 
	 * @param errors
	 * @param fieldName
	 * @param args
	 */
	public void reject(Errors errors, String fieldName, Object[] args, MsgValidatorPO msgValidatorVO)
	{
		fieldName = fieldName.replace('/', '.');
		// �����ǰ��֤��û��������˽�д�����ʹ�����Ϣ����
		if (StringX.nullity(msgValidatorVO.getMsgFormat())) errors.rejectValue(fieldName, errCd,
				args, msgFormat == null ? null : new MessageFormat(msgFormat).format(args));
		else errors.rejectValue(fieldName, msgValidatorVO.getErrCd(), args, msgValidatorVO
				.getMsgFormat() == null ? null : new MessageFormat(msgValidatorVO.getMsgFormat())
				.format(args));
	}

	public INode getNode(IMessage msg, String path)
	{
		if (msg.isRequestMsg()) return msg.findInRequest(path);
		return msg.findInResponse(path);
	}

	protected String name;
	protected String errCd; // ��֤�Ĵ�����
	protected String msgFormat; // ��֤�Ĵ���ģ��

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getErrCd()
	{
		return errCd;
	}

	public void setErrCd(String errCd)
	{
		this.errCd = errCd;
	}

	public String getMsgFormat()
	{
		return msgFormat;
	}

	public void setMsgFormat(String msgFormat)
	{
		this.msgFormat = msgFormat;
	}
}
