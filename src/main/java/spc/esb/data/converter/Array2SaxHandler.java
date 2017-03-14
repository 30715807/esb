package spc.esb.data.converter;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import spc.esb.data.ArrayNode;
import spc.esb.data.AtomNode;
import spc.esb.data.CompositeNode;
import spc.esb.data.IArrayNode;
import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.INode;
import spc.webos.util.StringX;

/**
 * ��Ҫ��������ʾ�� <array>a</array><array>b</array>���͵�xml����
 * �����arrayֻ����һ�Σ����������Ҫ���Ĺ淶��֧�ֲ���֪����array����map�ṹ
 * 
 * @author spc
 * 
 */
public class Array2SaxHandler extends SaxHandler
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
	}

	static final ThreadLocal STATUS = new ThreadLocal();
	static final ThreadLocal ROOT = new ThreadLocal();
	static final Logger log = LoggerFactory.getLogger(Array2SaxHandler.class);
	static final Array2SaxHandler handler = new Array2SaxHandler();

	private Array2SaxHandler()
	{
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
		ROOT.set(null);
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
		String s = new String(v, start, length);
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

	public void startElement(String uri, String localname, String qName, Attributes attr)
			throws SAXException
	{
		Status status = (Status) STATUS.get();
		if (status.isFirst)
		{
			status.isFirst = false;
			return;
		}
		INode node = null;
		String type = attr.getValue(INode.TYPE_TAG);
		if (type == null || type.length() == 0) type = String.valueOf(INode.TYPE_STRING);
		status.t = (byte) type.charAt(0);
		if (status.t == '8') status.t = INode.TYPE_STRING;
		if (status.t == INode.TYPE_MAP) node = new CompositeNode();
		else if (status.t == INode.TYPE_ARRAY) node = new ArrayNode();
		else node = new AtomNode(StringX.EMPTY_STRING); // Ĭ��Ϊ�ձ�ǩ���ַ���

		for (int i = 0; i < attr.getLength(); i++)
		{
			String key = attr.getQName(i);
			if (key.equals(INode.TYPE_TAG)) continue; // t,x
			// if (key.equals(INode.TYPE_TAG) || key.equals(INode.TYPE_XTAG)
			// || key.equals(INode.SIZE_TAG)) continue; // t,x,size
			node.setExt(key, attr.getValue(i));
		}
		// if (qName.equals("name")) System.out.println(current.getClass() + ","
		// + node.getClass() + "," + qName + "," + type + "," + (char) t);
		String name = qName;
		String x = attr.getValue(INode.TYPE_XTAG); // ��ǩ�Ƿ�base64ת��
		byte[] buf = name.getBytes();
		if (x != null) name = new String(buf, 1, buf.length - 1);
		if (status.current instanceof ICompositeNode)
		{
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
		{
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

			pnode = new CompositeNode();
			((CompositeNode) pnode).set(name, node); // �޸�һ��ԭ�ӽڵ����ڸ��ڵ���Ϣ

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
		else ((IArrayNode) status.parent).set(status.index, node);
	}

	// ��һ���ڵ���뵽���ڵ��У����ĳ���ڵ��Ѿ����뵽���ڵ���ʱ�����ð�ԭ�ڵ�ϲ����鴦��
	// ���ص�ǰ�ڵ��ʱ������parent
	INode add2compositenode(ICompositeNode parent, String name, INode current)
	{
		INode first = parent.getNode(name);
		System.out.println(name + ":" + (first == null));
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
		log.error("Line:" + e.getLineNumber() + ",Column:" + e.getColumnNumber(), e);
	}
}
