package spc.esb.data.iso8583;

import spc.esb.model.MsgSchemaPO;
import spc.webos.util.StringX;
import spc.webos.util.tree.TreeNode;

/**
 * @author chenjs
 * 
 *         ��������ַ������͵Ľ�����������Ϊһ�����๩��������д���
 */
public class Field implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	public String value = StringX.EMPTY_STRING; // �ֶ�ֵ
	public byte[] buf; // ��Ӧ8583�����еĶ���������
	public int len = 0; // �ֶγ���
	public int decimal = 2; // ��������С����λ��
	public int vlen = 0; // �ɱ䳤�� 2L 3L, LEN_LLVAR, LEN_LLLVAR
	public int type = 0; // �ֶ�����
	public int offset = 0; // ���ݰ��е�ƫ��λ��
	public int no; // λͼλ��
	public boolean enabled = false;
	public MsgSchemaPO schema;
	public TreeNode tree; // 751

	public static final int TYPE_A = 1; // ��ĸ�ַ�
	public static final int TYPE_N = 2; // ����
	public static final int TYPE_S = 3; // �����ַ�
	public static final int TYPE_AN = 4; // ��ĸ������
	public static final int TYPE_AS = 5; // ��ĸ����������
	public static final int TYPE_NS = 6; // ���ֺ������ַ�
	public static final int TYPE_B = 7; // ������
	public static final int TYPE_Z = 8; // �ŵ�
	public static final int TYPE_X = 9; // 'C''D'�ֱ��ʾ����
	public static final int TYPE_MONEY = 10; // ���
	public static final int TYPE_LL = 11; // ����
	public static final int TYPE_CMONEY = 12; // ������Ľ��
	public static final int TYPE_NN = 13; // ���֣�������ȥ��ǰ��0

	public static final int LEN_FIX = 0; // �̶���
	public static final int LEN_LLVAR = 2; // ���ֽڱ䳤
	public static final int LEN_LLLVAR = 3; // ���ֽڱ䳤

	public Field()
	{
	}

	public Field(TreeNode tree, MsgSchemaPO schema)
	{
		this.tree = tree;
		this.schema = schema;
		if (schema.getDeci() != null) decimal = schema.getDeci().intValue();
		int[] iso8583 = StringX.split2ints(schema.getIso8583(), "|");
		// modified by chenjs 2011-11-10
		no =  iso8583[0]; // Integer.parseInt(schema.getRcvName());
		type = iso8583[1];
		vlen = iso8583[2];
		len = iso8583[3];
	}

	public Field(int fno, int ftype, int flen, int vflen)
	{
		this(fno, ftype, flen, vflen, 2, null);
	}

	public Field(int fno, int ftype, int flen, int vflen, int decimal)
	{
		this(fno, ftype, flen, vflen, decimal, null);
	}

	public Field(int fno, int ftype, int flen, int vflen, String fv)
	{
		this(fno, ftype, flen, vflen, 2, fv);
	}

	public Field(int fno, int ftype, int flen, int vflen, int decimal, String fv)
	{
		no = fno;
		len = flen;
		type = ftype;
		vlen = vflen;
		value = fv;
		this.decimal = decimal;
		if (!StringX.nullity(value)) enabled = true;
	}

	public String toString()
	{
		return "{no:" + no + ", len:" + len + ", vlen:" + vlen + ", type:" + type + ", offset:"
				+ offset + ", enabled:" + enabled + ", value:'" + value + "'}";
	}

	public int getNo()
	{
		return no;
	}

	public void setNo(int no)
	{
		this.no = no;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isFixedLen()
	{
		return vlen == 0;
	}

	public byte[] getBuf()
	{
		return buf;
	}

	public void setBuf(byte[] buf)
	{
		this.buf = buf;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
		if (!StringX.nullity(value)) enabled = true;
	}

	/**
	 * �����Ƿ���Ч
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * ���
	 * 
	 */
	public void clear()
	{
		enabled = false;
		value = StringX.EMPTY_STRING;
	}

	/**
	 * ����ֵ�����䳤
	 */
	public int length()
	{
		return len + vlen;
	}

	public int getLen()
	{
		return len;
	}

	public void setLen(int len)
	{
		this.len = len;
	}

	public int getVlen()
	{
		return vlen;
	}

	public void setVlen(int vlen)
	{
		this.vlen = vlen;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public int getDecimal()
	{
		return decimal;
	}

	public void setDecimal(int decimal)
	{
		this.decimal = decimal;
	}
}
