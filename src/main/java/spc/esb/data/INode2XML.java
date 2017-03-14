package spc.esb.data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import spc.webos.constant.Common;

/**
 * �ڵ��xml���л��ӿڣ�
 * 
 * @author spc
 * 
 */
public interface INode2XML
{
	public final static String ATTR_KEY_NO_CDATA = "NO_CDATA"; // ����xml�ַ��Ƿ����cdata
	public final static String ATTR_KEY_NO_EMPTY_TAG = "NO_EMPTY_TAG"; // �Ƿ���Կյ�arraynode��mapnode������ǩ

	public final static String ATTR_KEY_NO_NULL_TAG = "NO_NULL_TAG";
	// ��������ж���, ����compositenode�����ṹԪ��ʱ���������ַ�����ǩ
	// final static ThreadLocal NO_NULL_TAG = new ThreadLocal();
	// ��������ж����ʾtoxmlʱ,������t�������Ա�ǩ, Ĭ�ϲ�����t���Ա�ǩ
	public final static String ATTR_KEY_TYPE_TAG = "TYPE_TAG";
	// added by chenjs 2011-12-20 ���л�ʱʹ�ýڵ��Դ��������ռ�����
	public final static String USING_NODE_NS = "USING_NODE_NS";
	// final static ThreadLocal NO_TYPE_TAG = new ThreadLocal();
	// �����л���ʱ��ϣ��xml������esb���������Ͳ��Ǳ�ǩv����

	// final static ThreadLocal CHARSET = new ThreadLocal(); // �ַ���
	public final static String ATTR_KEY_CHARSET = "CHARSET";

	public final static String ATTR_KEY_CN2UTF8 = "CN2UTF8";
	// final static ThreadLocal CN2UTF8 = new ThreadLocal(); // �Ƿ�Ҫ��CN 2 UTF8

	public final static String DEFAULT_CHARSET = Common.CHARSET_UTF8; // Ĭ�ϱ��뼯

	/*
	 * esb: <array> <v>aaa</v> <v>bbb</v> </array> ������v�� �ظ���ǩ��ʽΪ��
	 * <array>aaa</array> <array>bbb</array>
	 */
	// final static ThreadLocal ARRAY_REPEAT = new ThreadLocal();
	void node2xml(OutputStream os, INode node, String ns, String tag, boolean pretty,
			ICompositeNode root, ICompositeNode parent, List path, Map attribute, int index)
			throws IOException;

	// �ṹ����xml���л�
	/**
	 * ���ĺ͵�ǰ�ڵ��ڱ����е�·��
	 */
	void map(OutputStream os, ICompositeNode cnode, String ns, String tag, boolean pretty,
			ICompositeNode root, ICompositeNode parent, List path, Map attribute, int index)
			throws IOException;

	// ��������xml���л�
	void array(OutputStream os, IArrayNode anode, String ns, String tag, boolean pretty,
			ICompositeNode root, ICompositeNode parent, List path, Map attribute, int index)
			throws IOException;

	// ԭ���������л�
	void atom(OutputStream os, IAtomNode node, String ns, String tag, boolean pretty,
			ICompositeNode root, ICompositeNode parent, List path, Map attribute, int index)
			throws IOException;
}
