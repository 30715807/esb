package spc.esb.security;

import java.util.HashMap;
import java.util.Map;

/**
 * ǩ��/��ǩ���ӿڡ�
 * 
 * @author spc
 * 
 */
public interface Signature
{
	// ����ǩ���� ������ʱ���̨����ϵͳ��xml���Ľṹ��esb�ı��Ľṹ��һ�£����Խӿڲ���cnodeģʽ
	String sign(String nodeCd, byte[] content, Map<String, Object> attribute) throws Exception;

	boolean unsign(String nodeCd, String sign, byte[] content, Map<String, Object> attribute)
			throws Exception;

	Map<String, Signature> SIGS = new HashMap<>();
}
