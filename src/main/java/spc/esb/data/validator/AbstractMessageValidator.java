package spc.esb.data.validator;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import spc.esb.constant.ESBRetCode;
import spc.esb.data.IArrayNode;
import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.MessageSchema;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;
import spc.esb.model.MsgValidatorPO;
import spc.webos.constant.AppRetCode;
import spc.webos.util.StringX;
import spc.webos.util.tree.TreeNode;

public abstract class AbstractMessageValidator implements IMessageValidator
{
	protected static Logger log = LoggerFactory.getLogger(AbstractMessageValidator.class);
	protected String name;
	protected MessageSchema source;
	protected boolean segment = false; // �����Ƿ�ȫ�ṹ�����ǲ��ֽṹ

	public MessageErrors validate(IMessage msg, MessageErrors errors)
	{
		try
		{
			String[] validators = getValidators(msg);
			for (int i = 0; validators != null && i < validators.length; i++)
			{
				TreeNode schema = source.getMsgSchema(validators[i]);
				if (schema == null)
				{
					log.warn("canot find schema for " + validators[i]);
					continue;
				}
				if (!segment) validate(msg, schema, null, msg.getTransaction(), errors);
				else
				{
					if (msg.isRequestMsg()) validate(msg, schema, null, msg.getRequest(), errors);
					else validate(msg, schema, null, msg.getResponse(), errors);
				}
			}

			// 1. �Ա����������֤
			validateComposite(msg, errors);
		}
		catch (Exception e)
		{
			log.warn("validate:" + msg.getMsgSn() + '/' + msg.getMsgCd(), e);
		}
		return errors;
	}

	abstract String[] getValidators(IMessage msg);

	/**
	 * ��֤����ֶ�
	 * 
	 * @param msg
	 * @param errors
	 */
	public void validateComposite(IMessage msg, MessageErrors errors)
	{
		// 1. �Ա����������֤
		List validators = source.getMsgValidator(msg.getMsgCd());
		if (validators == null) return;
		for (int i = 0; i < validators.size(); i++)
		{
			MsgValidatorPO msgValidatorVO = (MsgValidatorPO) validators.get(i);
			if (StringX.nullity(msgValidatorVO.getValidator())) continue;
			ICompositeValidator cvalidator = (ICompositeValidator) ICompositeValidator.VALIDATOR
					.get(msgValidatorVO.getValidator());
			if (cvalidator != null && !"1".equals(msgValidatorVO.getDisable()))
				cvalidator.validate(msg, msgValidatorVO, errors);
		}
	}

	public static void validate(IMessage msg, TreeNode tree, String field, ICompositeNode cnode,
			Errors errors) throws Exception
	{
		if (tree == null || cnode == null) return;
		List items = tree.getChildren();
		if (items == null) return;
		if (!StringX.nullity(field)) errors.pushNestedPath(field);
		for (int i = 0; i < items.size(); i++)
		{
			TreeNode item = (TreeNode) items.get(i);
			MsgSchemaPO struct = (MsgSchemaPO) item.getTreeNodeValue();
			String name = struct.getEsbName();
			INode node = cnode.find(name);
			byte type = (byte) struct.getFtyp().charAt(0);

			if (type == INode.TYPE_UNDEFINED) continue; // 703_20140318
														// δ�������ͱ�ǩ��У��

			// added by spc. 2010-11-6 check msg node type
			if ((node instanceof IArrayNode) && (type != INode.TYPE_ARRAY))
			{
				errors.rejectValue(name, ESBRetCode.MSG_UNMATCH_TYPE, new Object[] { name,
						String.valueOf((char) INode.TYPE_ARRAY), String.valueOf((char) type) },
						null);
				continue;
			}
			if ((node instanceof ICompositeNode)
					&& !(type == INode.TYPE_MAP || type == INode.TYPE_ARRAY))
			{
				errors.rejectValue(name, ESBRetCode.MSG_UNMATCH_TYPE, new Object[] { name,
						String.valueOf((char) INode.TYPE_MAP), String.valueOf((char) type) }, null);
				continue;
			}
			// added by spc. 2010-11-6 end
			// 0. ���ڵ��Ƿ�ɿ�
			// �ڵ�Ϊ�ջ���
			// modified by chenjs 2011-07-20, ��equalsIgnoreCase��Ϊequals, ������Сmͨ��
			if (node == null && MessageSchema.MO_MUST.equals(struct.getOptional()))
			{
				Utils.require(name, node, item, errors);
				continue;
			}
			else if (MessageSchema.MO_optional.equals(struct.getOptional())
					&& (node == null || (node instanceof IAtomNode
							&& ((IAtomNode) node).stringValue().length() == 0)))
			{ // �����Сo������������ǩΪ�ձ�ǩ�򵱴˱�ǩ������
				continue;
			}
			// 1. ���ڵ�������Ƿ�ƥ��, �°汨�Ĺ涨array �� map�ڵ㶼������atom�ڵ�ı�ǩ����
			// if (((byte) struct.getFtyp().charAt(0) == INode.TYPE_MAP &&
			// !(node instanceof ICompositeNode)))
			// {
			// errors.rejectValue(fdesc, AppRetCode.MSG_UNMATCH_TYPE, new
			// Object[] { field,
			// String.valueOf((char) node.type()),
			// String.valueOf((char) INode.TYPE_STRING) }, null);
			// continue;
			// }
			if (type == INode.TYPE_MAP) node = cnode.findComposite(name, null);
			else if (type == INode.TYPE_ARRAY) node = cnode.findArray(name, null);

			// 2. ��֤��ǰ�ڵ����������, ԭ�ӽڵ�����֤ԭ������
			if (node == null || !validate(msg, item, name, node, errors)) continue; // ��֤��ǰ���Ľڵ�

			// modified by spc 2009-12-12 ֻ��ͨ�������������ж���Map�ڵ㻹��Array�ڵ�,
			// ��ΪArray�ڵ������һ��Ԫ��
			if (type == INode.TYPE_MAP) validate(msg, item, name, (ICompositeNode) node, errors);
			else if (type == INode.TYPE_ARRAY) validate(msg, item, name, (IArrayNode) node, errors);
			// if (node instanceof ICompositeNode) validate(msg, item, name,
			// (ICompositeNode) node,
			// errors);
			// else if (node instanceof IArrayNode) validate(msg, item, name,
			// (IArrayNode) node,
			// errors);
		}
		if (!StringX.nullity(field)) errors.popNestedPath();
	}

	public static void validate(IMessage msg, TreeNode tree, String field, IArrayNode anode,
			Errors errors) throws Exception
	{
		List items = tree.getChildren();
		// 1. �����е�Ԫ��Ϊԭ������, ���ǽṹ����
		if (items == null || items.size() == 0)
		{ // ����Ԫ��Ϊԭ�ӽڵ�
			errors.pushNestedPath(field);
			for (int i = 0; i < anode.size(); i++)
				validate(msg, tree, String.valueOf(i), anode.getNode(i), errors);
			errors.popNestedPath();
			return;
		}
		errors.pushNestedPath(field);
		// 2. �����Ԫ��Ϊ�ṹ����
		for (int i = 0; i < anode.size(); i++)
		{
			ICompositeNode cnode = (ICompositeNode) anode.getNode(i);
			validate(msg, tree, String.valueOf(i), cnode, errors);
		}
		errors.popNestedPath();
	}

	// validate node, ����true��ʾ�˽ڵ���Լ�����֤�������ܼ�����֤
	public static boolean validate(IMessage msg, TreeNode tnode, String field, INode node,
			Errors errors) throws Exception
	{
		MsgSchemaPO schemaVO = (MsgSchemaPO) tnode.getTreeNodeValue();
		String fdesc = StringX.nullity(schemaVO.getFdesc()) ? field
				: field + '|' + schemaVO.getFdesc();

		// 1. ���ڵ��ȡֵ��Χ�Ƿ���ȷ
		String strMin = schemaVO.getMin();
		String strMax = schemaVO.getMax();
		byte ftyp = INode.TYPE_UNDEFINED;
		if (!StringX.nullity(schemaVO.getFtyp())) ftyp = (byte) schemaVO.getFtyp().charAt(0);
		if (StringX.nullity(schemaVO.getFtyp()) || ftyp == INode.TYPE_ARRAY
				|| ftyp == INode.TYPE_MAP || ftyp == INode.TYPE_STRING)
		{ // ����ڵ�������,�ṹ,�ַ�������
			int min = 0;
			int max = Integer.MAX_VALUE;
			if (!StringX.nullity(strMin)) min = Integer.parseInt(strMin);
			if (!StringX.nullity(strMax)) max = Integer.parseInt(strMax);
			Utils.length(msg, fdesc, node, tnode, errors, min, max);
		}
		else if (ftyp == INode.TYPE_INT || ftyp == INode.TYPE_LONG || ftyp == INode.TYPE_DOUBLE)
		{ // ������������ͣ�����С����λ��
			BigDecimal min = new BigDecimal(String.valueOf(Double.MIN_VALUE));
			BigDecimal max = new BigDecimal(String.valueOf(Double.MAX_VALUE));
			if (!StringX.nullity(strMin)) min = new BigDecimal(strMin);
			if (!StringX.nullity(strMax)) max = new BigDecimal(strMax);
			int decimal = schemaVO.getDeci() == null ? 0 : schemaVO.getDeci().intValue();
			Utils.number(msg, fdesc, node, tnode, errors, min, max, decimal);
		}

		// 2. ��֤������ʽ(��ԭ�ӽڵ㲻��������������ʽ)
		if (!StringX.nullity(schemaVO.getPattern()))
		{
			if (log.isDebugEnabled()) log.debug("pattern:" + schemaVO.getPattern());
			Utils.regex(msg, field, node, tnode, errors, Pattern.compile(schemaVO.getPattern()));
		}

		// 3.��֤�Զ������֤��
		String validators = schemaVO.getValidator();
		if (!StringX.nullity(validators))
		{
			if (log.isDebugEnabled()) log.debug("validators:" + validators);
			GroupNodeValidator gnv = new GroupNodeValidator();
			gnv.setValidatorNames(validators);
			gnv.init();
			gnv.validate(msg, field, node, tnode, errors);
		}

		// 4. ��֤�����ֵ�
		if (!StringX.nullity(schemaVO.getDict()))
		{
			if (log.isDebugEnabled()) log.debug("dict:" + schemaVO.getDict());
			Utils.dict(msg, field, node, tnode, errors, schemaVO.getDict());
		}

		return true;
	}

	public void init() throws Exception
	{
		if (name != null)
		{
			if (VALIDATOR.containsKey(name)) log.warn("VALIDATOR(" + name + ") has been exist!!!");
			VALIDATOR.put(name, this);
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isSegment()
	{
		return segment;
	}

	public void setSegment(boolean segment)
	{
		this.segment = segment;
	}

	public void setSource(MessageSchema source)
	{
		this.source = source;
	}
}
