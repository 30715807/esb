package spc.esb.data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * һ���ٸ��ӵĶ���Ҳ����ICompsiteData(����Map)��IArray�ͼ����ݵ����ṹǶ����ɡ� ��������Ϊ:�����ͣ�String, int,
 * float, double, long, byte[] �����������Ϊ��IArray, ICompsiteData
 * 
 * @author spc
 * 
 */
public interface ICompositeNode extends INode
{
	ICompositeNode clone();
	
	int size();

	void remove(String key);

	void remove(String[] keys);

	void removeNotIn(String[] keys);

	void removeNotIn(String[] keys, boolean ignoreCase);

	boolean dfs(INodeVisitor visitor);

	boolean dfs(INodeVisitor visitor, INode parent, String tag);

	Object putAsFirstChild(String key, Object o);

	Object putAsLastChild(String key, Object o);

	/**
	 * �ҵ���ǰICompsiteDataΪ���Ķ���ģ�path·���µĽڵ� ����ICompsiteData,IArray�ͼ�����
	 * 
	 * @param path
	 * @return
	 */
	INode find(String path);

	ICompositeNode findComposite(String path, ICompositeNode def);

	IArrayNode findArray(String path, IArrayNode def);

	IAtomNode findAtom(String path, IAtomNode def);

	/**
	 * ��ȡ������ֵ
	 * 
	 * @param curPath
	 *            ��ǰ�ڵ���message�е�·��
	 * @param path
	 *            ��ǰ�ڵ��µĲ�����
	 * @param type
	 * @param canNull
	 * @return
	 */
	INode find(String curPath, String path, byte type, boolean canNull);

	INode find(String curPath, String path, byte type, INode defaultValue);

	/**
	 * �ҵ���ǰΪ���Ķ���path·���µĽڵ㣬����ָ����java����
	 * 
	 * @param path
	 * @param target
	 * @return
	 */
	Object find(String path, Object target);

	INode findIgnoreCase(String path);

	/**
	 * ���ҽڵ��µ�·�����������������Ĭ��ֵ
	 * 
	 * @param path
	 * @param defaultValue
	 * @return
	 */
	Object lookup(String path, Object defaultValue);

	ICompositeNode create(String path);

	/**
	 * �ҵ���ǰICompsiteDataΪ���Ķ���ģ�path·���µĽڵ㣬 dataΪ������·����ICompsiteData����ʵ��
	 * 
	 * @param path
	 * @param data
	 * @return
	 */
	ICompositeNode create(String path, ICompositeNode data);

	/**
	 * �������µ�����ֵ
	 * 
	 * @param name
	 * @param value
	 */
	Object set(String name, Object value);

	/**
	 * ����һ������ֵ��cnode�ڵ��У����ԭ�������ˣ�����뵽ԭ�����������
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	Object add(String name, Object value);

	Object set(String xml);

	ICompositeNode setAll(ICompositeNode cn);

	ICompositeNode set(ICompositeNode cn);

	ICompositeNode apply(ICompositeNode cn);

	ICompositeNode apply(ICompositeNode cn, String[] keys);

	ICompositeNode apply(ICompositeNode cn, String[] keys, String[] names);

	ICompositeNode applyIf(ICompositeNode cn);

	ICompositeNode applyIf(ICompositeNode cn, String[] keys);

	ICompositeNode applyIf(ICompositeNode cn, String[] keys, String[] names);

	ICompositeNode set(Object object);

	ICompositeNode setByMapping(Object object, Object mapping);

	ICompositeNode setByAttr(Object object, Map attr);

	INode getNode(String name);

	INode getNodeIgnoreCase(String name);

	Object toObject(Object target);

	Object toObject(Object target, Map attr);

	// ���ԭʼ���󣬿�����INode,������java����
	Object get(Object key);

	Iterator keys();

	ICompositeNode newInstance();

	String toXml(String tag, boolean pretty) throws IOException;

	String toXml(String tag, boolean pretty, INode2XML node2xml) throws IOException;

	void toXml(OutputStream os, String tag, boolean pretty, INode2XML node2xml) throws IOException;

	void toXml(OutputStream os, String tag, boolean pretty, INode2XML node2xml, Map attribute)
			throws IOException;

	void toXml(OutputStream os, String ns, String tag, boolean pretty, INode2XML node2xml,
			Map attribute) throws IOException;

	byte[] toXml(String ns, String tag, boolean pretty, INode2XML node2xml, Map attribute)
			throws IOException;

	void clear();

	Map mapValue();

	Map plainMapValue();

	boolean containsKey(Object key);
	// Object clone();
}
