package spc.esb.data.util;

import spc.esb.data.INode;
import spc.webos.util.tree.TreeNode;

/**
 * ���ݱ��Ľṹ�Ա��Ľ��б��� ��ʹ�õĽڵ���ʽӿ�
 * 
 * @author spc
 * 
 */
public interface INodeVisitor
{
	/**
	 * ��ʾ����ýڵ��¼����������true��ʾ���ʼ���������false���ٱ��������Ľṹ
	 * 
	 * @param node
	 * @param nodeSchema
	 * @return
	 */
	boolean start(INode node, TreeNode nodeSchema) throws Exception;

	/**
	 * ��ʾ�����ýڵ��¼�
	 * 
	 * @param node
	 * @param nodeSchema
	 * @return
	 * @throws Exception
	 */
	boolean end(INode node, TreeNode nodeSchema) throws Exception;
}
