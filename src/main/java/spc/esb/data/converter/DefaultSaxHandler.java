package spc.esb.data.converter;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import spc.esb.data.ArrayNode;
import spc.esb.data.AtomNode;
import spc.esb.data.IArrayNode;
import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.INode;
import spc.webos.util.StringX;

/**
 * esb���ĵ�sax handler�� Ϊ�˶��̰߳�ȫ������ѽ���ĳһxml���ĵ�״̬���뵱ǰ�̻߳�����
 * 
 * @author spc
 * 
 */
public class DefaultSaxHandler extends SaxHandler
{
	static final class Status
	{
		public Stack stack = new Stack();
		public byte t; // ����
		// public byte x; // ��ǩ�Ƿ�base64
		public boolean isFirst = true;
		public INode current;
		public INode parent; // ���ڵ�
		public String name; // ��ǰ�ڵ��ڸ��ڵ�������
		public int index; // ������ڵ���ArrayNode�ڵ�.���¼��ArrayNode�е�λ��.
		// public long start;
	}

	static final ThreadLocal STATUS = new ThreadLocal();
	static final ThreadLocal ROOT = new ThreadLocal();
	static final Logger log = LoggerFactory.getLogger(DefaultSaxHandler.class);
	static final DefaultSaxHandler handler = new DefaultSaxHandler();
	protected String tagType = INode.TYPE_TAG;
	protected String tagTypeMap = String.valueOf(INode.TYPE_MAP);
	protected String tagTypeList = String.valueOf(INode.TYPE_ARRAY);
	protected String tagList = INode.ARRAY_TAG;
	protected boolean ignoreAttr; // ignore��������

	public DefaultSaxHandler()
	{
	}

	public DefaultSaxHandler(String tagType, String tagTypeMap, String tagTypeList, String tagList,
			boolean ignoreAttr)
	{
		this.tagType = tagType;
		this.tagTypeMap = tagTypeMap;
		this.tagTypeList = tagTypeList;
		this.tagList = tagList;
		this.ignoreAttr = ignoreAttr;
	}

	public static SaxHandler getInstance()
	{
		return handler;
	}

	public void setRoot(ICompositeNode root)
	{
		ROOT.set(root);
	}

	public ICompositeNode root()
	{
		return (ICompositeNode) ROOT.get();
	}

	public void startDocument() throws SAXException
	{
		Status status = new Status();
		status.current = (ICompositeNode) ROOT.get();
		status.stack.clear();
		status.stack.push(status.current);
		status.isFirst = true;
		STATUS.set(status);
		// status.start = System.currentTimeMillis();
	}

	public void endDocument() throws SAXException
	{
		// Status status = (Status) STATUS.get();
		// System.out.println((System.currentTimeMillis() - status.start));
		STATUS.set(null);
	}

	public void endElement(String uri, String localname, String qName) throws SAXException
	{
		Status status = (Status) STATUS.get();
		if (status.stack.empty()) return;
		status.stack.pop();
		if (status.stack.empty()) return;
		status.current = (INode) status.stack.peek();
	}

	public void characters(char[] v, int start, int length)
	{
		if (length == 0) return;
		Status status = (Status) STATUS.get();

		String s = StringX.utf82str(new String(v, start, length));
		// StringX.utf82str(StringX.trim(new String(v, start, length),
		// CHAR_ARRAY));

		// if (s.trim().length() == 0) return; // ��Ч�ո����Դ���
		INode node = (INode) status.current;
		// System.out.print("\nchars:" + s + "," + node.getClass());
		if (node == null || !(node instanceof AtomNode)) return;
		// System.out.print(" ..ok: ");
		Object o = ((AtomNode) node).getValue();
		if (o != null) s = o.toString() + s; // �����ı��д��ڻس�, ���������ÿ����Ϊһ���¼����д���
		((AtomNode) node).set(s, status.t);
	}

	// public void ignorableWhitespace(char[] v, int start, int length)
	// {
	// String s = new String(v, start, length);
	// System.out.println("ignorableWhitespace:" + s + ",");
	// }

	protected void setAttrs(INode node, Attributes attr)
	{
		if (ignoreAttr) return;
		for (int i = 0; i < attr.getLength(); i++)
		{
			String key = attr.getQName(i);
//			System.out.println("k: " + key);
			if (key.equals(tagType) || key.equals(INode.TYPE_XTAG)) continue; // t,x
			// if (key.equals(tagType) || key.equals(INode.TYPE_XTAG)
			// || key.equals(INode.SIZE_TAG)) continue; // t,x,size
			node.setExt(key, attr.getValue(i));
		}
	}

	public void startElement(String uri, String localname, String qName, Attributes attr)
			throws SAXException
	{
		Status status = (Status) STATUS.get();
		if (status.isFirst)
		{
			status.isFirst = false;
			setAttrs((INode) ROOT.get(), attr);
			return;
		}
		INode node = null;
		String type = attr.getValue(tagType);
		if (StringX.nullity(type)) type = String.valueOf(INode.TYPE_STRING);

		// status.t = (byte) type.charAt(0);
		// modified by chenjs, ������Ӧ���ͣ�����Ӧtrc��xml�淶 2010-10-20
		if (type.equalsIgnoreCase(tagTypeMap)) status.t = INode.TYPE_MAP;
		else if (type.equalsIgnoreCase(tagTypeList)) status.t = INode.TYPE_ARRAY;
		else status.t = (byte) type.charAt(0);

		if (status.t == '8') status.t = INode.TYPE_STRING;
		// modifed by chenjs 2011-12-02 ���������ͬʵ����cnode
		if (status.t == INode.TYPE_MAP) node = root().newInstance(); // new
																		// CompositeNode();
		else if (status.t == INode.TYPE_ARRAY) node = new ArrayNode();
		else node = new AtomNode(StringX.EMPTY_STRING); // Ĭ��Ϊ�ձ�ǩ���ַ���

		setAttrs(node, attr);
		// if (!ignoreAttr)
		// {
		// for (int i = 0; i < attr.getLength(); i++)
		// {
		// String key = attr.getQName(i);
		// System.out.println("k: " + key);
		// if (key.equals(tagType) || key.equals(INode.TYPE_XTAG)) continue; //
		// t,x
		// // if (key.equals(tagType) || key.equals(INode.TYPE_XTAG)
		// // || key.equals(INode.SIZE_TAG)) continue; // t,x,size
		// node.setExt(key, attr.getValue(i));
		// }
		// }
		// if (qName.equals("name")) System.out.println(current.getClass() + ","
		// + node.getClass() + "," + qName + "," + type + "," + (char) t);
		node.setNs(ns(qName)); // �����ǰ��ǩ��ns���¼
		String name = ignoreNS(qName);
		String x = attr.getValue(INode.TYPE_XTAG); // ��ǩ�Ƿ�base64ת��
		byte[] buf = name.getBytes();
		if (x != null) name = new String(buf, 1, buf.length - 1);
		if (status.current instanceof ICompositeNode)
		{ // �����ǰ���׽ڵ��ǽṹ����
			// ��Ҫ�Է�ESB�����ʾ��ʽ�ı��ģ���������֧�֡� modified by spc 20090601
			/*
			 * esb: <array> <v>aaa</v> <v>bbb</v> </array> ������v�� �ظ���ǩ��ʽΪ��
			 * <array>aaa</array> <array>bbb</array>
			 */
			ICompositeNode parent = (ICompositeNode) status.current;
			add2compositenode(parent, name, node);

			status.parent = status.current;
			status.name = name;
			status.current = node;
		}
		else if (status.current instanceof IArrayNode)
		{ // �����ǰ���׽ڵ�������ڵ�
			((IArrayNode) status.current).add(node);
			status.parent = status.current;
			status.index = ((IArrayNode) status.current).size() - 1;
			status.current = node;
		}
		else
		{ // �޸ĸ��ڵ�Ϊcompositenode�ڵ�
			// System.out.println(name + ":" +
			// status.name+":"+status.stack.peek().getClass());
			INode pnode = null;
			if (name.equalsIgnoreCase(tagList))
			{ // �����ǰ������ڵ��µ���Ԫ��
				pnode = new ArrayNode(); // �޸ĸ��ڵ�Ϊ����ڵ�
				((ArrayNode) pnode).add(node); // �޸�һ��ԭ�ӽڵ����ڸ��ڵ���Ϣ
				status.index = 0;
			}
			else
			{ // �����ǰ�Ƿ��Ͻڵ��µ���Ԫ��
				// modifed by chenjs 2011-12-02 ���������ͬʵ����cnode
				pnode = root().newInstance(); // new CompositeNode();
				// System.out.println("pnode: "+pnode.getClass());
				((ICompositeNode) pnode).set(name, node); // �޸�һ��ԭ�ӽڵ����ڸ��ڵ���Ϣ
			}
			pnode.setExt(status.current.getExt());
			pnode.setNs(status.current.getNs()); // added by chenjs 2011-12-20
													// �����ռ�Ҳ���ӽ���
			changeParent(status, pnode);
			// if (status.parent instanceof ICompositeNode) ((ICompositeNode)
			// status.parent)
			// .set(status.name, pnode);
			// else ((IArrayNode) status.parent).set(status.index, pnode);
			// System.out.println("p: "+status.parent);

			status.stack.pop();
			status.stack.push(pnode);

			status.name = name;
			status.current = node;
			status.parent = pnode;
		}
		status.stack.push(node);
	}

	// ����ÿ����ǩ��namespace
	String ignoreNS(String name)
	{
		int index = name.lastIndexOf(':');
		return index >= 0 ? name.substring(index + 1) : name;
	}

	String ns(String name)
	{
		int index = name.lastIndexOf(':');
		return index >= 0 ? name.substring(0, index) : null;
	}

	// �ı丸�ڵ�����, ��ԭ�����͸�Ϊcompositenode or arraynode
	void changeParent(Status status, INode node)
	{
		if (status.parent instanceof ICompositeNode)
		{
			INode first = ((ICompositeNode) status.parent).getNode(status.name);
			if (first instanceof IAtomNode) ((ICompositeNode) status.parent).set(status.name, node);
			else if (first instanceof IArrayNode) ((IArrayNode) first).set(
					((IArrayNode) first).size() - 1, node);
			else System.err
					.println("cannot change parent node, cos parent in parent is valid node.."
							+ first.getClass());
		}
		else
		{
			IArrayNode pnode = (IArrayNode) status.parent;
			// System.out.println(pnode.size() + ":" + node);
			// if (anode.size()==0)anode.add(node);
			pnode.set(status.index, node);
		}
	}

	// ��һ���ڵ���뵽���ڵ��У����ĳ���ڵ��Ѿ����뵽���ڵ���ʱ�����ð�ԭ�ڵ�ϲ����鴦��
	// ���ص�ǰ�ڵ��ʱ������parent
	INode add2compositenode(ICompositeNode parent, String name, INode current)
	{
		INode first = parent.getNode(name);
		// System.out.println(name + ":" + (first == null));
		if (first != null)
		{ // ����xml��������ͬһ�㼶���Ѿ�������һ��ͬ����ǩ�� ��ʱӦ�����Ϊ����
			if (first instanceof IArrayNode)
			{
				((IArrayNode) first).add(current);
				return first;
			}
			// �޸ĸ��׽ڵ��е�Ԫ�أ�ʹ֮��Ϊһ���������ͣ�
			// Note: ��ʱ��û����Ԫ�����͵ĵ�һ�����;����������ͣ���ʱ�߼�������⣬��������Ƕ�ױȽ���
			IArrayNode arr = new ArrayNode();
			arr.add(first);
			arr.add(current);
			parent.set(name, arr);
			return arr;
		}
		parent.set(name, current);
		return parent;
	}

	public void error(SAXParseException e) throws SAXException
	{
		log.error("Line:" + e.getLineNumber() + ",Column:" + e.getColumnNumber(), e);
	}

	public void fatalError(SAXParseException e) throws SAXException
	{
		log.error("Line:" + e.getLineNumber() + ",Column:" + e.getColumnNumber(), e);
	}

	public void warning(SAXParseException e) throws SAXException
	{
		log.warn("Line:" + e.getLineNumber() + ",Column:" + e.getColumnNumber(), e);
	}

	public String getTagType()
	{
		return tagType;
	}

	public void setTagType(String tagType)
	{
		this.tagType = tagType;
	}

	public String getTagTypeMap()
	{
		return tagTypeMap;
	}

	public void setTagTypeMap(String tagTypeMap)
	{
		this.tagTypeMap = tagTypeMap;
	}

	public String getTagTypeList()
	{
		return tagTypeList;
	}

	public void setTagTypeList(String tagTypeList)
	{
		this.tagTypeList = tagTypeList;
	}

	public String getTagList()
	{
		return tagList;
	}

	public void setTagList(String tagList)
	{
		this.tagList = tagList;
	}

	public void setIgnoreAttr(boolean ignoreAttr)
	{
		this.ignoreAttr = ignoreAttr;
	}
}
