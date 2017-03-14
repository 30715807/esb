package spc.esb.data.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.esb.data.MessageSchema;
import spc.esb.model.MsgSchemaPO;
import spc.webos.persistence.IPersistence;
import spc.webos.service.BaseService;
import spc.webos.util.tree.TreeNode;

public class DBMsgValidatorSource extends BaseService implements MessageSchema
{
	public final static Logger log = LoggerFactory.getLogger(DBMsgValidatorSource.class);
	protected String msgCdSqlId;
	protected String msgStructSqlId;
	protected Map msgStructMap;

	public void init() throws Exception
	{
		msgStructMap = loadMsgStruct();
	}

	public Map loadMsgStruct()
	{
		log.debug("start to load MSGSTRUCT...");
		Map msgStructMap = new HashMap();
		Map param = new HashMap();
		List msgList = (List) persistence.execute(msgCdSqlId, param);// ��ѯ���ı��
		List structList = (List) persistence.execute(msgStructSqlId, param);// ��ѯ���������ĵ�ȫ�����Ľṹ��Ϣ

		List temp = new ArrayList();
		// ��ÿ��������������treeNode
		for (int i = 0; i < msgList.size(); i++)
		{
			String msgCd = msgList.get(i).toString();
			msgStructMap.put(msgCd, createTreeNode(structList, msgCd, temp));
		}
		if (log.isInfoEnabled()) log.info("msgStructMap: " + msgStructMap.size());
		return msgStructMap;
	}

	// ΪmsgStructs�е�ÿ��msgCd���ļ������νṹ
	private TreeNode createTreeNode(List msgStructs, String msgCd, List temp)
	{
		temp.clear();
		MsgSchemaPO msgStructVO = new MsgSchemaPO();
		msgStructVO.setSeq(new Integer(0));
		temp.add(msgStructVO);
		for (int i = 0; i < msgStructs.size(); i++)
		{
			msgStructVO = (MsgSchemaPO) msgStructs.get(i);
			if (msgStructVO.getMsgCd().equals(msgCd)) temp.add(msgStructVO);
		}
		TreeNode root = new TreeNode();
		root.createTree(temp);
		return root;
	}

	public TreeNode getMsgSchema(String msgCd)
	{
		return (TreeNode) msgStructMap.get(msgCd);
	}

	public List getMsgValidator(String name)
	{
		return null;
	}

	public void setMsgCdSqlId(String msgCdSqlId)
	{
		this.msgCdSqlId = msgCdSqlId;
	}

	public void setMsgStructSqlId(String msgStructSqlId)
	{
		this.msgStructSqlId = msgStructSqlId;
	}

	public void setPersistence(IPersistence persistence)
	{
		this.persistence = persistence;
	}
}
