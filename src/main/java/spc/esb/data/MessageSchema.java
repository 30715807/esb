package spc.esb.data;

import java.util.List;

import spc.webos.util.tree.TreeNode;

/**
 * �����֤��Դ(����schema)
 * 
 * @author spc
 * 
 */
public interface MessageSchema
{
	public final static String MO_OPTIONAL = "O";
	public final static String MO_optional = "o"; // �����������һ���ձ�ǩ�������Ĭ��ֵ���
	public final static String MO_MUST = "M";
	public final static String MO_must = "m"; // ���������û�д���ǩ�������Ĭ��ֵ�����û��Ĭ��ֵ�ḳ��һ���ձ�ǩ

	/**
	 * ��ȡ���Ʊ��Ľṹ�ĵ��ڵ���֤ģʽ
	 * 
	 * @param name
	 * @return
	 */
	TreeNode getMsgSchema(String name);

	/**
	 * ��ȡ���ĵĶ�ڵ���֤ģʽ
	 * 
	 * @param name
	 * @return
	 */
	List getMsgValidator(String name);
}
