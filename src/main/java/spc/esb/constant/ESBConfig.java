package spc.esb.constant;

/**
 * ESB��esb_config��Ҫ��key
 * 
 * @author chenjs
 * 
 */
public class ESBConfig
{
	// ----------- for perf --------
	public static String PERF_IIBW = "esb.perf.iibW"; // IIB�ķ���Ȩ��
	public static String PERF_YCMBW = "esb.perf.ycmbW"; // ycmb�ķ���Ȩ��
	public static String PERF_YCMBJ = "esb.perf.ycmbJ"; // ycmb�Ƿ�journal��־
	public static String PERF_MINC = "esb.perf.minC"; // ����ת�����׵���С�����ʱ(10)
	// ----------- for perf end --------

	// ----------- for license --------
	public static String LICENSE_key = "esb.license.key"; // license�ļ�
	// ----------- for license end --------

	// ----------- for security --------
	public static String SECURITY_trace = "esb.security.trace"; // false, ���ټӽ��ܱ���
	// ----------- for security end --------

	// -------------for reversal----------------
	public static String RVSL_debug = "esb.reversal.debug"; // false
	public static String RVSL_defaultTimes = "esb.reversal.defaultTimes"; // 5
	public static String RVSL_intervalSeconds = "esb.reversal.intervalSeconds"; // 10000
	// -------------for reversal end------------

	// ---------------for timeout------------
	public static String TIMEOUT_traceQ = "esb.timeout.traceQ";
	// ---------------for timeout end------------

	// ---------------for alarm------------
	public static String ALARM_traceQ = "esb.alarm.traceQ";
	public static String ALARM_server = "esb.alarm.server"; // ��Щ����ϵͳ��������esb_alarm����
	// ---------------for alarm end------------

	// -------------for journal----------------
	public static String JOURNAL_ftsstatus = "esb.journal.ftsstatus"; // false,
																	// �Ƿ��¼fts��Ӧ����
	public static String JOURNAL_traceQ = "esb.journal.traceQ"; // ׷����ˮ��־����(������ռ��)
	public static String JOURNAL_traceDetail = "esb.journal.traceDetail"; // false,
																		// ���ڻع���ԣ�ȫ���ļ�¼
	public static String JOURNAL_fullBookInSubmsg = "esb.journal.fullBookInSubmsg"; // false,�ӽ����������̽���,
																				// ��¼ȫ����
	public static String JOURNAL_withoutSensitive = "esb.journal.withoutSensitive"; // false
	public static String JOURNAL_maxMsgSize = "esb.journal.maxMsgSize"; // 9144
	public static String JOURNAL_maxOrigBytesSize = "esb.journal.maxOrigBytesSize"; // 3000
	public static String JOURNAL_maxStatusDescSize = "esb.journal.maxStatusDescSize"; // 900
	public static String JOURNAL_charset = "esb.journal.charset"; // utf-8
	public static String JOURNAL_exSleepSeconds = "esb.journal.exSleepSeconds"; // 30
	public static String JOURNAL_performance = "esb.journal.performance"; // false
	public static String JOURNAL_percent = "esb.journal.percent"; // 0,
																// ϵͳ�����ܲ�������¼�¼��ˮ�İٷֱ�(0-100)
	// -------------for journal end------------

	// -------------for dlq journal----------------
	public static String DLQ_trace2db = "esb.dlq.trace2db"; // true, �Ƿ�������Ϣ�Ǽǵ����ݿ�,
	public static String DLQ_JOURNAL_PUTAPPTYP_MAX_SIZE = "esb.dlq.putAppTypeMaxSize"; // 100
	public static String DLQ_JOURNAL_PUTAPPNM_MAX_SIZE = "esb.dlq.putAppNameMaxSize"; // 100
	public static String DLQ_JOURNAL_MSG_MAX_SIZE = "esb.dlq.msgMaxSize"; // 100
	// -------------for dlq journal end------------

	// -------------for MB-----------------
	// �Ƿ�У��㲥���׵�Ȩ�ޣ�����ũ�����ߺ��кཻܶ��ûУ��㲥����Ȩ��, ����Ĭ��ֵ����Ϊtrue
	public static String MB_broadcastAuth = "esb.mb.broadcastAuth"; // "true"
	// �㲥�������û�����ö�Ӧ���ķ�����ֱ����ֹ�����ڹ㲥��2��ģʽ��1. ��׼�Ƽ�ģʽ���㲥ӳ��ɷ��� 2. ��ũ��ģʽ���㲥ӳ�����ϵͳ
	// ���ں���ũ��ecif��ͣ���㲥����û�н��շ�����ʱ��Ϣ����Ҫ�������գ�������ʹ����ũ�̵Ĺ㲥����ϵͳ
	public static String MB_broadcastExit = "esb.mb.broadcastExit"; // "true"

	// MB ���log4j�����ļ��ļ��ʱ�䣬Ĭ��Ϊ10s
	public static String MB_log4jRefreshInterval = "esb.mb.log4jRefreshInterval"; // "10"
	// MB �� tranlog ��־������ü�Ҫģʽ���ȴ�ͳ����2����־
	// public static String MB_briefTranlog = "mb.briefTranlog"; // "true"
	// MB Ĭ�Ϻ��Բ��Ϸ���xml�ַ�
	public static String MB_ignoreUnvalidXMLChar = "esb.mb.ignoreUnvalidXMLChar"; // "false"
	// ��ĳЩ��͸�����ܲ��Գ�����, ReqBa, RepFaʱ��ɾ��orginalbytes�������ѹ����������
	public static String MB_removeOriginalBytes = "esb.mb.removeOriginalBytes"; // "true"
	// �Ƿ�֧�ֻ�������ǩ��, 3.1�汾ʹ�ã��Ժ�汾����ʹ��
	public static String MB_bodySig = "esb.mb.bodySig"; // "false"
	// SigPre��ȡ���ĵĴ�С����, -1�����С�����ƣ�0������ȡ����,
	public static String MB_traceMaxMsgLen = "esb.mb.traceMaxMsgLen"; // "0"
	// 2012-05-25 �Ƿ���MB��Ƕ��ʽִ��BPL, Ĭ��ΪǶ��ʽ
	public static String MB_embedBPL = "esb.mb.embedBPL"; // "JBPM3"
	// 2012-05-12 ����MB��Ϣ���� ͬ��Ӧ����Ϣ ʱָ�����������й������е�ָ������,
	// Ĭ��Ϊ���ַ�������ʾ���뼯Ⱥ��ĳ�����У�����ָ��������й�����
	public static String MB_synResponse2QM = "esb.mb.synResponse2QM"; // ""
	// ͬ��Ӧ����Ϣ�ڶ����еĳ�ʱʱ�䣬Ĭ��Ϊ10��
	public static String MB_synRepExpireSeconds = "esb.mb.synRepExpireSeconds"; // 10
	public static String MB_asynRepExpireSeconds = "esb.mb.asynRepExpireSeconds"; // 259200,
																				// 3*24hours
	public static String MB_validateHeader = "esb.mb.validateHeader"; // true
	public static String MB_validateBody = "esb.mb.validateBody"; // true
	public static String MB_autoFilterUndefinedTag = "esb.mb.autoFilterUndefinedTag"; // false
	// default is [2, 5]
	public static String MB_validServiceStatus = "esb.mb.validServiceStatus";
	public static String MB_mappingRetCd = "esb.mb.mappingRetCd";
	public static String MB_replyToQPrefix = "esb.mb.replyToQPrefix"; // Ӧ�����ǰ׺
	// -------------for MB end----------------------------------

	// -----------for DB--------------
	public static String DB_ok = "esb.db.ok"; // 452, �ж����ݿ��Ƿ���ã���������ֹͣ��DB��¼��־, true
	public static String DB_esbInfoVerDt = "status.refresh.esb.base"; // ������Ϣ����
	public static String DB_msgDefVerDt = "status.refresh.esb.msgdef"; // ���Ķ���汾����
	public static String DB_flowCtrlVerDt = "status.refresh.esb.flowctrl"; // �����������ݰ汾����
	public static String DB_bplVerDt = "status.refresh.esb.bpl"; // ���̷�����汾����
	// -----------for DB end----------

	// -----------for Log----------------
	public static String LOG_FTL = "Log.FTL"; // ��־FTL����
	// -----------for Log end----------------
}
