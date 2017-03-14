package spc.esb.data.util;

import java.util.List;

import spc.esb.data.IArrayNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;
import spc.webos.util.tree.TreeNode;

/**
 * ���ݱ��Ľṹ�Ա��Ľ��б���
 * 
 * @author spc
 * 
 */
public class MessageTraversal
{
	protected ICompositeNode root;
	protected TreeNode schema;

	public MessageTraversal(ICompositeNode root, TreeNode schema)
	{
		this.root = root;
		this.schema = schema;
	}

	public void setRoot(ICompositeNode root)
	{
		this.root = root;
	}

	public void setSchema(TreeNode schema)
	{
		this.schema = schema;
	}

	/**
	 * ���ڱ�����ȱ���
	 */
	public boolean dfs(INodeVisitor visitor) throws Exception
	{
		return processMap(root, schema, visitor);
	}

	protected boolean processMap(ICompositeNode cnode, TreeNode schema, INodeVisitor visitor)
			throws Exception
	{
		if (schema == null) return true;
		List items = schema.getChildren();
		if (items == null || cnode == null) return true;
		for (int i = 0; i < items.size(); i++)
		{
			TreeNode item = (TreeNode) items.get(i);
			MsgSchemaPO struct = (MsgSchemaPO) item.getTreeNodeValue();
			INode node = cnode.find(struct.getEsbName());
			if (node == null)
			{
				if (!visitor.start(node, item)) return false;
				if (!visitor.end(node, item)) return false;
				continue;
			}
			byte type = (byte) struct.getFtyp().charAt(0);
			if (type == INode.TYPE_MAP)
			{
				node = cnode.findComposite(struct.getEsbName(), null);
				if (!visitor.start(node, item)) return false;
				if (!processMap((ICompositeNode) node, item, visitor)) return false;
				if (!visitor.end(node, item)) return false;
			}
			else if (type == INode.TYPE_ARRAY)
			{
				node = cnode.findArray(struct.getEsbName(), null);
				if (!visitor.start(node, item)) return false;
				if (!processArray((IArrayNode) node, item, visitor)) return false;
				if (!visitor.end(node, item)) return false;
			}
			else
			{ // ����ԭ�ӽڵ� ����δ����ڵ�
				if (!visitor.start(node, item)) return false;
				if (!visitor.end(node, item)) return false;
			}
		}
		return true;
	}

	protected boolean processArray(IArrayNode anode, TreeNode schema, INodeVisitor visitor)
			throws Exception
	{
		if (anode == null) return true;
		List items = schema.getChildren();
		// 1. �����е�Ԫ��Ϊԭ������, ���ǽṹ����
		if (items == null || items.size() == 0)
		{
			for (int i = 0; i < anode.size(); i++)
			{
				if (!visitor.start(anode.getNode(i), schema)) return false;
				if (!visitor.end(anode.getNode(i), schema)) return false;
			}
			return true;
		}
		// 2. �����Ԫ��Ϊ�ṹ����
		for (int i = 0; i < anode.size(); i++)
		{
			if (!processMap((ICompositeNode) anode.getNode(i), schema, visitor)) return false;
		}
		return true;
	}
}
