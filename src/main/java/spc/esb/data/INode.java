package spc.esb.data;

import java.util.Map;

/**
 * ICompsiteData��������еķǼ����ͽڵ㣬String,int,long,float,double,byte[]Ϊ������
 * IArray,ICompsiteDataΪ�Ǽ�����
 * 
 * @author spc
 * 
 */
public interface INode extends java.io.Serializable
{
	// INode parent(); // ���׽ڵ㡣������Map���ͣ�Ҳ������Array����

	// Object getValue();
	byte type();

	boolean isNull();

	Object getExt(String key);

	Map getExt();

	void setExt(Map ext);

	void setExt(String key, Object value);
	
	Object removeExt(String key);

	// added by chenjs 2011-12-20 Ϊÿ���ڵ�����ns����
	void setNs(String ns);

	String getNs();

	// void toXml(OutputStream os, String tag, int level) throws IOException;

	public static final byte TYPE_STRING = 'S'; // Ӣ���ַ���
	public static final byte TYPE_CNSTR = 'C'; // ���ܺ������ĵ��ַ���
	public static final byte TYPE_BOOL = 'b'; // boolean
	public static final byte TYPE_INT = 'I'; // ����
	public static final byte TYPE_LONG = 'L'; // ������
	public static final byte TYPE_DOUBLE = 'D'; // ����
	public static final byte TYPE_BYTE = 'B'; // bytes
	public static final byte TYPE_ARRAY = 'A'; // ����
	public static final byte TYPE_MAP = 'M'; // map����
	public static final byte TYPE_UNDEFINED = 'U'; // δ���������

	public static final String TYPE_TAG = "t"; // �ڵ���������
	public static final String TYPE_XTAG = "x"; // ��ǩ�Ƿ񾭹����⴦��������X�ַ�ͷ
	public static final String ARRAY_TAG = "v"; // �����ʾ
//	public static final String SIZE_TAG = "size"; // ���鳤�� // modified by chenjs 2012-01-10 ȡ��������ΪĬ������
	public static final String NULL_TAG = "null"; // null��ǩ
}
