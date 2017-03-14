package spc.esb.data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * ����ڵ�Ҫ���л�Ϊwebservice�淶�ڵ�
 * 
 * @author chenjs
 * 
 */
public class SOAPNode2XML extends Array2Node2XML
{
	protected void ext2XML(OutputStream os, INode value, ICompositeNode parent, String name,
			Map ext, Map attribute) throws IOException
	{
		super.ext2XML(os, value, parent, name, ext, attribute);
		if ((value instanceof ICompositeNode) && (ext == null || !ext.containsKey("xsi:type"))) addAttr(
				os, "xsi:type".getBytes(), ("esb:" + name + "Type").getBytes());
		// ���ڸ��ӽڵ�����Ͳο�Wsdl11DefinitionService.struct2schema����WSDL�ļ���ʽ
	}
}
