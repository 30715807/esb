package spc.esb.data.iso8583;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.esb.data.fixmsg.IAtom2FixedLenConverter;
import spc.webos.constant.Common;
import spc.webos.util.StringX;
import spc.webos.util.charset.EBCDUtil;

/**
 * ���ǵ���8583�����У�ֵ���ܴ��ڲ�ͬ���ͱ�ʾ�������������ͣ�������Ҫ���룬���ܹ涨û��С���㡣�ַ���������Ҫbcdת��
 * 
 * @author chenjs
 * 
 */
public class DefaultFieldConverter implements IFieldConverter
{
	public byte[] vlen(Field f) throws Exception
	{
		String strLen = StringX.int2str(String.valueOf(f.buf.length), f.vlen);
		return bcd ? EBCDUtil.gbk2bcd(strLen) : strLen.getBytes();
	}

	/**
	 * ��һ���ֶα�ɶ�����, ���������ݲ������Ƕ���LLVar & LLLVar��2 & 3�ĳ�����Ϣ. ������Ϣ���ݶ���������ʵ�ʳ��ȴ��ʱ��д
	 * 
	 * @param f
	 * @return
	 */
	public void pack(Field f) throws Exception
	{
		str2bytes(f);
		if (f.type > 0) f.len = f.buf.length;
	}

	public int unpack(byte[] buf, int offset, Field f) throws Exception
	{
		f.offset = offset;
		int flen = f.len;
		byte[] lenbuf = null;
		byte[] vbuf = null;
		if (f.vlen > 0)
		{ // ��ǰ���ڱ䳤��֧��Nλ�䳤
			lenbuf = new byte[f.vlen];
			System.arraycopy(buf, offset, lenbuf, 0, f.vlen);
			flen = getVaryLen(lenbuf, f);
		}
		vbuf = new byte[flen];
		System.arraycopy(buf, offset + f.vlen, vbuf, 0, flen);
		f.buf = vbuf;
		bytes2str(f);
		return offset + flen + f.vlen;
	}

	// ���������ֽڵõ���ɳ���
	protected int getVaryLen(byte[] len, Field f)
	{
		return Integer.parseInt(bcd ? EBCDUtil.bcd2gbk(len) : new String(len));
	}

	protected void bytes2str(Field f) throws Exception
	{
		String strValue = null;
		switch (f.type)
		{
			case Field.TYPE_B:
				strValue = new String(StringX.encodeBase64(f.buf));
				break;
			default:
				strValue = new String(f.buf, charset);
				if (f.schema != null && !StringX.nullity(f.schema.getExt1())
						&& f.schema.getExt1().indexOf(IAtom2FixedLenConverter.TRIM) >= 0) strValue = strValue
						.trim();
				break;
		}
		f.setValue(strValue);
	}

	protected void str2bytes(Field f) throws Exception
	{
		switch (f.type)
		{
			case Field.TYPE_N: // 752, f.len>0 ��Ϊf.len<=0
				f.buf = f.len <= 0 ? f.value.getBytes() : StringX.int2str(f.value, f.len).getBytes();
				break;
			case Field.TYPE_MONEY:
				if (StringX.nullity(f.value)) f.buf = "0.00".getBytes();
				else f.buf = f.len > 0 ? f.value.getBytes() : StringX.float2str(f.value, f.len,
						f.decimal, withDot).getBytes();
				break;
			case Field.TYPE_B:
				f.buf = StringX.decodeBase64(f.value.getBytes());
				break;
			default:
				f.buf = f.value.getBytes(charset);
				break;
		}
		// ����ǹ̶����ȣ����ֽ�����������ո�
		if (f.vlen == 0 && f.buf.length < f.len)
		{
			byte[] buf = new byte[f.len];
			for (int i = 0; i < buf.length; i++)
				buf[i] = ' ';
			System.arraycopy(f.buf, 0, buf, 0, f.buf.length);
			f.buf = buf;
		}
	}

	protected Logger log = LoggerFactory.getLogger(getClass());
	protected String charset = Common.CHARSET_UTF8;
	protected boolean bcd; // �Ƿ�bcd����
	protected boolean withDot = true; // ��������Ƿ�����С����.

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	public boolean isBcd()
	{
		return bcd;
	}

	public void setBcd(boolean bcd)
	{
		this.bcd = bcd;
	}

	public boolean isWithDot()
	{
		return withDot;
	}

	public void setWithDot(boolean withDot)
	{
		this.withDot = withDot;
	}
}
