package spc.esb.common.service;

import spc.esb.data.IMessage;

/**
 * ǩ������ӿ�
 * 
 * 
 * 
 */
public interface SignatureService
{
	// ǩ������������ǩ�����ǩ����
	// msg��ʾ��ǰ����
	// node��ʾ���ո�ǩ����ϵͳ��sig������Ҫ��ȡnode��Ҫ���ժҪ������ǩ������������ǩ��
	// srcBytes��ʾĬ�ϵĴ�ǩ������
	String sig(IMessage msg, String nodeApp, byte[] srcBytes) throws Exception;

	// ��ǩ���������غ�ǩ�Ƿ�ɹ�
	// msg��ʾ��ǰ����
	// node��ʾ�����ǩ����ϵͳ��unsig������Ҫ��ȡnode��ʹ�õ�ժҪ������ǩ����������ɺ�ǩ
	// srcBytes��ʾĬ�ϵĴ���ǩ����
	// signature��ʾ����ǩ��ǩ����
	boolean unsig(IMessage msg, String nodeApp, byte[] srcBytes, String signature) throws Exception;

	// final static String SIG_MODE_BODY = "0"; // ����xml��body��ǩ��body��ǩ��ǩ��ģʽ
	// final static String SIG_MODE_ELEMENT = "1"; // ���ھ���xml�����������е�ǩ��Ҫ��ǩ��

	boolean isUnsig(IMessage msg); // �ж��Ƿ���Ҫ��ǩ�����ݱ������Ժ�ext/unsign�ڵ��ж�
}
