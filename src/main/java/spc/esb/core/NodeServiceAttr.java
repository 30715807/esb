package spc.esb.core;

import spc.webos.util.StringX;

/**
 * ���������ϵ���� 
 * X 1λ(0��ʾδ��Ȩ���ʣ�1��ʾ����Ȩ)
 * X 1λ(1��ʾ�Զ�����Ϊ���˱�ǩ)
 * X 1λ(1��ʾУ�鱨����)
 * 
 * @author chenjs
 * 
 */
public class NodeServiceAttr
{
	String attr = "10000000000000000000000000000000"; // Ĭ�ϵ�һλ��ʾ��Ȩ����

	public NodeServiceAttr()
	{
	}

	public NodeServiceAttr(String attr)
	{
		if (!StringX.nullity(attr)) this.attr = (attr.length() >= 32 ? attr : attr + this.attr);
	}

	public boolean isAuth()
	{
		return attr.charAt(0) != '0';
	}

	public boolean isAutoFilterUndefinedTag()
	{
		return attr.charAt(1) != '0';
	}

	public boolean isValidateBody()
	{
		return attr.charAt(2) != '0';
	}
}