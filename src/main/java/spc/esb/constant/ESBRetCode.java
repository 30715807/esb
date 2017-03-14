package spc.esb.constant;

import spc.webos.constant.AppRetCode;

/**
 * ϵͳ�������ͳһ����ģʽʵ��, ������Ϊ12λ: XX+XXXX ��λ����� ���˵�� ��ע 0X �ɹ�
 * ����ɹ���ķ�����Ϣ�룬ͨ�óɹ���Ϊ000000 1X ϵͳ����� 10 ͨ������� ϵͳͨ�Ų����Ĵ����� 11 �ļ������ 12 ���ݿ������ 13
 * �м������� MQ,tuxedo��MB��WPS�� 19 ����ϵͳ����� 2X ��������� 20 ���ĸ�ʽ����� 29 ������������� 3X ҵ�������
 * 30 ҵ����Ȩ���� 31 �˻������ 39 ����δ����ҵ����� 99 ����δ�������
 * 
 * @author spc
 * 
 */
public class ESBRetCode extends AppRetCode
{
	public static String NO_ENDPOINT = "100000"; // û��ָ��endpoint

	// 20���ĸ�ʽ����
//	public static String MSG_ERRS = "200000"; // ��������У�鲻ͨ��
	public static String MSG_SNDAPPCD = "208886"; // ���Ϸ��ķ��ͷ�ϵͳ
	public static String MSG_UNVALIDCHAR = "208888"; // ���ĺ��зǷ��ַ�
	public static String MSG_FIELD_VALIDATOR = "209999"; // �����нڵ���֤ͨ�ô����룬�˴����벻ָ������Ĵ���ģ����Ϣ,�����������ݿ�
	public static String MSG_BYTE2XML = "200001"; // ��bytes��xml��������
	public static String MSG_2INNEROBJ = "200002"; // ��xml���ı�Ϊbytes����
	public static String MSG_UNDEF_TAG = "200003"; // ��������û��ָ��TAG
	public static String MSG_UNMATCH_TYPE = "200004"; // Ӧ�ó���Ҫ��Ľڵ����ͺͱ��Ĳ�����
														// args=path,cur_type,target_type
	public static String MSG_UNSIG_FAIL = "200005"; // ������ǩʧ��
	public static String MUST_OPTIONAL = "200006"; // ������δ��д
	public static String MSG_STRING_FORMAT = "200007"; // ���ַ���{0}�ӵ�ǰ��ʽ{1}תΪĿ���ʽ{2]ʧ��
	public static String MSG_FIELD_INVALID = "200008"; // �����ֶ�{0}��ֵ{1}���Ϸ�{2}
	// public static String NO_ATOMCONVERTER = "200009"; //
	// �Ҳ������ڵ�ת����args=fconverter
	public static String MSG_SCHEMA_FAILD = "200010"; // ����schema���ʧ��{0}
	public static String MSG_FIELD_LEN = "200011"; // �����ֶ�{0}�ĳ���{1}��Χ���Ϸ�{2}-{3}
	public static String MSG_FIELD_VAL_RANGE = "200012"; // �����ֶ�{0}{1}��Χ���Ϸ�{2}-{3}
	public static String MSG_FIELD_REG = "200013"; // �����ֶ�{0}��ֵ{1}��������ʽ{2}
	public static String MSG_FIELD_DECIMAL = "200014"; // �����ֶ�{0}ֵΪ{1]��С���㳤�ȳ���{2}
	public static String MSG_FIELD_NOTNUMBER = "200015"; // �����ֶ�{0}ֵΪ{1}������������

	// 21 cache���޷��ҵ���Ҫ�����ݿ����ص���Ϣ
	public static String MSGCD_NOTFOUND = "210001"; // �Ҳ���������Ϣ
	public static String MSGSTRUCT_NOTFOUND = "210002";// �Ҳ������Ľṹ
	public static String CACHEINFO_NOTFOUND = "210003";// �Ҳ�����Ҫ�����ݿ⻺����Ϣ

	// 22
	public static String MSGCD_UNDEFINDED = "220001"; // ���ı���Ҳ���
	public static String MSG_BRD_NOTRETURN = "200017"; // �㲥�����޷���
}
