package spc.esb.data.fixmsg;

import spc.esb.data.IArrayNode;
import spc.esb.data.ICompositeNode;
import spc.webos.util.tree.TreeNode;

/**
 * 2011-08-05 ��Ҫ֧�ֶ������ж�������
 * 
 * @author spc
 * 
 */
public interface IArray2FixedLenConverter2
{
	// ��Ҫ���������ܳ���
	int pack(ICompositeNode root, ICompositeNode pnode, byte[] fixedLen, int offset,
			IArrayNode value, TreeNode schema, IAtom2FixedLenConverter atom2fixedLen, String charset)
			throws Exception;

	int unpack(ICompositeNode root, ICompositeNode pnode, byte[] fixedLen, int offset,
			IArrayNode value, TreeNode schema, IAtom2FixedLenConverter atom2fixedLen, String charset)
			throws Exception;
}
