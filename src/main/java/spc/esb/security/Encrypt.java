package spc.esb.security;

import java.util.HashMap;
import java.util.Map;

/**
 * ��PIN�ֶν��мӽ���
 * 
 * @author spc
 * 
 */
public interface Encrypt {
	// �������ʺŶ�pin�ֶν���ת����
	byte[] translatePinWith2AccNo(String pin, String nodeCd1, String nodeCd2, String acc1, String acc2)
			throws Exception;

	// ����
	byte[] encode(String nodeCd, byte[] src, Map<String, Object> attribute) throws Exception;

	// ����
	byte[] decode(String nodeCd, byte[] src, Map<String, Object> attribute) throws Exception;

	Map<String, Encrypt> ENCRYPTS = new HashMap<>();
}
