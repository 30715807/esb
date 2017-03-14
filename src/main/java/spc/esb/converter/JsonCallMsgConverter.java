package spc.esb.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.data.converter.NodeConverterFactory;
import spc.esb.model.MessagePO;
import spc.esb.model.MsgSchemaPO;
import spc.esb.model.ServicePO;
import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;
import spc.webos.util.tree.TreeNode;

/**
 * ��esb soap��ɷ���json call�ı��ĸ�ʽ
 * 
 * @author chenjs
 *
 */
public class JsonCallMsgConverter extends AbstractMsgConverter
{
	@Override
	public IMessage deserialize(byte[] buf, IMessage msg) throws Exception
	{ // ��ʱ��reqmsg�Ѿ���һ��Ӧ����ͷ
		Object body = msg.getInLocal(RESPONSE_BODY_KEY);
		log.debug("msg:{}, ret:{}", msg, body);
		// ԭ���������Ϣ
		ServicePO servicePO = msgDefService.getService(msg.getRefMsgCd());
		MessagePO repMsgPO = msgDefService.getMessage(servicePO.getRepMsgCd());
		log.info("{}'s ext1:{}", servicePO.getRepMsgCd(), repMsgPO.getExt1());
		if (!StringX.nullity(repMsgPO.getExt1())) msg.setInResponse(repMsgPO.getExt1(), body);
		else msg.setBody((ICompositeNode) NodeConverterFactory.getInstance().unpack(body, null));

		msg.setMsgCd(servicePO.getRepMsgCd());
		return msg;
	}

	@Override
	public byte[] serialize(IMessage msg) throws Exception
	{
		TreeNode schema = msgDefService.getMsgSchema(msg.getMsgCd());
		List<Object> args = new ArrayList<>();
		for (TreeNode item : schema.getChildren())
		{
			MsgSchemaPO s = (MsgSchemaPO) item.getTreeNodeValue();
			INode n = msg.getBody().find(s.getEsbName());
			log.debug("find {} in body: {}", s.getEsbName(), n);
			args.add(n);
		}
		log.info("args:{}", args.size());

		Map soap = msg.getTransaction().plainMapValue();
		soap.put(JsonUtil.TAG_BODY, args);

		msg.setInLocal(REQUEST_SOAP_KEY, soap);
		return null; // jvm call ������ܲ�תΪΪbytes
	}

	public static String REQUEST_SOAP_KEY = "JC_REQUEST_SOAP";
	public static String RESPONSE_BODY_KEY = "JC_RESPONSE_BODY";
}
