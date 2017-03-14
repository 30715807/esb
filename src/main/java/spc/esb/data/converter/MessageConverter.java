package spc.esb.data.converter;

import spc.esb.data.IMessage;

public interface MessageConverter
{
	// IMessage deserialize(InputStream is) throws Exception;
	IMessage deserialize(byte[] buf, int offset, int len) throws Exception;

	IMessage deserialize(byte[] buf, int offset, int len, IMessage reqmsg) throws Exception;

	IMessage deserialize(byte[] buf) throws Exception;

	IMessage deserialize(byte[] buf, IMessage reqmsg) throws Exception;

	byte[] serialize(IMessage msg) throws Exception;

	// ���л�����������ͣ�����http response contentType
	String getContentType();

	// void serialize(IMessage msg, OutputStream os) throws Exception;
}
