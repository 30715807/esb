package spc.esb.data.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.esb.data.ArrayNode;
import spc.esb.data.AtomNode;
import spc.esb.data.CompositeNode;
import spc.esb.data.IArrayNode;
import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.data.MessageSchema;
import spc.esb.model.MsgSchemaPO;
import spc.webos.util.StringX;
import spc.webos.util.tree.TreeNode;

/**
 * ��������εı���ת��,�ӱ�׼��ESB���ĵ�Ŀ��ESB����,�м���ǩ�����, ת����, ���ͼ��, ���ȼ��, �ֶ���ת��.
 * ���ɵ�ESB������Ϣ����ȫ�Ǻ�̨Ҫ��ı���, ֻ�Ǳ��ĸ�ʽ�ϻ���esb����
 * 
 * @author spc
 * 
 */
public class MessageTranslator
{
	protected static Logger log = LoggerFactory.getLogger(MessageTranslator.class);

	// ��һ��cnode����schema�ṹ���������������͸��ӽڵ����ͣ�
	// ��Ϊ�������ֻ��һ���ڵ�����ڽ���ʱ��Ϊ��һ��ԭ�����͵ı�ǩ
	public static ICompositeNode adjust(TreeNode schema, ICompositeNode cnode)
	{
		if (cnode == null || schema == null) return null;
		List items = schema.getChildren();
		if (items == null) return null;
		ICompositeNode node = new CompositeNode();
		for (int i = 0; i < items.size(); i++)
		{
			TreeNode item = (TreeNode) items.get(i);
			MsgSchemaPO vo = (MsgSchemaPO) item.getTreeNodeValue();
			byte type = (byte) vo.getFtyp().charAt(0);
			if (type == INode.TYPE_MAP)
			{
				ICompositeNode value = cnode.findComposite(vo.getEsbName(), null);
				if (value == null) continue;
				node.set(vo.getEsbName(), adjust(item, value));
			}
			else if (type == INode.TYPE_ARRAY)
			{
				IArrayNode value = cnode.findArray(vo.getEsbName(), null);
				if (value == null) continue;
				IArrayNode nanode = new ArrayNode();
				for (int j = 0; j < value.size(); j++)
				{
					INode nn = value.getNode(j);
					if (nn instanceof ICompositeNode) nanode.add(adjust(item, (ICompositeNode) nn));
					else nanode.add(nn);
				}
				node.set(vo.getEsbName(), nanode);
			}
			else
			{
				IAtomNode value = cnode.findAtom(vo.getEsbName(), null);
				if (value == null) continue;
				node.set(vo.getEsbName(), value);
			}
		}
		return node;
	}

	public void translateMap(TreeNode schema, IMessage srcMsg, ICompositeNode src,
			ICompositeNode target, boolean esb2rcv) throws Exception
	{
		translateMap(schema, srcMsg, src, target, esb2rcv, StringX.EMPTY_STRING);
	}

	public void translateMap(TreeNode schema, IMessage srcMsg, ICompositeNode src,
			ICompositeNode target, boolean esb2rcv, String path) throws Exception
	{
		translateMap(schema, srcMsg, src, target, esb2rcv, false, StringX.EMPTY_STRING);
	}

	/**
	 * ֻ������ת��
	 * 
	 * @param schema
	 * @param srcMsg
	 * @param src
	 * @param target
	 */
	public void translateMap(TreeNode schema, IMessage srcMsg, ICompositeNode src,
			ICompositeNode target, boolean esb2rcv, boolean autoFilterUndefinedTag, String path)
					throws Exception
	{
		List items = schema.getChildren();
		if (items == null || items.size() == 0)
		{
			log.debug("schema children is null, path:{}", path);
			return;
		}
		for (int i = 0; i < items.size(); i++)
		{
			TreeNode subSchema = (TreeNode) items.get(i);
			MsgSchemaPO vo = (MsgSchemaPO) subSchema.getTreeNodeValue();
			byte type = (byte) vo.getFtyp().charAt(0);

			String esbNm = vo.getEsbName();
			if (type == INode.TYPE_UNDEFINED)
			{
				INode node = src.find(esbNm);
				if (node != null) target.set(esbNm, node);
				continue;
			}
			else if (type == INode.TYPE_MAP)
			{
				ICompositeNode node = src.findComposite(esbNm, null);
				// modified by chenjs 2011-09-02 ֧�ֲ�����M����ֵʱ��һ��Ĭ��ֵ
				if ((node == null || node.size() == 0)
						&& !MessageSchema.MO_must.equals(vo.getOptional()))
					continue;
				if (node == null) node = new CompositeNode();

				ICompositeNode targetNode = target.newInstance();
				// 2012-06-12 ��ԭ����ȫ������һ��
				if (!autoFilterUndefinedTag) targetNode.set(node);
				targetNode.setExt(node.getExt()); // ���Բ����޸�

				// 700 2013-06-05 ����autoFilterUndefinedTag���ò�������
				translateMap(subSchema, srcMsg, node, targetNode, esb2rcv, autoFilterUndefinedTag,
						StringX.nullity(path) ? vo.getEsbName() : path + '.' + vo.getEsbName());
				if (targetNode.size() == 0
						&& (targetNode.getExt() == null || targetNode.getExt().size() == 0))
				{ // �����ǰcnode�ڵ�û���κ��ӽڵ㣬Ҳû���κ���������� 2012-03-02
					log.debug("esbNm:{}, a empty cnode without attrs", esbNm);
					target.remove(esbNm); // 2012-07-10 �����Ҫ���˱�ǩ��ɾ��ԭ��ֵ
				}
				else target.set(esbNm, targetNode);
			}
			else if (type == INode.TYPE_ARRAY)
			{
				IArrayNode node = src.findArray(esbNm, null);
				if (node == null || node.size() == 0) continue;
				IArrayNode targetNode = new ArrayNode();
				translateArray(subSchema, srcMsg, node, targetNode, target.newInstance(), esb2rcv,
						autoFilterUndefinedTag,
						StringX.nullity(path) ? vo.getEsbName() : path + '.' + vo.getEsbName());

				if (targetNode.size() == 0
						&& (targetNode.getExt() == null || targetNode.getExt().size() == 0))
				{ // �����ǰcnode�ڵ�û���κ��ӽڵ㣬Ҳû���κ���������� 2012-08-22
					log.debug("esbNm:{}, a empty cnode without attrs", esbNm);
					target.remove(esbNm); // 2012-08-22 �����Ҫ���˱�ǩ��ɾ��ԭ��ֵ
				}
				else target.set(esbNm, targetNode);
			}
			else
			{ // atom
				IAtomNode node = src.findAtom(esbNm, null);
				if (!StringX.nullity(vo.getDefValue())
						&& ((node == null && MessageSchema.MO_OPTIONAL.equals(vo.getOptional())
								|| ((node == null || StringX.nullity(node.stringValue()))
										&& MessageSchema.MO_optional.equals(vo.getOptional())))))
				{ // �����ǰ����������Ĭ��ֵ��������û�д��˱�ǩ������Сo���ͱ���ȴֻ����һ���ձ�ǩ
					node = new AtomNode(vo.getDefValue());
					log.debug("set def value for [{}]={}", vo.getEsbName(), node);
				}
				else if (node == null && MessageSchema.MO_must.equals(vo.getOptional()))
				{ // �����Сm���ͣ���û������˱�ǩ������Ĭ��ֵ���߿��ַ������
					node = new AtomNode(StringX.null2emptystr(vo.getDefValue()).trim());
					log.debug("set m for [{}]=[{}]", vo.getEsbName(), node.stringValue());
				}
				else if (node == null && log.isDebugEnabled())
					log.debug("{} is null, optional:{}", vo.getEsbName(), vo.getOptional());
				// modified by chenjs 2011-07-20, ����Բ����ڵı�ǩ����ftl, cvt
				node = atomProcessor.process(srcMsg, node, vo, esb2rcv, src, path, target);

				// modified by chenjs 2012-12-01
				// ���˺��Ա�ǩ�������processAtom֮����������processAtom����null��ǩ
				// �����ǰ�ӱ�ǩΪ���ַ�����ǩ����Ϊ��ѡ(O,o)��ǩ������˴˱�ǩ
				if ((node == null || StringX.nullity(node.toString()))
						&& MessageSchema.MO_OPTIONAL.equalsIgnoreCase(vo.getOptional()))
				{
					log.debug("remove null tag({}), value:{}", esbNm,
							StringX.null2emptystr(target.getNode(esbNm)));
					target.remove(esbNm); // 2012-07-10 �����Ҫ���˱�ǩ��ɾ��ԭ��ֵ
					continue;
				}
				if (node != null) target.set(esbNm, node);
			}
		}
	}

	public void translateArray(TreeNode msgStruct, IMessage srcMsg, IArrayNode src,
			IArrayNode target, ICompositeNode targetCN, boolean esb2rcv, String path)
					throws Exception
	{
		translateArray(msgStruct, srcMsg, src, target, targetCN, esb2rcv, false, path);
	}

	public void translateArray(TreeNode msgStruct, IMessage srcMsg, IArrayNode src,
			IArrayNode target, ICompositeNode targetCN, boolean esb2rcv,
			boolean autoFilterUndefinedTag, String path) throws Exception
	{
		List items = msgStruct.getChildren();
		// 1. �����е�Ԫ��Ϊԭ������, ���ǽṹ����
		if (items == null || items.size() == 0)
		{
			if (((MsgSchemaPO) msgStruct.getTreeNodeValue()).getFtyp()
					.endsWith(String.valueOf((char) INode.TYPE_UNDEFINED)))
			{ // ������������Ԫ����U���ͣ�˵���˽ڵ㲻��Ҫ������ֱ�ӷ��룬���ڴ�͸����
				for (int i = 0; i < src.size(); i++)
					target.add(src.getNode(i));
			}
			else
			{
				for (int i = 0; i < src.size(); i++)
					target.add(atomProcessor.process(srcMsg, (IAtomNode) src.getNode(i),
							(MsgSchemaPO) msgStruct.getTreeNodeValue(), esb2rcv, null,
							path + '[' + i + ']', null));
			}
			return;
		}
		// 2. �����Ԫ��Ϊ�ṹ����
		for (int i = 0; i < src.size(); i++)
		{
			INode n = src.getNode(i);
			// 2012-06-12, ��Щ������ķ���<result></result>�Ŀձ�ǩ����schema�淶ΪAM��
			if (!(n instanceof ICompositeNode)) continue;
			ICompositeNode cnode = (ICompositeNode) n;
			ICompositeNode cn = targetCN.newInstance();

			// chenjs 2012-09-03 �����еĽڵ��ж��Ƿ��Զ����ˣ�
			if (!autoFilterUndefinedTag) cn.set(cnode);
			cn.setExt(cnode.getExt());

			translateMap(msgStruct, srcMsg, cnode, cn, esb2rcv, autoFilterUndefinedTag,
					path + '[' + i + ']');
			// 700 2013-08-22 �������ڵ��еĸ��ӽڵ��ǿձ�ǩ���򲻼�������ڵ�
			if (cn.size() > 0 || (cn.getExt() != null && cn.getExt().size() > 0)) target.add(cn);
		}
	}

	protected IAtomProcessor atomProcessor = new DefaultAtomProcessor();

	public void setAtomProcessor(IAtomProcessor atomProcessor)
	{
		this.atomProcessor = atomProcessor;
	}
}
