package spc.esb.common.service;

/**
 * ·�ɹ���ӿڣ�Ŀǰ·�ɹ����Ϊ��̬�Ͷ�̬��qname & ftlRule ����, ����˽ӿڣ������Ժ�����δ������·����ʽ
 * 
 * @author chenjs
 * 
 */
public interface Route
{
	boolean isValidRoute();
	
	String getQname(); // ��̬·�ɶ�����

	String getFtlRule(); // Ftl�﷨�Ķ�̬·�ɹ���

	String getRouteBeanName(); // added by chenjs 2011-12-20
								// ���Զ�ִ̬��spring�����е�bean����·��
}
