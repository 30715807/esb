package spc.esb.converter;

import spc.esb.constant.ESBMsgLocalKey;
import spc.esb.data.FixedMessage;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.fixmsg.CNode2FixedLenBytesUtil;
import spc.esb.model.MessagePO;
import spc.webos.util.tree.TreeNode;

/**
 * Ĭ��xml���ĵ��������ĵ�ת������1. ���ȸ��ݱ�����Ϣ��õ�ǰ���ı�Ŷ�Ӧ�Ķ������ĵ��ܳ��� 2. �������õı��Ľṹ���б���ת��
 * 
 * @author chenjs
 * 
 */
public class FixMsgConverter extends AbstractMsgConverter
{
	public IMessage deserialize(byte[] buf, IMessage msg) throws Exception
	{
		String appMsgCd = new String(FixedMessage.read(buf, 0, msgCdStart, msgCdLen));
		String msgCd = msgDefService.getESBMsgCdByBA(appCd.toLowerCase(), appMsgCd);
		MessagePO msgVO = msgDefService.getMessage(msgCd);
		TreeNode schema = msgDefService.getMsgSchema(msgVO.getMsgCd());
		ICompositeNode cnode = CNode2FixedLenBytesUtil.unpack(buf, 0, schema, atom2FixedLen, null,
				charset);
//		ResponseAFNode.req2rep(msg);
		
		
		msg.setMsgCd(msgCd);// ��Ӧ���ı����ŵ�MsgCd�з���
		msg.setInLocal(ESBMsgLocalKey.LOCAL_ORIGINAL_REQ_BYTES, buf);
		if (ba) msg.setResponse(cnode);
		else msg.setRequest(cnode);
		return msg;
	}

	public byte[] serialize(IMessage msg) throws Exception
	{
		return pack2(msg.getMsgCd(), ba ? msg.getRequest() : msg.getResponse());
	}

	// added by chenjs 2012-01-01 ��һ��ָ�����ı�ź����ݴ����һ������
	public byte[] pack2(String esbMsgCd, ICompositeNode cnode) throws Exception
	{
		MessagePO msgVO = ba ? msgDefService.getMessage(esbMsgCd) : msgDefService.getFAMessage(
				appCd, esbMsgCd);
		TreeNode schema = ba ? msgDefService.getMsgSchema(esbMsgCd) : msgDefService
				.getMsgSchemaByFA(appCd, esbMsgCd);
		if (schema == null || msgVO == null)
		{
			log.warn("can not find msgvo or schema by: " + esbMsgCd);
			return null;
		}
		return pack2(msgVO, schema, esbMsgCd, cnode);
	}

	public byte[] pack2(MessagePO msgVO, TreeNode schema, String esbMsgCd, ICompositeNode cnode)
			throws Exception
	{
		byte[] fixmsg = new byte[msgVO.getLen().intValue()]; // ʹ�����ݿ�len�ֶ���Ϊ�����ĳ���,
		// ����ʹ��ext1�ֶ�
		FixedMessage.fillBlank(fixmsg, 0, fixmsg.length);
		CNode2FixedLenBytesUtil.pack2(cnode, fixmsg, 0, schema, atom2FixedLen, null, charset);
		if (log.isDebugEnabled()) log.debug("msgcd: " + esbMsgCd + ", fixmsg bytes:[["
				+ new String(fixmsg, charset) + "]]");
		return fixmsg;
	}

	protected int msgCdStart;
	protected int msgCdLen;

	public void setMsgCdStart(int msgCdStart)
	{
		this.msgCdStart = msgCdStart;
	}

	public void setMsgCdLen(int msgCdLen)
	{
		this.msgCdLen = msgCdLen;
	}
}
