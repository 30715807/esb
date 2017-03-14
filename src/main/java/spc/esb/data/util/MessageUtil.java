package spc.esb.data.util;

import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.converter.SOAPConverter;
import spc.webos.util.KMP;
import spc.webos.util.StringX;

/**
 * ���Ķ����Ʋ��������࣬ ��Ҫ���ڼ���ǩ���ֶκ���ȡǩ���ֶ�
 * 
 * @author spc
 * 
 */
public class MessageUtil
{
	public static ICompositeNode getCNodeHeader(byte[] msg) throws Exception
	{
		return SOAPConverter.getInstance().deserialize2composite(getHeader(msg));
	}

	/**
	 * �ӱ����л��ͷheader��ǩ(��header��ǩ)
	 * 
	 * @param msg
	 * @return
	 */
	public static byte[] getHeader(byte[] msg)
	{
		// �Ȳ���body��ǩ��β��
		int end = lastIndexOf(msg, msg.length, endTag(IMessage.TAG_BODY));
		int start = indexOf(msg, end, startTag(IMessage.TAG_HEADER));
		// header��ǩû�г�����body��ǩ����
		if (start < 0) start = indexOf(msg, startTag(IMessage.TAG_HEADER));
		end = indexOf(msg, start, endTag(IMessage.TAG_HEADER));

		return getContent(msg, start, end, IMessage.TAG_HEADER.length(), true);
	}

	public static byte[] getSOAPHeader(byte[] msg)
	{ // soap����header���������bodyǰ��
		byte[] startHeader = ":Header>".getBytes();
		int start = indexOf(msg, startHeader);
		StringBuffer ns = new StringBuffer(); // ���ҵ�ǰnamespace
		for (int i = start - 1; i > 0; i--)
		{
			if (msg[i] == '<') break;
			ns.append((char) msg[i]);
		}
		ns.reverse();
		String tag = ns + ":Header";
		start -= ns.length() + 1;

		return getContent(msg, start, indexOf(msg, start, endTag(tag)), tag.length(), true);
	}

	/**
	 * ���ڱ�����ǩ���ֶ�Ϊbody(��body��ǩ)
	 * 
	 * @param msg
	 * @return
	 */
	public static byte[] getBody(byte[] msg)
	{
		int start = indexOf(msg, 0, startTag(IMessage.TAG_BODY));
		if (start < 0) return null;
		int end = lastIndexOf(msg, msg.length, endTag(IMessage.TAG_BODY));
		if (start >= end) return null;
		return getContent(msg, start, end, IMessage.TAG_BODY.length(), true);
	}

	public static byte[] getOriginalBytes(byte[] msg)
	{
		int start = lastIndexOf(msg, msg.length, startTag(IMessage.TAG_ORIGINALBYTES));
		if (start < 0) return null;
		int end = lastIndexOf(msg, msg.length, endTag(IMessage.TAG_ORIGINALBYTES));
		if (end < 0) return null;
		return getContent(msg, start, end, IMessage.TAG_ORIGINALBYTES.length(), false);
	}

	/**
	 * �ӱ����л�ȡǩ����Ϣ signature��ǩ���������body���棬������xml���ĵ�β��
	 * 
	 * @param msg
	 * @return
	 */
	public static byte[] getSignature(byte[] msg)
	{
		int start = lastIndexOf(msg, msg.length, endTag(IMessage.TAG_BODY)); // </body>��ǩ��λ��
		start = indexOf(msg, start + 7, startTag(IMessage.TAG_SIGNATURE)); // ��body��ǩ��������<signature>��ǩ
		if (start < 0) return null;
		int end = lastIndexOf(msg, msg.length, endTag(IMessage.TAG_SIGNATURE));
		if (end < 0) return null;
		return getContent(msg, start, end, IMessage.TAG_SIGNATURE.length(), false);
	}

	// ��header�»�ȡsignature��ǩ
	public static byte[] getSignature2(byte[] msg)
	{
		int start = lastIndexOf(msg, msg.length, startTag(IMessage.TAG_HEADER)); // </header>��ǩ��λ��
		start = indexOf(msg, start + 7, startTag(IMessage.TAG_SIGNATURE)); // ��header��ǩ��������<signature>��ǩ
		if (start < 0) return null;
		int end = lastIndexOf(msg, msg.length, endTag(IMessage.TAG_SIGNATURE));
		if (end < 0) return null;
		return getContent(msg, start, end, IMessage.TAG_SIGNATURE.length(), false);
	}

	/**
	 * �ӱ��Ŀ��л��һ����Ϣ
	 * 
	 * @param msg
	 *            ����
	 * @param start
	 *            ָ����ǩ����ʼλ��
	 * @param end
	 *            ָ����ǩ�Ľ�ֹλ��
	 * @param tagLen
	 *            ��ǩ����
	 * @param includeTag
	 *            �Ƿ������ǩ(��ǩ������Ϣ����Ҫ������ǩ��ǩ����Ϣ��ʱֻ��Ҫ��ȡ��������ݿ�)
	 * @return
	 */
	public static byte[] getContent(byte[] msg, int start, int end, int tagLen, boolean includeTag)
	{
		if (start >= end) return null;
		byte[] body = new byte[includeTag ? (end - start + tagLen + 3) : (end - start - tagLen - 2)];
		System.arraycopy(msg, includeTag ? start : (start + tagLen + 2), body, 0, body.length);
		return body;
	}

	/**
	 * ɾ�������е�ǩ������
	 * 
	 * @param msg
	 * @return
	 */
	public static byte[] removeSignature(byte[] msg)
	{
		return remove(msg, lastIndexOf(msg, msg.length, startTag(IMessage.TAG_SIGNATURE)),
				lastIndexOf(msg, msg.length, endTag(IMessage.TAG_SIGNATURE)),
				IMessage.TAG_SIGNATURE.length());
	}

	public static byte[] removeSignature2(byte[] msg)
	{
		return remove(msg, lastIndexOf(msg, msg.length, startTag(IMessage.TAG_SIGNATURE)),
				lastIndexOf(msg, msg.length, endTag(IMessage.TAG_SIGNATURE)),
				IMessage.TAG_SIGNATURE.length());
	}

	public static byte[] removeOriginalBytes(byte[] msg)
	{
		return remove(msg, lastIndexOf(msg, msg.length, startTag(IMessage.TAG_ORIGINALBYTES)),
				lastIndexOf(msg, msg.length, endTag(IMessage.TAG_ORIGINALBYTES)),
				IMessage.TAG_ORIGINALBYTES.length());
	}

	public static byte[] removeHeader(byte[] msg)
	{
		// �Ȳ���body��ǩ��β��
		int end = lastIndexOf(msg, msg.length, endTag(IMessage.TAG_BODY));
		int start = indexOf(msg, end, startTag(IMessage.TAG_HEADER));
		// header��ǩû�г�����body��ǩ����
		if (start < 0) start = indexOf(msg, startTag(IMessage.TAG_HEADER));
		end = indexOf(msg, start, endTag(IMessage.TAG_HEADER));

		return remove(msg, start, end, IMessage.TAG_HEADER.length());
	}

	public static byte[] removeBody(byte[] msg)
	{
		int start = indexOf(msg, 0, startTag(IMessage.TAG_BODY));
		if (start < 0) return msg;
		int end = lastIndexOf(msg, msg.length, endTag(IMessage.TAG_BODY));
		if (start >= end) return msg;

		return remove(msg, start, end, IMessage.TAG_BODY.length());
	}

	public static byte[] remove(byte[] msg, int start, int end, int tagLen)
	{
		if (start >= end) return msg;
		byte[] xml = new byte[msg.length - (end - start + tagLen + 3)];
		System.arraycopy(msg, 0, xml, 0, start);
		System.arraycopy(msg, end + tagLen + 3, xml, start, xml.length - start);
		return xml;
	}

	/**
	 * ��ǩ�����뱨������
	 * 
	 * @param msg
	 * @param sig
	 * @return
	 */
	public static byte[] addSignature(byte[] msg, byte[] sig)
	{
		msg = removeSignature(msg);
		return add(msg, lastIndexOf(msg, msg.length, endTag(IMessage.TAG_ROOT)), sig,
				IMessage.TAG_SIGNATURE);
	}

	public static byte[] addSignature2(byte[] msg, byte[] sig)
	{
		msg = removeSignature(msg);
		return add(msg, lastIndexOf(msg, msg.length, endTag(IMessage.TAG_HEADER)), sig,
				IMessage.TAG_SIGNATURE);
	}

	public static byte[] addOriginalBytes(byte[] msg, byte[] originalBytes)
	{
		msg = removeOriginalBytes(msg);
		return add(msg, lastIndexOf(msg, msg.length, endTag(IMessage.TAG_ROOT)), originalBytes,
				IMessage.TAG_ORIGINALBYTES);
	}

	public static byte[] addHeader(byte[] msg, byte[] header)
	{
		msg = removeHeader(msg);
		byte[] rootStartTag = startTag(IMessage.TAG_ROOT);
		return add(msg, indexOf(msg, rootStartTag) + rootStartTag.length, header, null);
	}

	public static byte[] addBody(byte[] msg, byte[] body)
	{
		msg = removeBody(msg);
		byte[] hdrEndTag = endTag(IMessage.TAG_HEADER);
		return add(msg, indexOf(msg, hdrEndTag) + hdrEndTag.length, body, null);
	}

	/**
	 * ��ĳһ�����ƿ�����ԭ�����е�ĳһ��ʼλ�÷��뵽ԭ����
	 * 
	 * @param msg
	 *            ԭ����
	 * @param offset
	 *            ���뵽ԭ���ĵ���ʼλ��
	 * @param content
	 *            �����ֽ�����
	 * @param tag
	 *            �������ݱ�ǩ
	 * @return
	 */
	public static byte[] add(byte[] msg, int offset, byte[] content, String tag)
	{
		byte[] xml = new byte[msg.length + content.length
				+ (StringX.nullity(tag) ? 0 : (2 * tag.length() + 5))];
		System.arraycopy(msg, 0, xml, 0, offset);
		byte[] startTag = StringX.nullity(tag) ? new byte[0] : startTag(tag);
		byte[] endTag = StringX.nullity(tag) ? new byte[0] : endTag(tag);
		System.arraycopy(startTag, 0, xml, offset, startTag.length);
		System.arraycopy(content, 0, xml, offset + startTag.length, content.length);
		System.arraycopy(endTag, 0, xml, offset + startTag.length + content.length, endTag.length);
		System.arraycopy(msg, offset, xml, xml.length - msg.length + offset, msg.length - offset);
		return xml;
	}

	public static byte[] startTag(String tag)
	{
		return ('<' + tag + '>').getBytes();
	}

	public static byte[] endTag(String tag)
	{
		return ("</" + tag + '>').getBytes();
	}

	public static int lastIndexOf(byte[] src, byte[] target)
	{
		return lastIndexOf(src, src.length, target);
	}

	public static int lastIndexOf(byte[] src, int offset, byte[] target)
	{
		// src.length - target.length;
		int index = (offset > src.length - target.length) ? src.length - target.length : offset;
		while (index >= 0)
		{
			if (match(src, index, target)) return index;
			index--;
		}
		return -1;
	}

	public static int indexOf(byte[] src, byte[] target)
	{
		return indexOf(src, 0, target);
	}

	public static int indexOf(byte[] src, int offset, byte[] target)
	{
		// 700 2013-08-01 ʹ��KMP�㷨
		return KMP.indexOf(src, offset, target);
		
//		int index = offset < 0 ? 0 : offset;
//		while (index < src.length - target.length + 1)
//		{
//			if (match(src, index, target)) return index;
//			index++;
//		}
//		return -1;
	}

	public static boolean match(byte[] src, int start, byte[] target)
	{
		int j = 0;
		for (int i = start; i < src.length && j < target.length; i++, j++)
			if (src[i] != target[j]) return false;
		return j >= target.length;
	}
	
	// ���ñ���Ӧ����Ϣ���������ı�ΪӦ����
	
}
