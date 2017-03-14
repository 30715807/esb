package spc.esb.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.esb.data.IMessage;
import spc.esb.data.converter.CoreMessageConverter;
import spc.esb.data.converter.MessageConverter;
import spc.webos.util.StringX;

/**
 * ����ͨ��ת����
 * 
 * @author chenjs
 * 
 */
public class DefaultCoreMsgConverter implements CoreMessageConverter
{
	public void app2esb(IMessage msg, boolean request) throws Exception
	{
		log.debug("app2esb...");
		long start = System.currentTimeMillis();
		byte[] buf = msg.getOriginalBytes();
		IMessage nmsg = null;
		try
		{
			nmsg = converter.deserialize(buf, msg);
		}
		catch (Exception e)
		{
			log.warn("base64:" + StringX.base64(buf) + "\nstr:" + new String(buf), e);
			throw e;
		}
		if (nmsg == null)
		{
			log.warn("nmsg is null after converter: {}, request:{}" + converter.getClass(),
					request);
			nmsg = msg;
		}
		if (nmsg != msg)
		{ // added by chenjs 2012-01-08
			// ������ܣ����deserialize����ʹ��mb
			// msg��Ϊ���Ķ��󷵻أ�Ҳ����nmsg �� msg��һ��������ʡȥcopy����
			if (request && msg.isSynCall())
			{ // ���첽ģʽ����ʱ��Ҫ��ˮ����Ϊͬ��ƥ��
				if (StringX.nullity(nmsg.getMsgCd())) nmsg.setMsgCd(msg.getMsgCd());
				if (!StringX.nullity(msg.getSeqNb())) nmsg.setSeqNb(msg.getSeqNb());
				if (!StringX.nullity(msg.getSndApp())) nmsg.setSndAppCd(msg.getSndApp());
				if (!StringX.nullity(msg.getSndDt())) nmsg.setSndDt(msg.getSndDt());
				// if (!StringX.nullity(msg.getSndTm()))
				// nmsg.setSndTm(msg.getSndTm());
			}
			nmsg.getHeader().applyIf(msg.getHeader());
			msg.setHeader(nmsg.getHeader());
			if (!StringX.nullity(nmsg.getVersion())) msg.setVersion(nmsg.getVersion());
			if (request) msg.setRequest(nmsg.getRequest());
			else msg.setResponse(nmsg.getResponse());
			// msg.setOriginalBytes(buf); modified by chenjs 2011-07-18 ע�����ô���
			if (log.isDebugEnabled()) log.debug("nmsg hdr:" + nmsg.getHeader());
		}
		log.trace("app2esb msg: {}", msg);
		log.info("app2esb request:{}, cost: {}", request, (System.currentTimeMillis() - start));
	}

	public byte[] esb2app(IMessage msg, boolean request) throws Exception
	{
		log.debug("esb2app...");
		long start = System.currentTimeMillis();
		byte[] buf = null;
		try
		{
			buf = converter.serialize(msg);
		}
		catch (Exception e)
		{
			log.warn("xml:" + msg.toXml(true), e);
			throw e;
		}
		if (removeBody) msg.setBody(null);
		if (buf == null)
		{
			log.info("esb2app no bytes, request:{}", request);
			return null;
		}
		msg.setOriginalBytes(buf);
		// if (StringX.nullity(charset))
		// {
		// if (encrypt != null) buf = encrypt.encode(msg.getRcvNodeApp(), buf,
		// null);
		// msg.setOriginalBytes(buf);
		// }
		// else msg.setOriginalBytesPlainStr(new String(buf, charset));
		// ɾ�����õ�body xml���ݣ���Ϊǰ��/��˾�ʹ��orignalbytes
		if (log.isTraceEnabled()) log.trace("originalbytes.base64:" + StringX.base64(buf));
		log.info("esb2app request:{}, removeBody: {}, cost: {}, len: {}", request, removeBody,
				(System.currentTimeMillis() - start), (buf == null ? 0 : buf.length));
		return esb2appBytes ? buf : null;
	}

	public void init() throws Exception
	{
		if (StringX.nullity(name)) return;
		if (CoreMessageConverter.CORE_MSG_CVTERS.containsKey(name))
			log.error("CMC has contained name: " + name + " !!!");
		CoreMessageConverter.CORE_MSG_CVTERS.put(name, this);
	}

	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected MessageConverter converter;
	protected boolean esb2appBytes = true; // ESB ����Ƿ��ֽ�����
	// protected String charset; // 2011-07-22, ��������originalBytes�ŷǶ���������
	// protected IEncrypt encrypt; // 2011-08-02, ���������ı��Ķ��������ݽ��мӽ���
	protected boolean removeBody = false; // ���ʹ��MB��orignalģʽ���Ƿ��MBʱɾ��body����,
											// 451, Ĭ��Ϊtrue
	protected String name;

	public void setConverter(MessageConverter converter)
	{
		this.converter = converter;
	}

	public MessageConverter getConverter()
	{
		return converter;
	}

	public boolean isEsb2appBytes()
	{
		return esb2appBytes;
	}

	public void setEsb2appBytes(boolean esb2appBytes)
	{
		this.esb2appBytes = esb2appBytes;
	}

	// public String getCharset()
	// {
	// return charset;
	// }
	//
	// public void setCharset(String charset)
	// {
	// this.charset = charset;
	// }

	public void setRemoveBody(boolean removeBody)
	{
		this.removeBody = removeBody;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
