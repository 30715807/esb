package spc.esb.data.iso8583;

import spc.esb.data.IMessage;
import spc.webos.util.tree.TreeNode;

public interface IISO8583MsgSchema
{
	/**
	 * ����8583���ĵ��ֽ����飬ͨ���˻�ȡ�ñ��Ķ�Ӧ��xml���Ľṹ
	 * 
	 * @param buf
	 * @param offset
	 * @param len
	 * @param reqmsg
	 * @return
	 */
	TreeNode getMsgSchema(byte[] buf, int offset, int len, IMessage reqmsg);
}
