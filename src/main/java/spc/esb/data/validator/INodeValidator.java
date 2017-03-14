package spc.esb.data.validator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.Errors;

import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.webos.util.tree.TreeNode;

/**
 * ��һ�ڵ�������֤����������֤�����еĵ�һ�ڵ�
 * 
 * @author spc
 * 
 */
public interface INodeValidator
{
	/**
	 * ��֤������ÿ��ԭ�ӽڵ�
	 * 
	 * @param msg
	 *            ȫ������Ϣ
	 * @param node
	 *            ����֤�ڵ�, �ڵ������ԭ�ӽڵ�͸��ӣ�����ڵ�
	 * @param attr
	 *            ��������Ϣ�ṹ��struct ��Ϣ�ṹ
	 * @param errors
	 *            ��������
	 */
	void validate(IMessage msg, String field, INode node, TreeNode tnode, Errors errors);

	final static Map VALIDATOR = new HashMap();
}
