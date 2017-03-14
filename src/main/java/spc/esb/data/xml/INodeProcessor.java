package spc.esb.data.xml;

import spc.esb.data.ICompositeNode;
import spc.esb.data.IMessage;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;

/**
 * �������������͵�INode�ڵ㣬������ת��Ϊ��������, ����xml - xml'ת��ʱ�ı�ڵ����ͣ����統ǰ�ڵ����������ͣ�Ŀ��ڵ��Ǹ��ӽڵ�����
 * 
 * @author chenjs 2012-01-10
 * 
 */
public interface INodeProcessor
{
	INode process(IMessage msg, INode src, MsgSchemaPO schema, boolean esb2rcv,
			ICompositeNode pnode, String path, ICompositeNode tpnode) throws Exception;
}
