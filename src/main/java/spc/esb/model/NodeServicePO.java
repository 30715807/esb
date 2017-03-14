package spc.esb.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import spc.esb.common.service.Route;
import spc.webos.util.StringX;

@Entity
@Table(name = "esb_nodeservice")
public class NodeServicePO implements Serializable, Route
{
	public static final long serialVersionUID = 20090722L;
	// ��������Ӧ�ֶε�����
	@Id
	@Column
	String mbrCd; // ����
	@Id
	@Column
	String appCd; // ����
	@Id
	@Column
	String msgCd; // ����

	@Column
	String attr;
	@Column
	String qname; //
	@Column
	String routeRule; //
	@Column
	String routeBeanName;
	@Column
	String location;
	@Column
	String asynRepQName;
	@Column
	String asynRepLocation;

	// version��Ϣ
	@Column
	String userCd;// �����û�
	@Column
	String lastUpdTm;// ������ʱ��
	@Column
	String verDt; // ���ݰ汾����
	@Column
	String verStatus;// ���ݰ汾״̬
	@Column
	String actionNm;// ��������

	public NodeServicePO()
	{
	}

	public String getMbrCd()
	{
		return mbrCd;
	}

	public void setMbrCd(String mbrCd)
	{
		this.mbrCd = (StringX.nullity(mbrCd) ? " " : mbrCd);
	}

	public String getMsgCd()
	{
		return msgCd;
	}

	public void setMsgCd(String msgCd)
	{
		this.msgCd = msgCd;
	}

	public String getAppCd()
	{
		return appCd;
	}

	public void setAppCd(String appCd)
	{
		this.appCd = appCd;
	}

	public String getAttr()
	{
		return attr;
	}

	public void setAttr(String attr)
	{
		this.attr = attr;
	}

	public String getQname()
	{
		return qname;
	}

	public void setQname(String qname)
	{
		if (StringX.null2emptystr(qname).trim().length() > 0)
			this.qname = StringX.null2emptystr(qname).trim();
	}

	public String getLastUpdTm()
	{
		return lastUpdTm;
	}

	public void setLastUpdTm(String lastUpdTm)
	{
		this.lastUpdTm = lastUpdTm;
	}

	public String getUserCd()
	{
		return userCd;
	}

	public void setUserCd(String userCd)
	{
		this.userCd = userCd;
	}

	public String getRouteRule()
	{
		return routeRule;
	}

	public void setRouteRule(String routeRule)
	{
		if (StringX.null2emptystr(routeRule).trim().length() > 0)
			this.routeRule = StringX.null2emptystr(routeRule).trim();
	}

	public String getRouteBeanName()
	{
		return routeBeanName;
	}

	public void setRouteBeanName(String routeBeanName)
	{
		if (StringX.null2emptystr(routeBeanName).trim().length() > 0)
			this.routeBeanName = StringX.null2emptystr(routeBeanName).trim();
	}

	public String getFtlRule()
	{
		return routeRule;
	}

	public boolean isValidRoute()
	{
		return !StringX.nullity(qname) || !StringX.nullity(routeRule)
				|| !StringX.nullity(routeBeanName);
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public String getAsynRepQName()
	{
		return asynRepQName;
	}

	public void setAsynRepQName(String asynRepQName)
	{
		this.asynRepQName = asynRepQName;
	}

	public String getAsynRepLocation()
	{
		return asynRepLocation;
	}

	public void setAsynRepLocation(String asynRepLocation)
	{
		this.asynRepLocation = asynRepLocation;
	}

	public String getVerDt()
	{
		return verDt;
	}

	public void setVerDt(String verDt)
	{
		this.verDt = verDt;
	}

	public String getVerStatus()
	{
		return verStatus;
	}

	public void setVerStatus(String verStatus)
	{
		this.verStatus = verStatus;
	}

	public String getActionNm()
	{
		return actionNm;
	}

	public void setActionNm(String actionNm)
	{
		this.actionNm = actionNm;
	}
}
