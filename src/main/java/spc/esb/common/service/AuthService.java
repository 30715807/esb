package spc.esb.common.service;

import spc.esb.data.IMessage;

/**
 * ������Ȩ����ӿ�
 * 
 * @author sunqian at 2010-5-24
 * 
 */
public interface AuthService
{
	// ����Message����Ȩ״̬
	public boolean isAuth(IMessage msg) throws Exception;
}
