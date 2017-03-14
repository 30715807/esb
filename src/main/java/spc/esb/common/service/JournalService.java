package spc.esb.common.service;

import spc.esb.data.IMessage;
import spc.esb.model.LogPO;

/**
 * �����ĵǼǵ����ݿ�
 * 
 * @author spc
 * 
 */
public interface JournalService
{
	void sendLog(IMessage msg, String logPoint);

	void sendAlarm(IMessage msg);

	void doAlarm(IMessage msg, byte[] xml, String broker, String eg) throws Exception;

	void doJournal(IMessage msg, String logpoint) throws Exception;

	void doJournal(IMessage msg, String logpoint, String logDt) throws Exception;

	void doJournal(IMessage msg, String logPoint, String logDt, String broker, String eg)
			throws Exception;

	// added by chenjs 2011-04-02 �����첽Ӧ��ģʽ�»ָ������ģ��Ӷ����ɲο���Ϣ����չ��Ϣ
	IMessage fetchJournal(LogPO logVO) throws Exception;
}
