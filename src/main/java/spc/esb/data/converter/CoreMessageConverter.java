package spc.esb.data.converter;

import java.util.HashMap;
import java.util.Map;

import spc.esb.data.IMessage;

/**
 * ���ں���, ��mb���ĵ��õ�������ת��������
 * 
 * @author chenjs
 * 
 */
public interface CoreMessageConverter
{
	void app2esb(IMessage msg, boolean request) throws Exception;

	// ���غ��ϵͳ�Ķ����ƣ������ESB��׼xml�򷵻�NULL�����򷵻�ʵ�ʷ������ϵͳ��bytes
	byte[] esb2app(IMessage msg, boolean request) throws Exception;

	Map<String, CoreMessageConverter> CORE_MSG_CVTERS = new HashMap<>(); // ������к���������
}
