package spc.esb.core;

import spc.webos.util.StringX;

/**
 * Ӧ�ýڵ�/����������
 * X 1λ(���ȼ���0-9) 
 * X 2λ(0��ʾ����bodyǩ����1��ʾ����Ҫ��ǩ��) 
 * X 3λ( 0:��ǩ������ǩ;1:��ǩ������ǩ;2:����ǩ������ǩ;3. ����ǩҲ����ǩ)
 * X 4λ(0: ESB�ڲ�ISO20022���ģ� 1: ��ʾ����������SOAP����1.0, 2: SOAP 2.0,  8��json, 9: ��ʾ�Զ��屨��(����, ISO8583 etc.)) 
 * X 5λ(1��ʾ������ʹ��JMSЭ����ã�Ĭ��ΪMQ) 
 * X 6λ(0: utf-8, 1:EBCD, 2:GBK)
 * X 7λ(PIN�ֶ�����, 0��ʾ����û��PIN�ֶΣ�1��ʾDES)  
 * X 8λ(�Ƿ��쳣�˳���0: �����˳���Ӧ���˳�(Ҳ��������ǰ��Ĭ�����)
                      1: �����˳���Ӧ���˳���
                      2: �����˳���Ӧ���˳���
                      3. �����˳���Ӧ���˳���
* X 9λ(1: ǩ������MD5����ժҪ��2: ��SHA-1����ժҪ��������ʾ����ǩ�����ݽ���ժҪֱ�Ӽ���ǩ������)
* X 10λ(��Ϊ�������Ƿ���Ч:0 ����Ч��1: ������Ч��2����ʾ������Ч��3:��ʾ����Ч)
* X 11λ(ͬ��Ӧ����ϢREP.NBS����Ϣ��Чʱ�䣬ƽ̨Ĭ��Ϊ10s: 0��ʾʹ��ƽ̨Ĭ�ϣ�1-8ÿ����λΪ1s, 9��ʾ60s)
* X 12λ(1��ʾ�Զ�����δ����ı�ǩ, ���0��ʾʹ��ƽ̨esb_config����)
* X 13λ(1��ʾ�ڵ���Ϊ����ʱ��Ӧ���Ĳ���Ҫ����request��ǩ��Ϣ)
* X 14λ(1��ʾ�ڵ���Ϊ����ʱ��Ӧ���Ĳ���Ҫ����header/ext��ǩ��Ϣ)
* X 15λ(1��ʾ�ڵ�ֻ�ܽ���header/msg/ext��Ϊ�ַ�����ǩ�����Ǹ��ӱ�ǩ)
* X 16λ()
* X 17λ(���ڱ�ʾ��Ϊǰ������ʱ, ��ȺIDλ��������Ӧ��������A1000000000)
* X 18λ(1���ڱ�ʾ����ϵͳ��Ҫ��replyToQ����header/ext)
* X 19λ(1���ڱ�ʾϵͳ��ESBͨѶ��esb xml����ʹ����3DES�����)
 * @author chenjs
 * 
 */
public class NodeAttr
{
	String appAttr = "00000000000000000000000000000000";

	public NodeAttr()
	{
	}

	public NodeAttr(String appAttr)
	{
		if (!StringX.nullity(appAttr)) this.appAttr = (appAttr.length() >= 32 ? appAttr : appAttr
				+ this.appAttr);
	}

	public int priority()
	{
		return Integer.parseInt(String.valueOf(appAttr.charAt(0)));
	}

	public boolean isBodySig()
	{
		return appAttr.charAt(1) == '0';
	}

	public boolean isElementSig()
	{
		return appAttr.charAt(1) == '1';
	}

	public String getSigMode()
	{
		return String.valueOf(appAttr.charAt(1));
	}

	public boolean isNotUnsig()
	{
		return appAttr.charAt(2) == '1' || appAttr.charAt(2) == '3';
	}

	public boolean isNotSig()
	{
		return appAttr.charAt(2) == '2' || appAttr.charAt(2) == '3';
	}

	public boolean isSOAP()
	{
		return appAttr.charAt(3) == '1' || appAttr.charAt(3) == '2';
	}
	
	// ֧�ֲ�ͬ�汾��soap�淶2013-03-26
	public String getSOAPVersion()
	{
		return String.valueOf(appAttr.charAt(3));
	}
	
	// json��ʽ
	public boolean isJSON()
	{
		return appAttr.charAt(3) == '8';
	}
	
	// �û��Զ��屨�Ĺ淶
	public boolean isUserDefined()
	{
		return appAttr.charAt(3) == '9';
	}

	public boolean isJMS()
	{
		return appAttr.charAt(4) == '1';
	}

	public boolean isUTF8()
	{
		return appAttr.charAt(5) == '0';
	}

	public boolean isEBCD()
	{
		return appAttr.charAt(5) == '1';
	}

	public boolean isGBK()
	{
		return appAttr.charAt(5) == '2';
	}

	public String getCharset()
	{
		return String.valueOf(appAttr.charAt(5));
	}

	public boolean isDesPin()
	{
		return appAttr.charAt(6) == '1';
	}

	public String getDesPin()
	{
		return String.valueOf(appAttr.charAt(6));
	}

	public boolean isRequestExExit()
	{
		return appAttr.charAt(7) == '0' || appAttr.charAt(7) == '2';
	}

	public boolean isResponseExExit()
	{
		return appAttr.charAt(7) == '2' || appAttr.charAt(7) == '3';
	}

	public String getSigDigestAlg()
	{
		switch (appAttr.charAt(8))
		{
			case '1':
				return "MD5";
			case '2':
				return "SHA-1";
		}
		return StringX.EMPTY_STRING;
	}

	public boolean isUnvalidChannel()
	{
		return (appAttr.charAt(9) == '1' || appAttr.charAt(9) == '3');
	}

	public boolean isUnvalidServer()
	{
		return (appAttr.charAt(9) == '2' || appAttr.charAt(9) == '3');
	}

	public int getSynRepExpireSeconds()
	{
		int s = appAttr.charAt(10) - '0';
		if (s >= 0 && s < 7) return s;
		if (s == 7) return 20;
		if (s == 8) return 30;
		return 60;
	}

	public boolean isAutoFilterUndefinedTag()
	{
		return appAttr.charAt(11) == '1';
	}

	public boolean isNoRequestTag()
	{
		return appAttr.charAt(12) == '1';
	}

	public boolean isNoExtTag()
	{
		return appAttr.charAt(13) == '1';
	}

	public boolean isExt2Str()
	{ // 500, 1��ʾ����ֻ�����ַ���ext���ͣ�0��ʾ��������
		return appAttr.charAt(14) == '1';
	}

	public int getClusterIdLen()
	{
		if (appAttr.charAt(16) == '0') return 2;
		return appAttr.charAt(16) - '0';
	}
	
	public boolean isReplyToQInExt()
	{
		return appAttr.charAt(17) != '0';
	}
	
	public boolean isBodyEncryption()
	{
		return appAttr.charAt(18) != '0';
	}
}
