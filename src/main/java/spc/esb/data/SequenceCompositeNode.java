package spc.esb.data;

import java.util.Iterator;
import java.util.LinkedList;

import spc.esb.data.converter.NodeConverterFactory;
import spc.webos.util.StringX;

/**
 * ���ĳЩ����ĺ�̨ϵͳ,��Ҫ��xml���ĸ����������Ա�ڵ���Ҫ˳��ĳ�����xml�ĵ���, ���ౣ֤ÿ�������Ԫ�ص�˳����ǽ�������xml�ַ�����˳��
 * 
 * @author spc
 * 
 */
public class SequenceCompositeNode extends CompositeNode
{
	protected LinkedList keys = new LinkedList(); // �������ظ���key���뵽map
	private static final long serialVersionUID = 1L;

	public Object set(String name, Object value)
	{
		if (value == null) return null;
		int index = name.lastIndexOf('/');
		SequenceCompositeNode parent = this;
		if (index >= 0)
		{
			parent = (SequenceCompositeNode) create(name.substring(0, index),
					new SequenceCompositeNode());
			name = name.substring(index + 1);
		}
		return parent.put(name, NodeConverterFactory.getInstance().unpack(value, null));
	}

	public ICompositeNode newInstance()
	{
		return new SequenceCompositeNode();
	}

	public Iterator keys()
	{
		Iterator ks = super.keySet().iterator();
		while (ks.hasNext())
		{ // added by chenjs 2011-11-01 ��map�к��У���keys��û�еļ��뵽����
			String key = ks.next().toString();
			if (!keys.contains(key)) keys.add(key);
		}
		return keys.iterator();
	}

	public Object put(Object key, Object o)
	{
		// if (keys.contains(key)) keys.remove(key);
		// keys.add(key); // ���ĳ����ֵ���ظ����뵽map�У� �����һ�εĴ���Ϊ׼
		// modified by chenjs 2011-12-09 ������������key�򱣳�ԭ��key���ڵ�λ�ã�Ҳ�����޸�ֵ���ı����
		if (!keys.contains(key)) keys.add(key);
		super.put(key, o);
		return o;
	}

	public void remove(String key)
	{
		if (key == null) return;
		super.remove(key);
		keys.remove(key);
	}

	public Object putAsFirstChild(String key, Object o)
	{
		if (key == null) return null;
		super.remove(key);
		keys.remove(key);
		keys.addFirst(key);
		super.put(key, o);
		return o;
	}

	public Object putAsLastChild(String key, Object o)
	{
		if (key == null) return null;
		super.remove(key);
		keys.remove(key);
		keys.add(key);
		super.put(key, o);
		return o;
	}

	public LinkedList getKeys()
	{
		return keys;
	}

	public void setKeys(LinkedList keys)
	{
		this.keys = keys;
	}

	public INode getNode(String name)
	{
		Object o = super.get(name);
		if (o == null || o instanceof INode) return (INode) o;
		INode node = NodeConverterFactory.getInstance().unpack(o, null);
		// set(name, node); // ����ִ�з�������� ������ȡ��ı�ԭ��˳��״̬
		return node;
	}

	public SequenceCompositeNode()
	{
	}

	public SequenceCompositeNode(ICompositeNode cnode, String keys)
	{
		this.keys = new LinkedList();
		set(cnode);
		this.keys = new LinkedList(StringX.split2list(keys, StringX.COMMA));
	}
}
