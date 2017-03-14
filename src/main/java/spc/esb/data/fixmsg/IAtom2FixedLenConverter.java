package spc.esb.data.fixmsg;

import spc.esb.data.IAtomNode;
import spc.esb.data.INode;
import spc.esb.model.MsgSchemaPO;

public interface IAtom2FixedLenConverter
{
	/**
	 * ��һ��������ֶ�ֵд�뵽���������С��������ڲ�ͬϵͳ���������͵Ĳ��������ܲ�һ����
	 * 
	 * @param fixedLen
	 * @param start
	 * @param len
	 * @param value
	 */
	void pack(byte[] fixedLen, int offset, IAtomNode value, MsgSchemaPO struct, String charset)
			throws Exception;

	/**
	 * �����������е�ĳ���ֶ���ȡ���һ��INode�ڵ�
	 * 
	 * @param fixedLen
	 * @param struct
	 * @return
	 * @throws Exception
	 */
	INode unpack(byte[] fixedLen, int offset, MsgSchemaPO struct, String charset) throws Exception;

	final static String DEFAULT_NUM_VALUE = "0";
	static final String TO_RIGHT = "R"; // ��Щ������Ҫ��߲��ո񣬷��ڱ��Ĺ淶��ext1����,Ĭ�����ұ߲��ո�
	static final String TRIM = "T"; // �Ƿ�trim
	static final String KICK = "K"; // �ߵ������еĿո� added by chenjs 2011-11-22
}
