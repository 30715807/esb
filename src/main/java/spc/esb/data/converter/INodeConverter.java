package spc.esb.data.converter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.esb.data.INode;

/**
 * ����java��ͨ����ͳ����������ͽڵ�֮���ת��
 * 
 * @author spc
 * 
 */
public interface INodeConverter
{
	boolean support(Object obj);

	boolean supportNode(INode node);

	INode unpack(Object obj, Map attribute);

	Object pack(INode node, Object target, Map attribute);

	static final Logger log = LoggerFactory.getLogger(INodeConverter.class);
}
