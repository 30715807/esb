package spc.esb.data.validator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.util.FileUtil;
import spc.webos.util.SpringUtil;

/**
 * ���������·���schema����У�鷢�����б��ģ���ǰ���м��
 * 
 * @author spc
 * 
 */
public class SchemaValidator
{
	protected String schemaDir; // schema ��ŵ�Ŀ¼
	protected Map schemas; // ���������schema�ļ��� �����н�������Ϊkey
	protected Logger log = LoggerFactory.getLogger(getClass());

	public void init() throws Exception
	{
		// schema�ļ����Ǳ��ı��.xsd��ʽ��.xsd֮ǰΪ���ı��
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Map schemas = new HashMap();
		File dir = SpringUtil.getInstance().getResourceLoader().getResource(schemaDir).getFile();
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			File file = files[i];
			String name = file.getName();
			// ����schema������������֤�ĵ��ļ���������Schema����
			schemas.put(name.substring(0, name.lastIndexOf('.')), schemaFactory
					.newSchema(new StreamSource(new ByteArrayInputStream(FileUtil
							.is2bytes(new FileInputStream(file))))));
		}
		this.schemas = schemas;
		log.info("load schemaDir: " + schemaDir + ", size: " + schemas.size());
		if (log.isDebugEnabled()) log.debug("schema is: " + schemas.keySet());
	}

	public boolean validate(String msgCd, byte[] xml) throws Exception
	{
		return validate(msgCd, xml, 0, xml.length);
	}

	public boolean validate(String msgCd, byte[] xml, int offset, int length) throws Exception
	{
		// ������֤�ĵ��ļ��������ô��ļ���������װ���ļ�����schema��֤
		Schema schema = (Schema) schemas.get(msgCd);
		if (schema == null)
		{
			log.warn("schema is null by " + msgCd + " !!!");
			return false;
		}

		// ͨ��Schema��������ڴ�Schema����֤��������GBAInitSchema.xsd������֤
		Validator validator = schema.newValidator();

		// �õ���֤������Դ������GBAInit.xml
		Source source = new StreamSource(new ByteArrayInputStream(xml, offset, length));

		// ��ʼ��֤���ɹ����success!!!��ʧ�����fail
		validator.validate(source);
		return true;
	}

	public void setSchemaDir(String schemaDir)
	{
		this.schemaDir = schemaDir;
	}
}
