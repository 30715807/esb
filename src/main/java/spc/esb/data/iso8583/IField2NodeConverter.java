package spc.esb.data.iso8583;

import spc.esb.data.INode;

/**
 * ��һ��8583��Field����esb xml�е��������ͽڵ�IAtomNode, IArrayNode, ICompositeNode
 * 
 * @author chenjs
 * 
 */
public interface IField2NodeConverter
{
	INode field2node(Field f);

	Field node2field(Field f, INode node);
}
