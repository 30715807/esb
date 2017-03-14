package spc.esb.data.converter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;

import spc.esb.data.AtomNode;
import spc.esb.data.CompositeNode;
import spc.esb.data.FixedMessage;
import spc.esb.data.IAtomNode;
import spc.esb.data.ICompositeNode;
import spc.esb.data.INode;
import spc.webos.util.POJOUtil;

public class CompositeNodeConverter implements INodeConverter
{
	static CompositeNodeConverter CNC = new CompositeNodeConverter();
	public final static String ATTR_MAPPING = "MAPPING";
	public final static String ATTR_CONVERTER = "CONVERTER";
	// public final static ThreadLocal MAPPING = new ThreadLocal(); //
	// �Ƿ�̬���������ļ���ӳ���ϵ�����VO��ӳ��
	// public final static ThreadLocal CONVERTER = new ThreadLocal(); //
	// �Ƿ�ÿ���ֶ���ת����
	Logger log = LoggerFactory.getLogger(CompositeNodeConverter.class);

	public static CompositeNodeConverter getInstance()
	{
		return CNC;
	}

	public INode unpack(Object value, Map attribute)
	{
		if (value == null) return null; // added by chenjs 2011-12-21
		if (value instanceof Map)
		{
			// modified by chenjs 2011-10-28
			CompositeNode cnode = new CompositeNode();
			Map m = (Map) value;
			Iterator keys = m.keySet().iterator();
			while (keys.hasNext())
			{
				String key = keys.next().toString();
				cnode.set(key, NodeConverterFactory.getInstance().unpack(m.get(key), attribute));
			}
			return cnode;
			// return new CompositeNode(new HashMap((Map) value));//
			// ��������ԭ�ÿռ䡣���ı�ṹ
		}
		if (value instanceof FixedMessage)
			return ((FixedMessage) value).toCNode(new CompositeNode());

		// ��ȡ������Ϣ�е�bean��·������, ����νṹ��Ϊ�ǲ�νṹ
		Object mappingName = attribute == null ? null : attribute.get(ATTR_MAPPING);
		Map mapping = null;
		if (mappingName instanceof Map) mapping = (Map) mappingName;
		// ��ȡ�ڵ�ת��������
		Object converterName = attribute == null ? null : attribute.get(ATTR_CONVERTER);
		Map converters = null;
		if (converterName instanceof Map) converters = (Map) converterName;

		CompositeNode cnode = new CompositeNode();
		Class clazz = value.getClass();
		for (; clazz != null; clazz = clazz.getSuperclass())
		{
			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < fields.length; i++)
			{
				Field field = fields[i];

				if (Modifier.isTransient(field.getModifiers())
						|| Modifier.isStatic(field.getModifiers()))
					continue;
				field.setAccessible(true);
				try
				{
					Object v = field.get(value);
					if (v == null) continue;// don't put null value
					// System.out.println("oc:"+field.getName()+","+v.getClass()+","+v);

					String fieldName = field.getName();
					String path = mapping == null ? fieldName : (String) mapping.get(fieldName);
					if (path == null) path = fieldName;

					// ���ýڵ��Ƿ���Ҫת��
					v = convert(v, converters, fieldName, false);

					// System.out.println("oc:"+field.getName()+","+v.getClass()+","+v);
					if (path != null) cnode.set(path, v);
					else log.warn("field:" + fieldName + ",path is null");
				}
				catch (Exception e)
				{
					// e.printStackTrace();
					log.warn("error accessing property [" + field.getName() + "] of class [" + clazz
							+ "]", e);
				}
			}
		}
		return cnode;
	}

	public boolean support(Object obj)
	{
		return true;
	}

	public Object pack(INode node, Object target, Map attribute)
	{
		ICompositeNode map = (ICompositeNode) node;
		Object v = null;
		try
		{
			if (target instanceof Class)
			{
				if ((Class) target == Map.class) v = new HashMap();
				else v = ((Class) target).newInstance();
			}
			else v = target;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		if (v instanceof ICompositeNode)
		{
			((ICompositeNode) v).set(map);
			return v;
		}
		if (v instanceof Map)
		{
			((Map) v).putAll(map.mapValue());
			return v;
		}
		Class voClazz = v.getClass();
		BeanWrapperImpl wrapper = new BeanWrapperImpl(true);
		wrapper.setWrappedInstance(v);
		// ��ȡ������Ϣ�е�bean��·������, ����νṹ��Ϊ�ǲ�νṹ
		Map mapping = null;
		Object mappingName = attribute == null ? null : attribute.get(ATTR_MAPPING);
		if (mappingName instanceof Map)
		{ // ����ԭvo����������
			mapping = new HashMap();
			mapping.putAll(POJOUtil.getClazzField(v.getClass()));
			mapping.putAll((Map) mappingName);
		}
		else
		{ // ���������������Ϣ, �򲻲���ԭ��������Ϊ����������
			if (mappingName == null) mappingName = voClazz.getName();
		}

		// ��ȡ�ڵ�ת��������
		Object converterName = attribute == null ? null : attribute.get(ATTR_CONVERTER);
		Map converters = null;
		if (converterName instanceof Map) converters = (Map) converterName;

		if (mapping == null)
		{ // ���ñ���������ṹ������Ϣ
			Iterator keys = map.keys();
			while (keys.hasNext())
			{
				String key = (String) keys.next();
				// System.out.println("v:"+v.getClass()+"--"+v);
				String propertyName = POJOUtil.getProperty(voClazz, key);
				// System.out.println(key + ", " + propertyName + ","
				// + wrapper.getPropertyType(propertyName));
				if (propertyName == null) continue;
				Class clazz = wrapper.getPropertyType(propertyName);
				if (clazz == null) continue;
				Object fv = NodeConverterFactory.getInstance().pack(map.getNode(key), clazz,
						attribute);

				// ���ýڵ��Ƿ���Ҫת��
				fv = convert(fv, converters, propertyName, true);
				if (fv != null && (clazz == String.class || !(fv instanceof String)
						|| fv.toString().length() > 0))
					wrapper.setPropertyValue(propertyName, fv);
			}
		}
		else
		{ // ������ñ��ж�ÿ��java bean�����˶�Ӧ���ر�·������
			Iterator keys = mapping.keySet().iterator();
			while (keys.hasNext())
			{
				String key = (String) keys.next();
				String propertyName = POJOUtil.getProperty(voClazz, key);
				String path = (String) mapping.get(key);
				// if (path == null) path = propertyName; // ���
				if (path != null && propertyName != null)
				{
					Object fv = NodeConverterFactory.getInstance().pack(map.find(path),
							wrapper.getPropertyType(propertyName), attribute);
					Class clazz = wrapper.getPropertyType(propertyName);

					// ���ýڵ��Ƿ���Ҫת��
					fv = convert(fv, converters, propertyName, true);
					if (fv != null && (clazz == String.class || !(fv instanceof String)
							|| fv.toString().length() > 0))
						wrapper.setPropertyValue(propertyName, fv);
				}
			}
		}

		return v;
	}

	// ��vo �� cnode֮������ת����
	protected Object convert(Object v, Map converters, String fieldName, boolean cnode2obj)
	{
		if (v == null) return null;
		if (converters == null) return v;
		IAtomConverter atomConverter = null;
		String conname = (String) converters.get(fieldName);
		if (conname != null)
			atomConverter = (IAtomConverter) IAtomConverter.CONVERTERS.get(conname);
		if (atomConverter == null) return v;

		if (cnode2obj) return atomConverter
				.converter(v instanceof IAtomNode ? (IAtomNode) v : new AtomNode(v), true, null)
				.stringValue();
		return atomConverter.converter(v instanceof IAtomNode ? (IAtomNode) v : new AtomNode(v),
				false, null);
	}

	public boolean supportNode(INode node)
	{
		return (node instanceof ICompositeNode);
	}
}
