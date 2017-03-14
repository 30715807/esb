package spc.esb.converter;

import spc.esb.data.AtomNode;
import spc.esb.data.CompositeNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.iso8583.BitMap;
import spc.esb.data.iso8583.ISO8583Util;
import spc.esb.data.xml.XMLConverterUtil;
import spc.webos.util.StringX;
import spc.webos.util.tree.TreeNode;

/**
 * ��ʹ��schema��̶��ı��ı��(��������ʹ��CUPS8583)�����8583 128����ȫ��������Ȼ��ʹ�þ����ҵ���ı�Ž���xml - xml'
 * ����
 * 
 * @author chenjs
 * 
 */
public class ISO8583MsgConverter extends AbstractMsgConverter
{
	/**
	 * ���ڴ�������һ��������MBģʽ, msg������BAģʽʱ��ԭreqmsg, ��FAģʽʱ�������ĳ�ʼֵ, Ҳ���ǽ���MBʱ�ı��Ķ���
	 */
	public IMessage deserialize(byte[] iso8583, IMessage msg) throws Exception
	{
		if (log.isDebugEnabled())
		{
			log.debug("iso8583:[[" + (iso8583 == null ? "null" : StringX.base64(iso8583)));
			BitMap bitmap = new BitMap(iso8583, offset);
			// ʹ��ƫ��������bitmap, ������bitmapֵ
			log.debug("offset:" + offset + ", bitmap:" + bitmap.getValidFields());
		}

		// ʹ��ƫ������8583���Ľ��н���, ����ϵͳ�ض���ȫ���ý�������,
		// ȫ����8583schema��Ϣ����esb_msgschema���У����FAʹ��Ҳ��Ҫ��ȫ����Ϣ����˱�
		ICompositeNode cnode = ISO8583Util.deserialize(iso8583MsgConverter, iso8583, offset,
				iso8583.length - offset, msg, getMsgSchema(iso8583, msg));
		if (log.isDebugEnabled()) log.debug("8583 cnode: " + cnode);

		ICompositeNode target = convertMap(msg, cnode, new CompositeNode()); // ��iso8583ȫ�ֶμ��ϸ���ESB
		// schemaӳ��ɵ�ǰ���Ĺ淶ָ���Ĺ淶
		if (ba)
		{ // �����BAģʽ������Ϊ�Ƿ���ϵͳӦ����
			req2rep(msg, cnode, target);
			msg.setResponse(target); // �����BA������������response
		}
		else msg.setRequest(target); // �����FA������������request

		// ����ESB����ͷ��Ϣ
		ICompositeNode msgHdr = msg.getHeader();
		msgHdr.apply(getESBMsgHdr(msg, cnode, target));
		msg.setHeader(msgHdr);

		if (log.isDebugEnabled()) log.debug("after deserialize msg :" + msg.toXml(true));

		return msg;
	}

	// ��õ�ǰ8583���ĵ�schema�� Ĭ��ʹ��ȫ�����õ�schema�ṹ
	protected TreeNode getMsgSchema(byte[] iso8583, IMessage msg)
	{
		return msgDefService.getMsgSchema(schema8583);
	}

	// ��BAͬ��ģʽʱ���������ı��һ��Ӧ����
	protected void req2rep(IMessage msg, ICompositeNode cnode, ICompositeNode target)
	{
//		ResponseAFNode.req2rep(msg); // �任���ģ����òο���Ϣ
	}

	// �õ�ESB����ͷ��Ϣ, msgCd, seqNb, sndDt, sndTm, sndMbrCd etc.
	protected ICompositeNode getESBMsgHdr(IMessage msg, ICompositeNode cnode, ICompositeNode target)
	{
		ICompositeNode msgHdr = new CompositeNode();
		String appMsgCd = cnode.findAtom(appMsgCdNm, new AtomNode(StringX.EMPTY_STRING))
				.stringValue();
		String esbMsgCd = ba ? msgDefService.getESBMsgCdByBA(appCd.toLowerCase(), appMsgCd)
				: msgDefService.getESBMsgCdByFA(appCd, appMsgCd);
		msgHdr.set(IMessage.TAG_HEADER_MSG_CD, esbMsgCd); // �õ�ESB���ı��
		return msgHdr;
	}

	// Ȼ��ʹ���ض����ı�Ž�ȫ��ӳ�䵽�Ӽ���, ʹ��xml - xml' ģʽ����
	protected ICompositeNode convertMap(IMessage msg, ICompositeNode cnode, ICompositeNode target)
			throws Exception
	{
		String appMsgCd = cnode.findAtom(appMsgCdNm, new AtomNode(StringX.EMPTY_STRING))
				.stringValue(); // 8583�������ṩ�ĵ�ǰ���ĵĽ�����
		String esbMsgCd = ba ? msgDefService.getESBMsgCdByBA(appCd.toLowerCase(), appMsgCd)
				: msgDefService.getESBMsgCdByFA(appCd, appMsgCd); // �ҵ����ڵ�ESB�ı��ı��
		TreeNode schema = ba ? msgDefService.getMsgSchema(esbMsgCd) : msgDefService
				.getMsgSchemaByFA(appCd, esbMsgCd); // �õ�esb���ı��8583schema�ṹ��Ϣ
		if (schema == null)
		{ // ���ĳ����û�����������Ӽ�����ʹ��ȫ���������
			if (log.isInfoEnabled()) log.info("schema is null by " + msg.getMsgCd());
			return cnode;
		}
		XMLConverterUtil.convertMap(schema, msg, cnode, target, false, atomProcessor,
				nodeProcessor, StringX.EMPTY_STRING, rcvIgnore, emptyIgnore, schemaTargetXMLTag);
		return target;
	}

	// ���л�ʱֱ��ʹ��ESB���ı�ŵ�schema��Ϣ����������ȫ���ṹ��Ϣ
	public byte[] serialize(IMessage msg) throws Exception
	{
		if (log.isDebugEnabled()) log.debug("msg:\n" + msg.toXml(true));
		// �õ�8583schema�ṹ��Ϣ
		TreeNode schema = ba ? msgDefService.getMsgSchema(msg.getMsgCd()) : msgDefService
				.getMsgSchemaByFA(appCd, msg.getMsgCd());
		if (schema == null)
		{ // ���ĳ����û�����������Ӽ�����ʹ��ȫ���������
			if (log.isInfoEnabled()) log.info("schema is null by " + msg.getMsgCd());
			schema = msgDefService.getMsgSchema(schema8583);
		}
		ICompositeNode cnode = ba ? msg.getRequest() : msg.getResponse(); // �����Ҫ���л��ĸ��ӽڵ���Ϣ
		byte[] buf = ISO8583Util.serialize(iso8583MsgConverter, cnode, schema);
		if (log.isDebugEnabled()) log.debug("after serialize buf.base64: "
				+ new String(StringX.encodeBase64(buf)));
		return buf;
	}

	protected String appMsgCdNm; // ȫ��8583�б�ʾappmsgcd�ֶε�nameֵ
	protected String schema8583; // ȫ��8583���ı�ţ���������ʹ��CUPS8583
	protected int offset = 0; // һ��8583ǰ���м����ֽڱ�ʾͷ

	public String getSchema8583()
	{
		return schema8583;
	}

	public void setSchema8583(String schema8583)
	{
		this.schema8583 = schema8583;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public void setAppMsgCdNm(String appMsgCdNm)
	{
		this.appMsgCdNm = appMsgCdNm;
	}
}
