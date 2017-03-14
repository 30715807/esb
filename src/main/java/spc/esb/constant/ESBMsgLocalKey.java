package spc.esb.constant;

/**
 * ������ESBMessage��ʱ����������
 * 
 */

public class ESBMsgLocalKey
{
	public final static String LOCAL_PORT = "localPort";
	public final static String REMOTE_IP = "remoteIP";
	public final static String HTTP_URI = "uri";

	// ****msg��ʱ�����ռ��е�����key
	public static final String TARGET_MSG_BYTES = "ML_TARGET_BYTES";
	public static final String MSG_TEMP_REQ_MSG = "REQ_MSG"; // ������ʱ���е�key
	public static final String REF_MSG_ATTRIBUTE = "REF_MSGATTR"; // �ο���������
	// public static final String MSG_TEMP_MSG_VO = "MSG_VO"; // ������ʱ���е�key
	// public static final String MSG_TEMP_MSGSTRUCT_VO = "MSGSTRUCT_VO"; //
	// ������ʱ���е�key

	public static final String MSG_LENGTH = "MSG_LENGTH"; // ��ǰ���ĵ��ֽڳ���

	// public static final String ESB_REP_BYTES = "ESB_REP_BYTES"; // esbӦ��bytes

	public static final String BLOB_HEADER = "BH";

	// ��ʱ�ռ� from IMessage
	// ESB����ʹ�ã�Ӧ���ĳ�ʱʱ�� chenjs 2012-01-27
	public static final String LOCAL_RESPONSE_TIMEOUT = "ML_RESPONSE_TIMEOUT";

	public static final String LOCAL_UNVALID_XML_CHAR = "ML_UNVALID_XML_CHAR"; // ���Ϸ���xml�ַ�
	public static final String LOCAL_UNVALID_XML = "ML_UNVALID_XML"; // ���Ϸ���xml����
	public static final String LOCAL_INBUF_TIME = "ML_INBUF_TIME"; // ��Ϣ����buffer��ʱ��
	public static final String LOCAL_REP_BUFFER = "ML_REP_BUF"; // ��ǰ���ĵ�ָ����Ӧbuffer
	public static final String LOCAL_REP_BUFFER_NAME = "ML_REP_BUF_NAME"; // ��ǰ���ĵ�ָ����Ӧbuffer
																			// NAME
	public static final String LOCAL_ORIGINAL_REQ_BYTES = "ML_REQ_BYTES"; // ���ܵ���ԭʼ�����Ʊ���
	public static final String LOCAL_ORIGINAL_REQ_QMSG = "ML_REQ_QUEUEMSG"; // ���ܵ���ԭʼQueueMessage����
	public static final String LOCAL_ORIGINAL_REQ_SOCKETMSG = "ML_REQ_SOCKETMSG"; // ���ܵ���ԭʼSocketMessage����
	public static final String LOCAL_ACCESS4LOCAL = "ML_ACCESS4LOCAL"; // ֱ��ͬ��������Ϣ��������Ҫ���뷵��buffer�Ͷ���
	public static final String LOCAL_PARENT_MSG = "ML_PARENT_MSG"; // ԭ����������
	public static final String LOCAL_SUB_MSG = "ML_SUB_MSG"; // �����������и����İ������ӱ���
	public static final String LOCAL_NSUB_MSG = "ML_NSUB_MSG"; // �����������и����İ������´������ӱ���
	public static final String LOCAL_LAST_NODE = "ML_LAST_NODE"; // ���һ��ִ�е�node
	public static final String LOCAL_JBPM_PROCESS_NAME = "ML_JBPM_PROCESS_NAME"; // ��ǰ������Ҫ�Ĵ���������
	public static final String LOCAL_MSG_STRUCT_VO = "ML_MSG_STRUCT_VO"; // ��ǰ���ĵ�MsgStructVO
	// map attribute
	public static final String LOCAL_MQ_ATTRIBUTE = "ML_MQ_ATTRIBUTE"; // ��Ϣ���ĵ�MQ����
	public static final String LOCAL_MB_ATTRIBUTE = "ML_MB_ATTRIBUTE"; // ��Ϣ���ĵ�MB����
	public static final String LOCAL_BPL_VARIABLES = "ML_BPL_VARIABLES"; // ��ǰBPL�����ռ�

	public static final String LOCAL_EXECUTABLE = "ML_EXECUTABLE"; // ��ǰ���ĵĿ�ִ�ж���������
	public static final String LOCAL_ORIGINAL_REQUEST = "ML_ORIGINAL_REQUEST"; // ��ǰ���ĵ�ԭʼ����request��ǩ
	public static final String LOCAL_REP_BYTES = "ML_REP_BYTES"; // Ӧ��bytes
	public static final String LOCAL_MSG_CONVERTER = "ML_MSG_CONVERTER"; // ��Ϣת����
	public static final String LOCAL_MSG_MSGFLOW = "ML_MSG_MSGFLOW"; // ָ����Ϣ������
	public static final String LOCAL_MSGCVT_DELAY = "ML_MSGCVT_DELAY"; // �����ӳٽ�����־
	// ��ʱ�ռ� from IMessage end

	public final static String SUB_PROCESS_NAME_KEY = "SUB_PROC_NAME"; // ��������

	public final static String MQGET_QMSG_KEY = "MQGET_QMSG"; // MQ���սڵ㷢��QueueMessage
	public final static String MQPUT_QMSG_KEY = "MQPUT_QMSG"; // MQ���ͽڵ㷢��QueueMessage
	public final static String MQPUT_MQMSG_KEY = "MQPUT_MQMSG"; // MQ���ͽڵ㷢��MQMessage

	public final static String PUT_QMSG_KEY = "QPUT_QMSG"; // ���ͽڵ㷢��QMessage
															// //add by liujk
															// 2013-3-12

	public final static String NO_RETURN = "MSG2BUF_NO_RETURN"; // ���践��
	public final static String SND_BUF = "MSG2BUF_SND_BUF"; // ����Buffer��־

	public final static String ACCEPTOR_PROTOCOL = "ML_ACCEPTOR_PROTOCOL"; // ���ĵĽ��뷽ʽ
	public static final String ACCEPTOR_REMOTE_HOST = "REMOTE_HOST";
	public static final String ACCEPTOR_REMOTE_URI = "REMOTE_URI";
	public static final String ACCEPTOR_LOCAL_PORT = "LOCAL_PORT";

	public final static String LOCAL_DELAY_TASKS = "ML_DELAY_TASKS"; // ��Ҫ��ʱ���������

	// for PersistenceAFNode
	public final static String JDBC_SQL_ID = "JDBC_SQL_ID";
	public final static String JDBC_RESULT = "JDBC_RESULT";

	// for blob msg in local
	public final static String BLOB_FILES = "BLOB_FILES";
}
