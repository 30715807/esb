package spc.esb.data.iso8583;

import spc.esb.data.AtomNode;
import spc.esb.data.INode;

/**
 * Ĭ�ϰ�һ��8583�ڵ���һ��esb xml��ԭ�ӽڵ�
 * 
 * @author chenjs
 * 
 */
public class DefaultField2NodeConverter implements IField2NodeConverter
{
	public INode field2node(Field f)
	{
		return new AtomNode(f.value);
	}

	public Field node2field(Field f, INode node)
	{
//		f.value = node.toString(); // ��bug, ��������enable����
		f.setValue(node.toString()); // 800
		return f;
	}

	static DefaultField2NodeConverter df2n = new DefaultField2NodeConverter();

	public static DefaultField2NodeConverter getInstance()
	{
		return df2n;
	}

}
