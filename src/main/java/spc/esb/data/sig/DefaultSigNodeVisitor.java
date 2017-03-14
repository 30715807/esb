package spc.esb.data.sig;

import java.util.ArrayList;
import java.util.List;

import spc.esb.data.IArrayNode;
import spc.esb.data.INode;
import spc.esb.data.util.INodeVisitor;
import spc.esb.model.MsgSchemaPO;
import spc.webos.util.StringX;
import spc.webos.util.tree.TreeNode;

/**
 * Ĭ�ϻ�������ǩ���Ľڵ����ģʽ������Ϊshcema�����˳������Ҫ��������ǩ���Ľڵ����ݺͽڵ�������Ϣ
 * 
 * @author spc
 * 
 */
public class DefaultSigNodeVisitor implements INodeVisitor {
	protected List<Object[]> sigCnts = new ArrayList<>();

	public boolean start(INode node, TreeNode nodeSchema) throws Exception {
		// modified by chenjs 2011-10-02
		// ���ֵΪnull�����ֶ���Ҫǩ����������룬�о�������ǩ�����ݵĽӿڸ����ж��Ƿ���Ҫ
		// if (node == null) return true;
		MsgSchemaPO schema = (MsgSchemaPO) nodeSchema.getTreeNodeValue();
		// byte type = (byte) schema.getFtyp().charAt(0);
		// if (type == INode.TYPE_ARRAY) return true;
		// if ("Y".equalsIgnoreCase(schema.getSig()))
		// modified by chenjs 2011-10-02 ǩ�������ԭ����Y/N
		if (!StringX.nullity(schema.getSig()) && (!(node instanceof IArrayNode)))
			sigCnts.add(new Object[] { node, schema });
		return true;
	}

	public boolean end(INode node, TreeNode nodeSchema) throws Exception {
		return true;
	}

	public List<Object[]> getSigCnts() {
		return sigCnts;
	}
}
