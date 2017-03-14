package spc.esb.model;

import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import spc.webos.service.resallocate.Resource;

@Entity
@Table(name = "esb_flowctrl")
public class FlowCtrlPO implements Resource
{
	public static final long serialVersionUID = 20110602L;
	// ��������Ӧ�ֶε�����
	@Column
	String groupId; //
	@Column
	Integer count; //
	@Column
	String status; //
	@Column
	String remark; //
	@Id
	@Column
	Long seq; //

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
	// �ʹ�VO�����������VO����

	public String getGroupId()
	{
		return groupId;
	}

	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}

	public Integer getCount()
	{
		return count;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public Long getSeq()
	{
		return seq;
	}

	public void setSeq(Long seq)
	{
		this.seq = seq;
	}

	public String getUserCd()
	{
		return userCd;
	}

	public void setUserCd(String userCd)
	{
		this.userCd = userCd;
	}

	public String getLastUpdTm()
	{
		return lastUpdTm;
	}

	public void setLastUpdTm(String lastUpdTm)
	{
		this.lastUpdTm = lastUpdTm;
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

	protected transient Pattern pattern;

	public boolean match(String key)
	{
		if (pattern == null) pattern = Pattern.compile(group());
		return pattern.matcher(key).matches();
	}

	public String group()
	{
		return groupId;
	}

	public String id()
	{
		return String.valueOf(count);
	}
	
	public void set(FlowCtrlPO vo)
	{
		this.groupId = vo.groupId;
		this.count = vo.count;
		this.status = vo.status;
		this.remark = vo.remark;
		this.seq = vo.seq;
		this.userCd = vo.userCd;
		this.lastUpdTm = vo.lastUpdTm;
		this.verDt = vo.verDt;
		this.verStatus = vo.verStatus;
		this.actionNm = vo.actionNm;
	}
	
	public Object clone()
	{
		FlowCtrlPO obj = new FlowCtrlPO();
		obj.set(this);
		return obj;
	}
}