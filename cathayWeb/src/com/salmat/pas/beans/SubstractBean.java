package com.salmat.pas.beans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;
import javax.persistence.NamedQuery;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.component.html.ext.SortableModel;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;

import com.ibm.icu.util.Calendar;
import com.salmat.pas.bo.ApplyDataService;
import com.salmat.pas.conf.Constant;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.BankReceipt;
import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.JobBag;
import com.salmat.pas.vo.LogisticStatus;
import com.salmat.pas.vo.PackStatus;
import com.salmat.util.HibernateSessionFactory;

public class SubstractBean extends BaseBean {

	private UIData dataTable;
	private SortableModel dataModel;
	private String result;
	private String uniqueNo;
	private AdminUser user;
	private Map<String, BankReceipt> bankMap;
	private HashMap<String, AdminUser> userMap = null;
	Logger logger = Logger.getLogger(SubstractBean.class);
	private List<Area> auditCenters = null;
	private String substractModifiderName;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

	/**
	 * 寫入資料庫前的資料驗證
	 * 
	 * @return 錯誤訊息
	 */
	public String validData() {
		return "";
	}

	// rowClasses="odd_row,even_row"
	public String getRowClass() {
		String classes = "";

		for (int i = 0; i < dataModel.getRowCount(); i++) {
			dataModel.setRowIndex(i);
			ApplyData applyData = (ApplyData) dataModel.getRowData();
			if (i % 2 == 0)
				classes += "even_row,";
			else
				classes += "odd_row,";

		}
		if (classes.length() > 0 && classes.endsWith(","))
			return classes.substring(0, classes.length() - 1);
		else
			return "";
	}

	/**
	 * For頁面查詢事件後呼叫
	 * 
	 * @param e
	 */
	public void resetDataScroller(ActionEvent e) {
		if (dataTable != null)
			dataTable.setFirst(0);
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public UIData getDataTable() {
		return dataTable;
	}

	public void setDataTable(UIData dataTable) {
		this.dataTable = dataTable;
	}

	public String doSubmit() {
		Date today = new Date();
		setResult("");
		Session session = null;
		Transaction tx = null;
		try {
			if (this.uniqueNo != null && !this.uniqueNo.trim().equals("")) {
				session = HibernateSessionFactory.getSession();

				ApplyData applyData = null;
				List<ApplyData> applyDatas = session.getNamedQuery("ApplyData.findByUniqueNo")
						.setString(0, this.uniqueNo).list();
				if (applyDatas != null && applyDatas.size() > 0) {
					for(ApplyData ad : applyDatas){
					   if(ad.getSubstract() != null && ad.getSubstract())
					       applyData = ad;
					}
					user = (AdminUser) this.getSession(true).getAttribute("loginUser");
					tx = session.beginTransaction();
					String[] oldBatchNameArr = new String[1];
					oldBatchNameArr[0] = applyData.getOldBatchName();
					String batchOrOnline = "B";
					if(applyData.getSourceCode() != null && applyData.getSourceCode().toUpperCase().equals("GROUP"))
						batchOrOnline = "G";						
					ApplyDataService.delegatedBack(today, session, tx, oldBatchNameArr, batchOrOnline, applyData.getCenter(), null, user, substractModifiderName);
					tx.commit();
					setResult("抽件退回國壽設定成功");

				} else {
					setResult("無此保單右上角號碼:" + this.uniqueNo);
					return null;
				}
			}
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			logger.error("", e);
			setResult("例外發生:" + e.getMessage());
		} finally {
			if (session != null)
				session.close();
		}
		return null;
	}

	public SortableModel getDataModel() {
		List<ApplyData> applyDatas = null;

		Session session = null;
		try {
			session = HibernateSessionFactory.getSession();
			applyDatas = session
					.createQuery(
							"select a from ApplyData as a left join a.packSatus as p where "
									+ "(a.packId is null or a.packId = '' or p.back = false)"
									+ " and groupInsure = false and receipt = false  and a.substract = true ")
					.list();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
			setResult("例外發生:" + e.getMessage());
		} finally {
			if (session != null)
				session.close();
		}

		setDataModel(new SortableModel(new ListDataModel(applyDatas)));
		return dataModel;
	}

	public void setDataModel(SortableModel dataModel) {
		this.dataModel = dataModel;
	}

	public String getUniqueNo() {
		return uniqueNo;
	}

	public void setUniqueNo(String uniqueNo) {
		this.uniqueNo = uniqueNo;
	}

	public String getSubstractModifiderName() {
		return substractModifiderName;
	}

	public void setSubstractModifiderName(String substractModifiderName) {
		this.substractModifiderName = substractModifiderName;
	}
	
}
