package spc.esb.data;

import spc.webos.util.StringX;

/**
 * ʹ�ö����Ĺ����������Բ���32λ����ͳһ���壬32λ���ֶ���˵�����£� 
 * X 1λ(��ʱʱ�䣬0ΪĬ��60��, 1-3��λΪ10�룬4-6��λΪ30�룬7-9��λΪ60��) 
 * X 1λ(������:xyz,x:1��ʾ��¼ȫ���ģ�y:��ʾ��¼03����־��z:��ʾ��¼12����־) 
 * X 1λ(0����Ҫ����, 1��Ҫ��˳���, 3��ʾ��˺�ǰ�˳�ʱδȡ����Ҫ����)
 * X 1λ(0��ʾ����ǩ��,1��ʾ��Ҫǰ��ǩ��, 2��ʾ����ϵͳ��Ҫǩ����3��ʾǰ������ͺ�̨����ϵͳ����Ҫǩ��) 
 * X 1λ(0��ʾͬ��, 1��ʾ�첽, 2��ʾ�����첽,��֧���첽תͬ��) 
 * X 1λ(0��ʾ�޷���,������ʾ���ر��ĸ���) 
 * X 1λ(0��ʾESB���ṩ����, 1��ʾESB�ṩ�������ƣ�һ��ESBֻ����Ҫ��֪ͨ�ͱ��Ľ��в�������Ҫ�ṩ������ˮ��) 
 * X 1λ(0��ʾ��ͨ���Ľ���, 1��ʾBLOB��Ϣ�ĸ�������) 
 * X 1λ(0��ʾ�����в��������ǩ, 1��ʾ����) 
 * X 1λ(0��ʾ�첽��־,1��ʾͬ����־) 
 * X 1λ(0��ͨ����, 1�㲥����) 
 * X 1λ(0��ͨ����, 1����bpel�ཻ��) 
 * X 1λ(0��ͨ����, 1Ӧ�����ʱ������Ҫ����) 
 * X 1λ(0��ͨѶ��ִ����, (1,3)��ʾ��ESBͨѶ��ִ��(2,3)��ʾ�н��ܷ�ϵͳͨѶ��ִ) 
 * X 1λ(0��ʾ��Ϣ����ҪMQ�־�, 1��ʾ��Ҫ�־û� 
 * X 1λ(1��ʾ��Ҫ������Ϣ��Ҫ��ʱ��� 
 * X 1λ(1��ʾecho���� 
 * X 1λ(��ʾ��������ȼ�0-9) 
 * X 1λ(1��ʾ��͸(��У�飬��ת��)���� 
 * X 1λ(1��ʾ�˱�����Ҫschema1 -> schema2��ת��) added by chenjs 2011-10-17 
 * X 1λ(1��ʾ�˱��ĺ���PIN�ֶ�) added by chenjs 2011-11-10 
 * X 1λ(1��ʾ�˱�����Ҫ���в������ռ�� added by chenjs 2012-01-20) 
 * X 1λ(1��ʾ���˵�schemaδ����ı�ǩ added by chenjs 2012-08-06) 
 * X 1λ000000000000 ���� 
 * X 1λ(0��ʾ����MBת��,1��ʾ��ҪMBת��) 
 * X 1λ(0��ʾ����MBǩ��,1��ʾ��Ҫ����MBǩ��)
 * 
 * @author spc
 * 
 */
public class MessageAttr
{
	String attr;
	// public final static int TIMEOUT_STEP = 30;
	public static int DEFAULT_TIMEOUT = 60;
	public static int MAX_TIMEOUT = 99999999;
	public final static char YES = '1';
	public final static char NO = '0';

	public MessageAttr(String attr)
	{
		this.attr = attr == null ? null : attr.trim();
	}

	public boolean isValid()
	{
		return !StringX.nullity(attr);
	}

	public int getTimeout()
	{
		return timeout();
	}

	public int timeout()
	{
		int timeout = Integer.parseInt(attr.substring(0, 1));
		// 2011-09-01 chenjs, 0ΪĬ��60��, 1-3��λΪ10�룬4-6��λΪ30�룬7-9��λΪ60��
		if (timeout <= 0) return DEFAULT_TIMEOUT;
		if (timeout <= 3) return timeout * 10;
		if (timeout <= 6) return timeout * 30;
		if (timeout == 9) return MAX_TIMEOUT; // 2012-02-24 �������Ϊ9���ʾ�ӽ������Ƶĳ�ʱʱ��
		return timeout * 60;
	}

	// public boolean isQueryErr()
	// { // ��ѯ���׳���ʱ��¼����
	// // return '2' == attr.charAt(1);
	// return false;
	// }

	// �Ƿ�ֻ��������־��, ��ȫ����
	public boolean isFullLog()
	{
		return (4 & (attr.charAt(1) - '0')) > 0;
	}

	// �Ƿ�ֻ��������־��, ����ȫ����
	public boolean isLog(String logPoint)
	{
		if (logPoint == "0" || logPoint == "3") return (2 & (attr.charAt(1) - '0')) > 0;
		return (1 & (attr.charAt(1) - '0')) > 0;
	}

	// ǰ�˳�ʱδȡ��Ҫ����
	public boolean isFrontReversal()
	{
		return '3' == attr.charAt(2);
	}

	public boolean isReversal()
	{
		return NO != attr.charAt(2);
	}

	public boolean isSig()
	{
		// return YES == attr.charAt(3) || '3' == attr.charAt(3);
		return NO != attr.charAt(3); // modified by spc 2011-03-08
	}

	public boolean isSndSig()
	{
		return '1' == attr.charAt(3) || '3' == attr.charAt(3);
	}

	public boolean isRcvSig()
	{
		return '2' == attr.charAt(3) || '3' == attr.charAt(3);
	}

	public String sig()
	{
		return String.valueOf(attr.charAt(3));
	}

	public String getSignature()
	{
		return String.valueOf(attr.charAt(3));
	}

	public boolean isAsyn()
	{
		return NO != attr.charAt(4);
	}

	public boolean isMustAsyn()
	{
		return '2' == attr.charAt(4);
	}

	public int getResMsgNum()
	{
		return Integer.parseInt(String.valueOf(attr.charAt(5)));
	}

	public int resMsgNum()
	{
		return Integer.parseInt(String.valueOf(attr.charAt(5)));
	}

	public boolean isRenotice()
	{
		return NO != attr.charAt(6);
	}

	public boolean isBlob()
	{
		return YES == attr.charAt(7);
	}

	// added by spc 2010.1.20
	// ��ǰ�����Ƿ��������ڵ㣬�������ݱ�ʾģʽ���ڲ���
	public boolean isContainArray()
	{
		return YES == attr.charAt(8);
	}

	// ��ǰ�����Ƿ�Ϊ�ؼ����ģ���Ҫͬ����¼���ݿ⣬��������ͨ���׵��첽��¼���ݿ�
	public boolean isInSynJournal()
	{
		return (Integer.parseInt(String.valueOf(attr.charAt(9))) & 1) > 0;
	}

	public boolean isOutSynJournal()
	{
		return (Integer.parseInt(String.valueOf(attr.charAt(9))) & 2) > 0;
	}

	// ��ǰ�����ǹ㲥����
	public boolean isBroadcast()
	{
		return YES == attr.charAt(10);
	}

	// �Ƿ��������̽���
	public boolean isBpl()
	{
		return NO != attr.charAt(11);
	}

	// �Ƿ����Ӧ��ʱ��Ҫ����
	public boolean isErrReversal()
	{
		return NO != attr.charAt(12);
	}

	// �˱����Ƿ���ͨѶ��ִ
	public boolean isReturnReceipt()
	{
		return NO != attr.charAt(13);
	}

	// �˱����Ƿ���ESBͨѶ��ִ
	public boolean isEsbReturnReceipt()
	{
		return attr.charAt(13) == '1' || attr.charAt(13) == '3';
	}

	// �˱����Ƿ��н��շ�ϵͳ��ͨѶ��ִ
	public boolean isRcvReturnReceipt()
	{
		return attr.charAt(13) == '2' || attr.charAt(13) == '3';
	}

	public boolean isPersist()
	{
		return attr.charAt(14) == YES;
	}

	public boolean isWatchTimeout()
	{
		return attr.charAt(15) == YES;
	}

	public boolean isEcho()
	{
		return attr.charAt(16) == YES;
	}

	public int priority()
	{
		return Integer.parseInt(String.valueOf(attr.charAt(17)));
	}

	public boolean isIgnoreBody()
	{
		return attr.charAt(18) == YES;
	}

	public boolean isNeedMapping()
	{
		return attr.charAt(19) == YES;
	}

	public boolean isContainPIN()
	{
		return attr.charAt(20) == YES;
	}

	public boolean isRisk()
	{
		return attr.charAt(21) != NO;
	}

	// ʵʱ���ռ��
	public boolean isRealTimeRisk()
	{
		return attr.charAt(21) == YES;
	}
	// added by spc 2010.1.20 end

	public boolean isAutoFilterUndefinedTag()
	{
		return attr.charAt(22) == YES;
	}

	// mb ��Ҫ������
	public boolean isMbConvert()
	{
		// �����ڶ�λ modified by spc at 2010.02.05 ���������ֶεĳ�����չ
		return YES == attr.charAt(attr.length() - 2); // 14
	}

	public boolean isMbSig()
	{
		// ���һλ modified by spc at 2010.02.05
		// return YES == attr.charAt(attr.length() - 1); // 15
		// modified by spc 2011-03-08
		return NO != attr.charAt(attr.length() - 1);
	}
	// mb ��Ҫ���� end

	public String toAttr()
	{
		return attr;
	}

	public String toString()
	{
		return attr;
	}
}
