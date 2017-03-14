package spc.esb.converter;

import java.io.ByteArrayOutputStream;

import spc.esb.data.IMessage;
import spc.esb.data.converter.MessageConverter;

/*
 * �����屨��ת����
 * chenjs
 */
public abstract class AbstractMsgConverter extends BaseMsgConverter implements
		MessageConverter
{
	// ��ȡ�����ģ���������ͬ�����첽����ģʽ�������ͬ��ģʽmsg���������ģ�������첽ģʽ��Ҫ�����ݿ�ָ�
	protected IMessage getRequestMsg(byte[] buf, IMessage msg) throws Exception
	{
		return msg;
	}

	public IMessage deserialize(byte[] buf, int offset, int len) throws Exception
	{
		return deserialize(buf, offset, len, null);
	}

	public IMessage deserialize(byte[] buf, int offset, int len, IMessage msg) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(buf, offset, len);
		return deserialize(baos.toByteArray(), msg);
	}

	public IMessage deserialize(byte[] buf) throws Exception
	{
		return deserialize(buf, null);
	}

	protected boolean inMB = true; // 2012-01-25 chenjs �Ƿ�MB��������ģʽ,
									// Ĭ��������������������ģʽ

	public boolean isInMB()
	{
		return inMB;
	}

	public void setInMB(boolean inMB)
	{
		this.inMB = inMB;
	}
}
