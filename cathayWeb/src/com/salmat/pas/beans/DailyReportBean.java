package com.salmat.pas.beans;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.model.SelectItem;

import org.hibernate.Session;

import com.ibm.icu.util.Calendar;
import com.salmat.pas.conf.Constant;
import com.salmat.pas.report.GenerateReport;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.ApplyData;
import com.salmat.util.HibernateSessionFactory;


/*
 * Thor新增
 */
public class DailyReportBean extends BaseBean {
	private Integer year;
	private Integer month;
	private String result;
	private String center;
	private Set<SelectItem> years;
	private Set<SelectItem> months;
	private Set<SelectItem> centers;
	Calendar cal = null;
	
	public DailyReportBean (){
		cal = Calendar.getInstance();
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public int getYear() {
		
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getMonth() {
		if(month == null)
			month = cal.get(Calendar.MONTH);
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	
	public Set<SelectItem> getYears() {
		if(years == null){
			years = new LinkedHashSet<SelectItem>();			
			year = cal.get(Calendar.YEAR);
			SelectItem selectItem = new SelectItem();
			selectItem.setValue(year);
		    selectItem.setLabel(year + "");
		    years.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(year - 1);
		    selectItem.setLabel((year - 1) + "");
		    years.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(year - 2);
		    selectItem.setLabel((year - 2) + "");
		    years.add(selectItem);
		}
		return years;
	}
	
	public Set<SelectItem> getMonths() {
		if(months == null){
			months = new LinkedHashSet<SelectItem>();			
			month = cal.get(Calendar.MONTH);
			SelectItem selectItem = new SelectItem();
			selectItem.setValue(0);
		    selectItem.setLabel("一");
		    months.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(1);
		    selectItem.setLabel("二");
		    months.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(2);
		    selectItem.setLabel("三");
		    months.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(3);
		    selectItem.setLabel("四");
		    months.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(4);
		    selectItem.setLabel("五");
		    months.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(5);
		    selectItem.setLabel("六");
		    months.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(6);
		    selectItem.setLabel("七");
		    months.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(7);
		    selectItem.setLabel("八");
		    months.add(selectItem);
		    
		    selectItem = new SelectItem();
			selectItem.setValue(8);
		    selectItem.setLabel("九");
		    months.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(9);
		    selectItem.setLabel("十");
		    months.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(10);
		    selectItem.setLabel("十一");
		    months.add(selectItem);
		    selectItem = new SelectItem();
			selectItem.setValue(11);
		    selectItem.setLabel("十二");
		    months.add(selectItem);
		}
		return months;
	} 
	
	public String exportExcel(){
		Session session = null;
		try{
			//key = center + "_" + 20140601
			//Map<String, int[]> centerMap, String name
			Calendar dateBeg = Calendar.getInstance();
			Calendar dateEnd = Calendar.getInstance();
			dateBeg.set(this.year, this.month, 1, 0, 0, 0);
			dateBeg.set(Calendar.MILLISECOND, 0);
			dateEnd.set(this.year, this.month, 1, 0, 0, 0);
			int maxDate = dateEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
			dateEnd.set(this.year, this.month, maxDate, 0, 0, 0);
			
			
			session = HibernateSessionFactory.getSession();
            HashMap<String, int[]> map = dailyReportQuery(session, dateBeg,
					dateEnd, center);
            if(map == null){
            	setResult("所選擇的月份查無資料");
            	return null;
            }
			String targetFileNm = GenerateReport.generateDailyReport(map, year + "-" + (month + 1));
			this.getRequest().setAttribute("reportNameForDownload", targetFileNm);
			return "download";
			
		}catch(Exception e){
			logger.error("", e);
			setResult(e.getMessage());
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		
		return null;
	}
	public static HashMap<String, int[]> dailyReportQuery(Session session,
			Calendar dateBeg, Calendar dateEnd, String queryCenter) {
		String groupBy1 = " group by center, receipt, cycleDate, sourceCode, groupInsure";						
		String groupBy2 = " group by center, cycleDate";
		String groupBy4 = " group by batchOrOnline, cycleDate, center order by cycleDate";
		String queryStr1 = "select count(*), sum(totalPage), center, receipt, cycleDate, sourceCode, groupInsure from ApplyData where "
				+ "cycleDate between ? and ? and center <> '00'  and ((receipt = true and newBatchName not like '%9999' and newBatchName is not null) or receipt = false or receipt is null)";			
		String queryStr2 = "select count(*), center, cycleDate from ApplyData where "
				+ "cycleDate between ? and ? and (policyStatus >= '35' or policyStatus = '100') and receipt = false and center <> '00' and (groupInsure = false or groupInsure is null)"; //已生產
		String queryStr3 = "select count(*), center, cycleDate from ApplyData where "
				+ "cycleDate between ? and ? and policyStatus < '35' and policyStatus <> '100' and receipt = false and center <> '00' and (groupInsure = false or groupInsure is null)"; //尚未生產			
		String queryStr4 = "select count(*), batchOrOnline, cycleDate, center from LogisticStatus where cycleDate between ? and ? ";
		String queryStr5 = "select count(*), batchOrOnline, cycleDate, center from PackStatus where cycleDate between ? and ? ";
		

		if(queryCenter != null && !"".equals(queryCenter)){
			queryStr1 = (queryStr1 + " and center = '" + queryCenter + "' ") ;
			queryStr2 = (queryStr2 + " and center = '" + queryCenter + "' ") ;
			queryStr3 = (queryStr3 + " and center = '" + queryCenter + "' ") ;
			queryStr4 = (queryStr4 + " and center = '" + queryCenter + "' ") ;
			queryStr5 = (queryStr5 + " and center = '" + queryCenter + "' ") ;
		}
		queryStr1 += groupBy1;
		queryStr2 += groupBy2;
		queryStr3 += groupBy2;
		queryStr4 += groupBy4;
		queryStr5 += groupBy4;
		
		List<Object []> list1 = session.createQuery(queryStr1).setParameter(0, new Date(dateBeg.getTimeInMillis())).setParameter(1, new Date(dateEnd.getTimeInMillis())).list();
		List<Object []> list2 = session.createQuery(queryStr2).setParameter(0, new Date(dateBeg.getTimeInMillis())).setParameter(1, new Date(dateEnd.getTimeInMillis())).list();
		List<Object []> list3 = session.createQuery(queryStr3).setParameter(0, new Date(dateBeg.getTimeInMillis())).setParameter(1, new Date(dateEnd.getTimeInMillis())).list();
		List<Object []> list4 = session.createQuery(queryStr4).setParameter(0, new Date(dateBeg.getTimeInMillis())).setParameter(1, new Date(dateEnd.getTimeInMillis())).list();
		List<Object []> list5 = session.createQuery(queryStr5).setParameter(0, new Date(dateBeg.getTimeInMillis())).setParameter(1, new Date(dateEnd.getTimeInMillis())).list();
		HashMap<String, int[]> map = new HashMap<String, int[]>();
		
		if(list1 == null || list1.size() == 0){			
			return null;
		}
		for(Object[] object : list1){
			String date = Constant.yyyyMMdd.format((Date)object[4]);
			String center = (String)object[2];
			int pages = 0;
			if(object[1] != null)
				pages = ((Long)object[1]).intValue();
			int books = 0;
			if(object[0] != null)
			   books = ((Long)object[0]).intValue();
			Boolean receipt = (Boolean)object[3];				 
			String sourceCode = (String)object[5];
			Boolean groupInsure = (Boolean)object[6] == null? false : (Boolean)object[6];
		    String key = center + "_" + date;
		    int [] inputNum = null;
		    if(map.get(key) == null){
		    	inputNum = new int[18];
		    }else{
		    	inputNum = map.get(key); 
		    }
		    if(receipt != null && !receipt && (sourceCode.toUpperCase().equals("NORM") || sourceCode.toUpperCase().equals("REPT"))){
		    	inputNum[0] += books;			    	
		    	inputNum[3] += pages;
		    	inputNum[6] += (pages / 2 + pages % 2);
		    	inputNum[9] += (pages / 2 + pages % 2);
		    }else if(receipt != null && !receipt && (sourceCode.toUpperCase().equals("REIS") || sourceCode.toUpperCase().equals("CONV"))){
		    	inputNum[1] += books;			    	
		    	inputNum[4] += pages;
		    	inputNum[7] += (pages / 2 + pages % 2);
		    	inputNum[9] += (pages / 2 + pages % 2);
		    }else if(receipt != null && !receipt && !groupInsure && sourceCode.toUpperCase().equals("GROUP")){
		    	inputNum[2] += books;
		    	inputNum[5] += pages;
		    	inputNum[8] += (pages / 2 + pages % 2);
		    	inputNum[9] += (pages / 2 + pages % 2);
		    }else if(receipt != null && receipt && !groupInsure && (sourceCode.toUpperCase().equals("NORM") || sourceCode.toUpperCase().equals("REPT") )){
		    	inputNum[13] += pages;
		    }else if(groupInsure && sourceCode.toUpperCase().equals("GROUP")){
		    	inputNum[14] += pages;
		    }else if(receipt != null && receipt && !groupInsure && sourceCode.toUpperCase().equals("GROUP") ){
		    	inputNum[15] += pages;
		    }				
		    map.put(key, inputNum);
		}
		for(Object[] object : list2){
			String date = Constant.yyyyMMdd.format((Date)object[2]);
			String center = (String)object[1];
			int books = 0;
			if(object[0] != null)
			   books = ((Long)object[0]).intValue();				
		    String key = center + "_" + date;
		    int [] inputNum = null;
		    if(map.get(key) == null){
		    	inputNum = new int[18];
		    }else{
		    	inputNum = map.get(key); 
		    }
		    inputNum[16] += books;
		    map.put(key, inputNum);
		}
		for(Object[] object : list3){
			String date = Constant.yyyyMMdd.format((Date)object[2]);
			String center = (String)object[1];
			int books = 0;
			if(object[0] != null)
			   books = ((Long)object[0]).intValue();				
		    String key = center + "_" + date;
		    int [] inputNum = null;
		    if(map.get(key) == null){
		    	inputNum = new int[18];
		    }else{
		    	inputNum = map.get(key); 
		    }
		    inputNum[17] += books;
		    map.put(key, inputNum);
		}	
		for(Object[] object : list5){
			String date = Constant.yyyyMMdd.format((Date)object[2]);
			String center = (String)object[3];
			String batchOrOnline = (String)object[1];
			int counts = 0;
			if(object[0] != null)
			   counts = ((Long)object[0]).intValue();				
		    String key = center + "_" + date;
		    int [] inputNum = null;
		    if(map.get(key) == null){
		    	inputNum = new int[18];
		    }else{
		    	inputNum = map.get(key); 
		    }
		    if(batchOrOnline.equals("G")){
		    	inputNum[12] = counts;
		    }else if(batchOrOnline.equals("B") && !center.equals("06")){		    	
		    	inputNum[10] = counts;
		    }
		    map.put(key, inputNum);
		}
		
		for(Object[] object : list4){
			String date = Constant.yyyyMMdd.format((Date)object[2]);
			String center = (String)object[3];
			String batchOrOnline = (String)object[1];
			int counts = 0;
			if(object[0] != null)
			   counts = ((Long)object[0]).intValue();				
		    String key = center + "_" + date;
		    int [] inputNum = null;
		    if(map.get(key) == null){
		    	inputNum = new int[18];
		    }else{
		    	inputNum = map.get(key); 
		    }
		    if(batchOrOnline.equals("G")){
		    	//團險時有二份回送的交寄清單
		    	inputNum[12] += (inputNum[2] + 5) / 23;
		    	if(((inputNum[2] + 5) % 21) > 0)
		    		inputNum[12]++;
		    }else if(batchOrOnline.equals("B") && center.equals("06")){		    	
		    	if(inputNum != null){
		    	   inputNum[10] = (inputNum[0] + 6) / 23;
		    	   if(((inputNum[0] + 5) % 21) > 0)
		    		   inputNum[10]++;
		    	}
		    }
		    map.put(key, inputNum);
		}
		
		return map;
	}
	public String getCenter() {
		return center;
	}
	public void setCenter(String center) {
		this.center = center;
	}
	
	public Set<SelectItem> getCenters() {
		if(centers == null){
			centers = new LinkedHashSet<SelectItem>();
			AdminUser adminUser = (AdminUser)this.getSession(true).getAttribute("loginUser");
			Map<String, String> centerMap = ApplyData.getCenterMap();
			if(adminUser.getCenter() == null || "".equals(adminUser.getCenter().trim())){
				SelectItem selectItem = new SelectItem();
				selectItem.setValue("");
				selectItem.setLabel("全部");
				centers.add(selectItem);
				Set<String> keySet = new TreeSet<String>(centerMap.keySet());
				for(String key : keySet){
					selectItem = new SelectItem();
					selectItem.setValue(key);
					selectItem.setLabel(centerMap.get(key));
					if(!"00".equals(key))
					   centers.add(selectItem);
				}				
			}else{
				SelectItem selectItem = new SelectItem();
				selectItem.setValue(adminUser.getCenter());
				selectItem.setLabel(centerMap.get(adminUser.getCenter()));
				centers.add(selectItem);
			}
		}
		return centers;
	}	
	
}
