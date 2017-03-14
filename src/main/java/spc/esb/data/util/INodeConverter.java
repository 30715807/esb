package spc.esb.data.util;

import java.util.HashMap;
import java.util.Map;

import spc.esb.data.IArrayNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;

/**
 * ����ڵ�͸��ӽڵ�ת��
 * 
 * @author chenjs
 * 
 */
public interface INodeConverter
{
	/**
	 * ��������ڵ�ת��Ϊ����ڵ����ͷ���
	 * 
	 * @param msg
	 * @param src
	 * @param schema
	 * @param esb2rcv
	 * @param pnode
	 * @param path
	 * @param tpnode
	 * @return
	 * @throws Exception
	 */
	INode converter(IMessage msg, INode src, MsgSchemaPO schema, boolean esb2rcv,
			ICompositeNode pnode, String path, ICompositeNode tpnode) throws Exception;

	final static Map CONVERTERS = new HashMap();
}
