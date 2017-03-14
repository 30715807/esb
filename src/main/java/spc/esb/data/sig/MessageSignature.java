package spc.esb.data.sig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.esb.data.MessageSchema;
import spc.esb.data.util.MessageUtil;
import spc.esb.security.Signature;

/**
 * ����ǩ����
 * 
 * @author spc
 * 
 */
public class MessageSignature
{
	/**
	 * ESB ����ǩ��
	 * 
	 * @param nodeCd
	 *            ���սڵ���
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	public byte[] sig(String node, byte[] msg) throws Exception
	{
		byte[] body = MessageUtil.getBody(msg);
		String strSig = getSignature(node).sign(node, body, null);
		return MessageUtil.addSignature(msg, strSig.getBytes());
	}

	/**
	 * ESB ������ǩ
	 * 
	 * @param nodeCd
	 *            ���ͷ��ڵ��
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	public boolean unsig(String node, byte[] msg) throws Exception
	{
		byte[] sigs = MessageUtil.getSignature(msg);
		if (sigs == null)
		{
			log.warn("canot find signature in msg:" + node);
			return false;
		}
		return getSignature(node).unsign(node, new String(sigs), MessageUtil.getBody(msg), null);
	}

	public Signature getSignature(String node)
	{
		return sig;
	}

	protected MessageSchema msgSchema; // ��֤��Դ��������������ǩ���ı��Ľṹ��Ϣ
	protected Signature sig; // Ĭ�ϵ�ǩ���ӿ�
	public final static Logger log = LoggerFactory.getLogger(MessageSignature.class);

	public void setSig(Signature sig)
	{
		this.sig = sig;
	}

	public void setMsgSchema(MessageSchema msgSchema)
	{
		this.msgSchema = msgSchema;
	}
}
