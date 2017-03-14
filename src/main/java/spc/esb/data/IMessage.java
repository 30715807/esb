package spc.esb.data;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import spc.webos.exception.Status;

/**
 * ͨ�������ĽṹΪsoap��ʽ,�ṹΪ�� <Envelope> <Header></Header> <Body></Body> </Envelope>
 * 
 * @author spc
 * 
 */
public interface IMessage extends Serializable
{
	// �����������ͣ�version, clusterId, cn2utf8, signature, transSN, transId, status
	void setOriginalBytes(byte[] original);

	byte[] getOriginalBytes();

	String getOriginalBytesPlainStr();

	void setOriginalBytesPlainStr(String original);

	boolean isRequestMsg(); // ��ǰ������������

	boolean isCanResponse(); // ��ǰ�����ܷ���

	String getLang();

	void setLang(String lang);

	String getSndDt();

	void setSndDt(String sndDt);

	String getSndTm();

	void setSndTm(String sndTm);

	String getSeqNb();

	void setSeqNb(String seqNb);

	String getRcvAppSN();

	void setRcvAppSN(String rcvAppSN);

	String getRefSndDt();

	void setRefSndDt(String refSndDt);

	String getRefSeqNb();

	void setRefSeqNb(String refSeqNb);

	String getRefMsgSn();

	String getMsgSn();

	void setSn(String sn);

	String getRefMsgCd();

	void setRefMsgCd(String refMsgCd);

	String getMsgCd();

	void setMsgCd(String msgCd);
	
	String getReplyMsgCd();

	void setReplyMsgCd(String replyMsgCd);

	// String getSigType();
	//
	// void setSigType(String sigType);

	String getCallType();

	void setCallType(String callType);

	String getRefCallType();

	void setRefCallType(String callType);

	boolean isSynCall();

	String getSndNode();

	String getRcvNode();

	String getSndApp();

	String getRcvApp();

	String getSndNodeApp();

	String getRcvNodeApp();

	String getRefSndNode();

	String getRefSndApp();

	String getRefSndNodeApp();

	void setSndNode(String sndNode);

	void setSndAppCd(String sndAppCd);

	void setRefSndNode(String refSndNode);

	void setRefSndApp(String refSndApp);

	void setRcvNode(String rcvNode);

	void setRcvAppCd(String rcvAppCd);

	void setReplyToQ(String replyToQ);

	String getReplyToQ();

	String getVersion();

	void setVersion(String version);

	String getSignature();

	void setSignature(String signature);

	String getLocation();

	void setLocation(String location);

	void setCn2utf8(boolean cn2utf8);

	boolean isCn2utf8();

	byte[] getCorrelationID();

	void setCorrelationID(byte[] correlationID);

	int getExpirySeconds();

	void setExpirySeconds(int expirySeconds);

	long getStart();

	void setStart(long start);

	long getEnd();

	void setEnd(long end);

	String getFixedErrDesc(); // �̶��Ĵ�����Ϣ���� sn + transid

	Status getStatus();

	void setStatus(Status status);

//	ICompositeNode getMsg();
//
//	ICompositeNode setMsg(ICompositeNode msg);

	String getHeaderExtStr(); // 700, 2013-05-05

	void setHeaderExt(String hdrExt); // 700, 2013-05-05

	ICompositeNode getHeaderExt();

	void setHeaderExt(ICompositeNode hdrExt);

	void setInHeaderExt(String key, Object val);

	INode findInHeaderExt(String key);

	// added by chenjs 2011-09-02 ���ӱ���msg local����
	ICompositeNode getMsgLocal();

	ICompositeNode setMsgLocal(ICompositeNode local);

	IAtomNode findAtomInMsgLocal(String path, IAtomNode def);

	IArrayNode findArrayInMsgLocal(String path, IArrayNode def);

	ICompositeNode findCompositeInMsgLocal(String path, ICompositeNode def);

	void setInMsgLocal(String key, Object val);

	// added by chenjs 2011-09-02 ���ӱ���msg local����

	// 2011-09-02 ע�͵�pnt����
	// ICompositeNode getPnt();
	//
	// ICompositeNode setPnt(ICompositeNode pnt);

	// �����߱�����end

	String getMQCorId();

//	void init();

	void setTransaction(ICompositeNode transaction);

	ICompositeNode getTransaction();

	void clearLocal();

	void clearRequest();

	void clearHeader();

	Throwable getEx();

	/**
	 * ��鵱ǰ�ڵ����Ƿ���������±��������·��Ԫ��, ��������������쳣��ʽ�׳�
	 * 
	 * @param paths
	 */
	void mustContain(Collection paths);

	void mustContain(String[] paths);

	/**
	 * �ӱ����л��һ��ָ�����͵Ľڵ㣬���û�У����߽ڵ����Ͳ�ƥ�����׳��쳣
	 * 
	 * @param parentPath
	 * @param parent
	 * @param path
	 * @param type
	 * @param canNull
	 * @return
	 */
	INode find(String parentPath, ICompositeNode parent, String path, byte type, boolean canNull);

	/**
	 * @param path
	 * @return
	 */
	INode find(String path);

	IAtomNode findAtom(String path, IAtomNode def);

	IArrayNode findArray(String path, IArrayNode def);

	ICompositeNode findComposite(String path, ICompositeNode def);

	INode findIgnoreCase(String path);

	void setEx(Throwable ex);

	boolean isContainExFnode();

	boolean isExitFlow();

	void setExitFlow(boolean exitFlow);

	void setContainExFnode(boolean containExFnode);

	MessageAttr getAttr();

	void setAttr(MessageAttr attr);

	MessageAttr getRefAttr();

	void setRefAttr(MessageAttr refAttr);

	Map getLocal();

	Object getInLocal(String key);

	Object getInLocal(String key, Object obj);

	void setInLocal(String key, Object value);

	ICompositeNode getHeader();

	ICompositeNode createInHeader(String path);

	ICompositeNode setHeader(ICompositeNode header);

	ICompositeNode setBody(ICompositeNode body);

	ICompositeNode setRequest(ICompositeNode request);

	ICompositeNode setResponse(ICompositeNode response);

	INode findInHeader(String path);

	Object getInHeader(String path);

	Object findInHeader(String path, Object target);

	void setInHeader(String key, Object value);

	ICompositeNode createInResquest(String path);

	ICompositeNode getRequest();

	/**
	 * @param path
	 * @return
	 */
	INode findInRequest(String path);

	IAtomNode findAtomInRequest(String path, IAtomNode def);

	IArrayNode findArrayInRequest(String path, IArrayNode def);

	ICompositeNode findCompositeInRequest(String path, ICompositeNode def);

	Object getInRequest(String path);

	Object findInRequest(String path, Object target);

	void setInRequest(String key, Object value);

	ICompositeNode getBody();

	ICompositeNode getResponse();

	ICompositeNode createInResponse(String path);

	INode findInResponse(String path);

	IAtomNode findAtomInResponse(String path, IAtomNode def);

	IArrayNode findArrayInResponse(String path, IArrayNode def);

	ICompositeNode findCompositeInResponse(String path, ICompositeNode def);

	Object getInResponse(String path);

	/**
	 * @param path
	 * @return
	 */
	Object findInResponse(String path, Object target);

	void setInResponse(String key, Object value);

	void toXml(OutputStream os, boolean pretty);

	void toXml(OutputStream os, boolean pretty, INode2XML node2xml);

	void toXml(OutputStream os, boolean pretty, INode2XML node2xml, Map attribute);

	byte[] write2bytes();

	byte[] toByteArray2(boolean gzip);

	byte[] toByteArray(boolean gzip);

	byte[] toByteArray(boolean gzip, boolean pretty, INode2XML node2xml);

	String toXml(boolean pretty);

//	IMessage newInstance();

	// for local
	Object getBplVariable(String key);

	void setBplVariable(String key, Object value);

	void removeBplVariable(String key);

	Object getMQAttribute(String key);

	void setMQAttribute(String key, Object value);

	void removeMQAttribute(String key);

	Object getMBAttribute(String key);

	void setMBAttribute(String key, Object value);

	void removeMBAttribute(String key);

//	public static final String XML_HDR = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
//	// modified by chenjs 2012-01-17 ȡ��XML���ķ��͵�ͷ����
//	public static final byte[] XML_HEADER = XML_HDR.getBytes();
	// 900, ����soap�淶��xml
	public static String TAG_ROOT = "Envelope"; // "transaction";
	public static String TAG_HEADER = "Header"; // "header";
	public static String TAG_BODY = "Body"; // "body";
	// public static String TAG_REQUEST = "request";
	// public static String TAG_RESPONSE = "response";
	public static String TAG_LOCAL = "local"; // ������ʱ��Ϣ
	public static String TAG_LOCATION = "location"; // BAģʽʱ���ڴ�ŷ����ַ

	public static String HEADER_EXT_PREFIX = "base64:"; // 800, EXT�ֶ�base64����ǰ׺
	public static String TAG_HEADER_EXT = "ext";
	public static String TAG_CN2UTF8 = "cn2utf8"; // �Ƿ񷵻صĿ��ܺ������ĵ��ַ���ת��Ϊutf8�ַ���ʽ
	public static String TAG_HEADER_STATUS = "status";

	// ��esb����ͷ�з�Ϊrequester, esb(request, responose), provider��ǩ
	// public static String TAG_NODE = "mbrCd"; // ����ڵ�
	// public static String TAG_APP = "appCd"; // ҵ��ϵͳ
	// public static String TAG_IP = "ip";
	public static String TAG_SNDDT = "sndDt"; // ��������yyyyMMdd
	public static String TAG_SNDTM = "sndTm"; // ����ʱ��HHmmss

//	public static String TAG_HEADER_MSG = "msg"; // ������Ϣ
	// public static String TAG_HEADER_SIGTYPE = "sigTyp"; //
	// ǩ����ʽ��Ĭ����body�ֽ�ȫ��ǩ����1��ʾ��������ǩ��
	public static String TAG_HEADER_LANGUAGE = "lang"; // �������࣬Ĭ��Ϊ����
	// ��������: SYN:ͬ���� ASYN:�첽
	public static String TAG_HEADER_CALLTYPE = "callTyp";
	public static String TAG_HEADER_MSG_CD = "msgCd"; // ���ı��
	public static String TAG_HEADER_MSG_SN = "seqNb"; // ������ˮ�ţ�����Ψһ��ˮ
	public static String TAG_HEADER_MSG_RCVAPPSN = "rcvAppSN"; // ��̨������ˮ��
	public static String TAG_HEADER_MSG_SNDNODE = "sndMbrCd"; // ���ͳ�Ա���
	public static String TAG_HEADER_MSG_SNDAPP = "sndAppCd"; // ����Ӧ�ñ��
	public static String TAG_HEADER_MSG_RCVNODE = "rcvMbrCd"; // ���ճ�Ա����
	public static String TAG_HEADER_MSG_RCVAPP = "rcvAppCd"; // ����Ӧ�ñ��
	public static String TAG_HEADER_MSG_REFMSGCD = "refMsgCd"; // �ο����ı��
	public static String TAG_HEADER_MSG_REFCALLTYP = "refCallTyp"; // �ο���������
	public static String TAG_HEADER_MSG_REFSNDNODE = "refSndMbrCd"; // �ο����ͳ�Ա���
	public static String TAG_HEADER_MSG_REFSNDAPP = "refSndAppCd"; // �ο�����Ӧ�ñ��
	public static String TAG_HEADER_MSG_REFSNDDT = "refSndDt"; // �ο�����ʱ��
	public static String TAG_HEADER_MSG_REFSNDSN = "refSeqNb"; // �ο���ˮ��
	// 700_130520 Ӧ�����
	public static String TAG_HEADER_MSG_REPLYTOQ = "replyToQ";
	// 935, ����replyMsgCd, �ṩ��JS�������
	public static String TAG_HEADER_REPLYMSGCD = "replyMsgCd";

	// public static String TAG_HEADER_PNT = "pnt"; // ���뷽��Ϣ

	public static String CALLTYP_SYN = "SYN"; // ���÷�ʽ --- ͬ��
	public static String CALLTYP_ASYN = "ASYN"; // ���÷�ʽ --- �첽
	public static String NODE_CENTER = "0000"; // ���Ľڵ�
	public static String APP_ESB = "ESB";

	// ·��
	public static final String PATH_DELIM = "/";
//	public static String PATH_HEADER_MSG = TAG_HEADER + PATH_DELIM + TAG_HEADER_MSG;
	// public static String PATH_REQUEST = TAG_BODY + PATH_DELIM + TAG_REQUEST;
	// public static String PATH_RESPONSE = TAG_BODY + PATH_DELIM +
	// TAG_RESPONSE;
	public static String PATH_STATUS = TAG_HEADER + PATH_DELIM + TAG_HEADER_STATUS;

	public static String TAG_SIGNATURE = "signature"; // ǩ���ֶ�
	public static String TAG_ORIGINALBYTES = "originalBytes"; // ��ǰxml���Ķ�Ӧ��ԭ���Ķ���������
	public static String TAG_HEADER_VERSION = "ver"; // �汾��
	public static String HEADER_VERSION_1_0 = "1.0"; // �汾��

	public static String TRANS_TAG_SEQ = "header,body,signature";
}
