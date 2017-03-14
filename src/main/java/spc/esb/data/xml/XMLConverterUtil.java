package spc.esb.data.xml;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.esb.data.ArrayNode;
import spc.esb.data.IArrayNode;
import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.data.util.IAtomProcessor;
import spc.esb.model.MsgSchemaPO;
import spc.webos.util.StringX;
import spc.webos.util.tree.TreeNode;

/**
 * xml���ĸ�ʽת��
 * 
 * @author chenjs
 * 
 */
public class XMLConverterUtil
{
	static Logger log = LoggerFactory.getLogger(XMLConverterUtil.class);

	public static void convertMap(TreeNode schema, IMessage srcMsg, ICompositeNode src,
			ICompositeNode target, boolean esb2rcv) throws Exception
	{
		convertMap(schema, srcMsg, src, target, esb2rcv, null, StringX.EMPTY_STRING);
	}

	public static void convertMap(TreeNode schema, IMessage srcMsg, ICompositeNode src,
			ICompositeNode target, boolean esb2rcv, IAtomProcessor atomProcessor, String esbPath)
			throws Exception
	{
		convertMap(schema, srcMsg, src, target, esb2rcv, atomProcessor, esbPath, true, true);
	}

	public static void convertMap(TreeNode schema, IMessage srcMsg, ICompositeNode src,
			ICompositeNode target, boolean esb2rcv, IAtomProcessor atomProcessor, String esbPath,
			boolean rcvIgnore, boolean emptyIgnore) throws Exception
	{
		convertMap(schema, srcMsg, src, target, esb2rcv, atomProcessor, null, esbPath, true, true,
				null);
	}

	public static void convertMap(TreeNode schema, IMessage srcMsg, ICompositeNode src,
			ICompositeNode target, boolean esb2rcv, IAtomProcessor atomProcessor, String esbPath,
			boolean rcvIgnore, boolean emptyIgnore, ISchemaTargetXMLTag schemaTargetXMLTag)
			throws Exception
	{
		convertMap(schema, srcMsg, src, target, esb2rcv, atomProcessor, null, esbPath, rcvIgnore,
				emptyIgnore, schemaTargetXMLTag);
	}

	public static void convertMap(TreeNode schema, IMessage srcMsg, ICompositeNode src,
			ICompositeNode target, boolean esb2rcv, IAtomProcessor atomProcessor,
			INodeProcessor nodeProcessor, String esbPath, boolean rcvIgnore, boolean emptyIgnore,
			ISchemaTargetXMLTag schemaTargetXMLTag) throws Exception
	{
		if (schema == null) return;
		List items = schema.getChildren();
		if (items == null || src == null) return;
		for (int i = 0; i < items.size(); i++)
		{
			TreeNode item = (TreeNode) items.get(i);
			MsgSchemaPO vo = (MsgSchemaPO) item.getTreeNodeValue();
			byte type = (byte) vo.getFtyp().charAt(0);

			String esbNm = vo.getEsbName();
			String rcvNm = (schemaTargetXMLTag == null) ? vo.getRcvName() : schemaTargetXMLTag
					.xmlTag(vo); // modifed by chenjs 2011-12-08
									// ֧��xmlת��Ŀ���ǩΪschema��������ֶ�
			// ������ĽṹΪԭ�ӽڵ�����ʱ�����ܷ��ֶ�Ϊ�գ���ʾ��ESB�ֶ���ͬ
			if (StringX.nullity(rcvNm)
					&& (rcvIgnore || (type == INode.TYPE_UNDEFINED && type == INode.TYPE_MAP && type == INode.TYPE_ARRAY))) rcvNm = esbNm;
			String srcNm = esb2rcv ? esbNm : rcvNm;
			String targetNm = esb2rcv ? rcvNm : esbNm;
			INode node = StringX.nullity(srcNm) ? null : src.find(srcNm);
			if (emptyIgnore && node == null) continue;
			INode targetNode = null;
			// 1. �����ǰ���Ľṹ����Ҫ��������U���ͣ�Ҳ�������ض��ṹ���ͣ���ֱ�ӷ���
			if (type == INode.TYPE_UNDEFINED) targetNode = node;
			else if (type == INode.TYPE_MAP) // node.type()
			{
				node = StringX.nullity(srcNm) ? src : src.findComposite(srcNm, null);
				if (node == null && log.isDebugEnabled()) log.debug("cnode is null by " + srcNm);
				// added by chenjs 2012-01-25
				// ����ʹ�ýӿڸı䵱ǰxml�ṹ�����罫һ��Map�ڵ��Ϊarray�ڵ�
				INode tnode = (nodeProcessor == null ? node : nodeProcessor.process(srcMsg, node,
						vo, esb2rcv, src, esbPath, target));
				if (tnode != node)
				{ // ������صĽڵ������ԭ������Ϊ�˽ڵ㷢���仯��ֱ����ΪĿ��ڵ�
					log.debug("Map:using nodeProcessor result...");
					targetNode = tnode;
				}
				else
				{
					if (!StringX.nullity(targetNm))
					{ // ���û���ṩĿ������ ����Ϊ��Ҫ�����ṹ��Ϊ��ƽ�ṹ��ֱ�Ӳ��õ�ǰ��target�ṹ���еݹ鴦��
						targetNode = target.newInstance();
						if (node != null) targetNode.setExt(((ICompositeNode) node).getExt()); // ���Բ����޸�
					}
					else targetNode = target;
					convertMap(item, srcMsg, (ICompositeNode) node, (ICompositeNode) targetNode,
							esb2rcv, atomProcessor, nodeProcessor, StringX.nullity(esbPath) ? srcNm
									: esbPath + '.' + srcNm, rcvIgnore, emptyIgnore,
							schemaTargetXMLTag);
				}
			}
			else if (type == INode.TYPE_ARRAY) // node.type()
			{
				node = src.findArray(srcNm, null);
				if (node == null && log.isDebugEnabled()) log
						.debug("arraynode is null by " + srcNm);
				// added by chenjs 2012-01-25
				// ����ʹ�ýӿڸı䵱ǰxml�ṹ�����罫һ��Map�ڵ��Ϊarray�ڵ�
				INode tnode = (nodeProcessor == null ? node : nodeProcessor.process(srcMsg, node,
						vo, esb2rcv, src, esbPath, target));
				if (tnode != node)
				{ // ������صĽڵ������ԭ������Ϊ�˽ڵ㷢���仯��ֱ����ΪĿ��ڵ�
					log.debug("Array:using nodeProcessor result...");
					targetNode = tnode;
				}
				else
				{
					targetNode = new ArrayNode();
					convertArray(item, srcMsg, (IArrayNode) node, (IArrayNode) targetNode,
							target.newInstance(), esb2rcv, atomProcessor, nodeProcessor,
							StringX.nullity(esbPath) ? srcNm : esbPath + '.' + srcNm, rcvIgnore,
							emptyIgnore);
				}
			}
			// 4. ����ԭ�ӽڵ�
			else targetNode = (atomProcessor == null ? node : atomProcessor.process(srcMsg,
					(IAtomNode) node, vo, esb2rcv, src, esbPath, target));
			if (!StringX.nullity(targetNm) && targetNode != null)
			{
				// added by chenjs 2012-03-15 ���Map or Array�ڵ㣬���ڵ�û���ӽڵ��򲻼���Ŀ��ڵ���
				if ((targetNode instanceof ICompositeNode)
						&& ((ICompositeNode) targetNode).size() == 0) continue;
				if ((targetNode instanceof IArrayNode) && ((IArrayNode) targetNode).size() == 0) continue;
				target.set(targetNm, targetNode);
			}
		}
	}

	// public static void convertArray(TreeNode msgStruct, IMessage srcMsg,
	// IArrayNode src,
	// IArrayNode target, ICompositeNode targetCN, boolean esb2rcv) throws
	// Exception
	// {
	// convertArray(msgStruct, srcMsg, src, target, targetCN, esb2rcv, null,
	// StringX.EMPTY_STRING);
	// }

	public static void convertArray(TreeNode msgStruct, IMessage srcMsg, IArrayNode src,
			IArrayNode target, ICompositeNode targetCN, boolean esb2rcv,
			IAtomProcessor atomProcessor, INodeProcessor nodeProcessor, String esbPath,
			boolean rcvIgnore, boolean emptyIgnore) throws Exception
	{
		if (src == null) return;
		List items = msgStruct.getChildren();
		// 1. �����е�Ԫ��Ϊԭ������, ���ǽṹ����
		if (items == null || items.size() == 0)
		{
			if (((MsgSchemaPO) msgStruct.getTreeNodeValue()).getFtyp().endsWith(
					String.valueOf((char) INode.TYPE_UNDEFINED)))
			{ // ������������Ԫ����U���ͣ�˵���˽ڵ㲻��Ҫ������ֱ�ӷ��룬���ڴ�͸����
				for (int i = 0; i < src.size(); i++)
					target.add(src.getNode(i));
			}
			else
			{
				for (int i = 0; i < src.size(); i++)
					// target.add((IAtomNode) src.getNode(i));
					target.add((atomProcessor == null ? (IAtomNode) src.getNode(i) : atomProcessor
							.process(srcMsg, (IAtomNode) src.getNode(i),
									(MsgSchemaPO) msgStruct.getTreeNodeValue(), esb2rcv, null,
									esbPath + '[' + i + ']', null)));
			}
			return;
		}
		// 2. �����Ԫ��Ϊ�ṹ����
		for (int i = 0; i < src.size(); i++)
		{
			ICompositeNode cnode = (ICompositeNode) src.getNode(i);
			ICompositeNode cn = targetCN.newInstance();
			target.add(cn);
			convertMap(msgStruct, srcMsg, cnode, cn, esb2rcv, atomProcessor, nodeProcessor, esbPath
					+ '[' + i + ']', rcvIgnore, emptyIgnore, null);
		}
	}
}
