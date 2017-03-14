package spc.esb.data.validator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.Errors;

import spc.esb.data.IMessage;
import spc.esb.model.MsgValidatorPO;

/**
 * �����֤�����ݸ����Ķ���ֶΣ���֤����ֶ�֮��Ĺ�ϵ
 * 
 * @author spc
 * 
 */
public interface ICompositeValidator
{
	void validate(IMessage msg, MsgValidatorPO msgValidatorVO, Errors errors);

	final static Map VALIDATOR = new HashMap();
}
