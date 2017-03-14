package spc.esb.data.validator;

import java.text.MessageFormat;

import org.springframework.validation.Errors;

import spc.esb.constant.ESBRetCode;
import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;
import spc.webos.util.RegExp;
import spc.webos.util.StringX;
import spc.webos.util.tree.TreeNode;

/**
 * �ַ�����顣
 * 
 * @author spc
 * 
 */
public class StringCharsetValidator extends AbstractNodeValidator
{
	public void validate(IMessage msg, String field, INode node, TreeNode tnode, Errors errors)
	{
		if (node == null) return;
		String value = node.toString();
		if (StringX.nullity(value)) return;
		boolean ok = true;
		if (CHARSET_N.equalsIgnoreCase(name)) ok = RegExp.match(value, RegExp.CHARSET_N);
		else if (CHARSET_A.equalsIgnoreCase(name)) ok = RegExp.match(value, RegExp.CHARSET_A);
		else if (CHARSET_X.equalsIgnoreCase(name)) ok = RegExp.match(value, RegExp.CHARSET_X);
		if (!ok)
		{
			MsgSchemaPO struct = (MsgSchemaPO) tnode.getTreeNodeValue();
			String fieldName = StringX.nullity(struct.getFdesc()) ? field : struct.getFdesc();
			Object[] args = new Object[] { fieldName, value };
			errors.rejectValue(fieldName, errCd, args, new MessageFormat(msgFormat).format(args));
		}
	}

	public void init() throws Exception
	{
		super.init();
		msgFormat = "{0}({1}) \u4E0D\u662F\u5408\u6CD5\u5B57\u7B26\u96C6(";
		if (CHARSET_N.equalsIgnoreCase(name)) msgFormat += "n)";
		else if (CHARSET_A.equalsIgnoreCase(name)) msgFormat += "a)";
		else if (CHARSET_X.equalsIgnoreCase(name)) msgFormat += "x)";
		else if (CHARSET_G.equalsIgnoreCase(name)) msgFormat += "g)";
		else msgFormat += "z)";
		msgFormat = StringX.utf82str(msgFormat);
	}

	public StringCharsetValidator()
	{
		errCd = ESBRetCode.MSG_FIELD_VALIDATOR;
		name = CHARSET_N;
	}

	public final static String CHARSET_N = "charset_n"; // ��ʾ0��9������
	public final static String CHARSET_A = "charset_a"; // ���ֺ���ĸ
	public final static String CHARSET_X = "charset_x"; // ��ʾx-�ַ����е������ַ�
	public final static String CHARSET_G = "charset_g"; // ��ʾx-�ַ�����GB-2312�ַ������Ի��ʹ��
	public final static String CHARSET_Z = "charset_z"; // ��ISO7811��ISO7813����Ĵſ��ڶ��͵����ŵ��Ĵ��뼯
}
