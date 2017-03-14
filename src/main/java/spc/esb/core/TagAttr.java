package spc.esb.core;

import spc.webos.util.StringX;

/**
 * ESB_msgschema����ֶ�����
 * X 1λ(1��ʾ���ֶε���չ����) 
 * X 2λ(1��ʾ�������ֶΣ���־�ǲ������)
 * X 3λ(1��ʾ��BCD���ĵ�P����(Pack))
 * X 4λ(0��ʾ����¼���ݿ�biz�ֶΣ�1-9��ʾ��¼����Ӧ��biz�ֶ�)
 * X 5λ(1��ʾ���ֶ���Ҫ����Ԫ������Ϣ)
 * X 6λ(1��ʾ���ֶ������ٱ��Ĺ淶���������������ֶΣ���ֵ������Ĭ��ֵ�����Ǻ�̨�ӹ�ֵ)    
 * @author chenjs
 * 
 */
public class TagAttr
{
	String attr = "00000000000000000000000000000000";

	public TagAttr()
	{
	}

	public TagAttr(String attr)
	{
		if (!StringX.nullity(attr)) this.attr = (attr.length() >= 32 ? attr : attr + this.attr);
	}

	public boolean isTagExtAttr()
	{
		return attr.charAt(0) == '1';
	}

	public boolean isSensitive()
	{
		return attr.charAt(1) == '1';
	}

	public boolean isBcdPack()
	{
		return attr.charAt(2) == '1';
	}
	
	public String getBizNo()
	{
		return String.valueOf(attr.charAt(3));
	}
	
	public boolean isMetaData()
	{
		return attr.charAt(4) == '1';
	}
	
	public boolean isHidden()
	{
		return attr.charAt(5) == '1';
	}

	public String toString()
	{
		return attr;
	}
}
