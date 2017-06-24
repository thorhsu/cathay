package com.salmat.pas.beans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;









import javax.faces.component.UIData;
import javax.faces.model.ListDataModel;

import jxl.Sheet;
import jxl.Workbook;

import org.apache.myfaces.component.html.ext.SortableModel;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.testng.log4testng.Logger;

import com.salmat.pas.filter.ServletContextGetter;
import com.salmat.pas.report.GenerateReport;
import com.salmat.pas.vo.Area;
import com.salmat.util.HibernateSessionFactory;

public class AreaBean extends BaseBean {

	Logger logger = Logger.getLogger(AreaBean.class);
	private String result; // 後端處理結果
	private UploadedFile uploadFile;
	private UIData dataTable;
	private SortableModel dataModel;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public UploadedFile getUploadFile() {
		return uploadFile;
	}

	public void setUploadFile(UploadedFile uploadFile) {
		this.uploadFile = uploadFile;
	}
	public String doQuery(){
		Session session = null;
		try{
			session = HibernateSessionFactory.getSession();
			List<Area> list = session.createCriteria(Area.class).list();
			setDataModel(new SortableModel(new ListDataModel(list)));
		}catch(Exception e){
			e.printStackTrace();
			logger.error("", e);
		}finally{
			if(session != null)
				session.close();
			session = null;
		}
		return null;
	}

	public String upload() {	
		setResult("");
		OutputStream out = null;
		InputStream in = null;
		File detFile = null;
		try {
			in = uploadFile.getInputStream();
			detFile = new File(ServletContextGetter.getRealPath("/pdf")
					+ UUID.randomUUID() + ".xls");
			if (!detFile.getParentFile().exists())
				detFile.getParentFile().mkdirs();
			out = new FileOutputStream(detFile);
			byte[] buf = new byte[4096];

			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
			setResult("資料庫更新失敗:" + e);
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
		}

		Session session = null;
		Transaction tx = null;
		
		try {
			in = new FileInputStream(detFile);
			jxl.Workbook rwb = Workbook.getWorkbook(in);
			Sheet sheet = rwb.getSheet(0);
			session = HibernateSessionFactory.getSession();			
		    tx = session.beginTransaction();
		    
		    int rows = sheet.getRows();
		    
		    String areaIdT = "單位代號";
		    try{
		       areaIdT= sheet.getCell(0, 0).getContents().trim();
		    }catch(Exception e){
     	    	logger.error("", e);
     	    }
     	    String areaNameT = "單位名稱(簡稱)";
     	    try{
     	    	areaNameT = sheet.getCell(1, 0).getContents().trim();
     	    }catch(Exception e){
     	    	logger.error("", e);
     	    }
     	    String zipCodeT = "郵遞區號";
     	    try{
     	       zipCodeT = sheet.getCell(2, 0).getContents().trim();
     	   }catch(Exception e){
    	    	logger.error("", e);
    	    }
     	    String addressT = "單位地址";
     	    try{
     	       addressT = sheet.getCell(3, 0).getContents().trim();
     	    }catch(Exception e){
    	    	logger.error("", e);
    	    }
     	    String telT = "單位電話1";
     	    try{
     	       telT = sheet.getCell(4, 0).getContents().trim();
     	    }catch(Exception e){
    	    	logger.error("", e);
    	    }
     	    String serviceCenterT = "所屬服務中心代號";
     	    try{
     	       serviceCenterT = sheet.getCell(5, 0).getContents().trim();
     	    }catch(Exception e){
    	    	logger.error("", e);
    	    }
     		String  serviceCenterNmT = "所屬服務中心名稱(簡稱)";
     		try{
     		   serviceCenterNmT = sheet.getCell(6, 0).getContents().trim();
     		}catch(Exception e){
     	    	logger.error("", e);
     	    }
     	    
     	    String independentT = "獨立課區判別碼";
     	    try{
     	       independentT =sheet.getCell(7, 0).getContents().trim();
     	    }catch(Exception e){
    	    	logger.error("", e);
    	    }
     	    
     	    if(!"單位代號".equals(areaIdT) || !"單位名稱(簡稱)".equals(areaNameT) 
     	    		|| !"郵遞區號".equals(zipCodeT) || !"單位地址".equals(addressT)
     	    		|| !"單位電話1".equals(telT) || !"所屬服務中心代號".equals(serviceCenterT)
     	    		|| !"所屬服務中心名稱(簡稱)".equals(serviceCenterNmT) || !"獨立課區判別碼".equals(independentT)){
                setResult("第一行各欄名稱必須為─單位代號|單位名稱(簡稱)|郵遞區號|單位地址|單位電話1|所屬服務中心代號|所屬服務中心名稱(簡稱)|獨立課區判別碼。否則無法接受");     	    	
     	    	return "failure";
     	    }

		    Area area = null;
		    //從第二行開始
            for(int i = 1 ; i < rows ; i ++){
            	if(!sheet.getCell(0, i).getContents().trim().equals("")){
					String areaId = sheet.getCell(0, i).getContents().trim();

					String areaName = "";
					try {
						areaName = sheet.getCell(1, i).getContents().trim();
					} catch (Exception e) {
						logger.error("", e);
					}
					String zipCode = "";
					try {
						zipCode = sheet.getCell(2, i).getContents().trim();
					} catch (Exception e) {
						logger.error("", e);
					}
					String address = "";
					try {
						address = sheet.getCell(3, i).getContents().trim();
					} catch (Exception e) {
						logger.error("", e);
					}
					String tel = "";
					try {
						tel = sheet.getCell(4, i).getContents().trim();
					} catch (Exception e) {
						logger.error("", e);
					}
					String serviceCenter = "";
					try {
						serviceCenter = sheet.getCell(5, i).getContents()
								.trim();
					} catch (Exception e) {
						logger.error("", e);
					}
					String serviceCenterNm = "";
					try {
						serviceCenterNm = sheet.getCell(6, i).getContents()
								.trim();
					} catch (Exception e) {
						logger.error("", e);
					}
					Boolean independent = false;
					try {
						independent = (sheet.getCell(7, i).getContents().trim()
								.equals("") || sheet.getCell(7, i)
								.getContents().trim().equals("0")) ? false
								: true;
					} catch (Exception e) {
						logger.error("", e);
					}
          	       area = (Area) session.get(Area.class, areaId);
            	   if(area == null){
            		   area = new Area();
            		   area.setAreaId(areaId);
            	   }            	   
            	   area.setAreaName(areaName);            	   
            	   area.setZipCode(zipCode);
            	   area.setAddress(address);
            	   area.setTel(tel);
            	   
            	   area.setServiceCenter(serviceCenter);
            	   area.setServiceCenterNm(serviceCenterNm);
            	   area.setIndependent(independent);
            	   if(independent && areaId.length() >= 5){
            		   area.setSubAreaId(areaId.substring(0, 5));
            	   }else if(!independent && areaId.length() >= 4){
            		   area.setSubAreaId(areaId.substring(0, 4));
            	   }else{
            		   area.setSubAreaId(null);
            	   }
            	   
            	   session.saveOrUpdate(area);
            	}
            }
            tx.commit();
            /*
            String queryStr = "select count(*), address from Area where areaId in (select distinct serviceCenter from Area) and address is not null and address <> ''  group by address having count(*) > 1";
            List<Object[]> list = session.createSQLQuery(queryStr).list();
            for(Object[] row : list){
            	List<Area> areas = session.createQuery("from Area where areaId in (select distinct serviceCenter from Area) and address = '" + row[1] + "' ").list();
            	//看看服務中心名稱是不是相同
            	HashSet<String> areaNms = new HashSet<String>();
            	for(Area centerArea : areas){
            		if(centerArea.getAreaName() != null){
            		   areaNms.add(centerArea.getAreaName().trim());
            		}
            	}
            	//服務中心名稱都相同就不用管了
            	if(areaNms.size() == 0 || areaNms.size() == 1)
            		continue;
            	
            	for(int i = 0 ; i < areas.size() ; i++){
            	   if(i == 0){
            		   
            	   }else if(i ==){
            		   
            	   }
            	}
            	
            }
            */            
			setResult("上傳成功，資料庫已更新");
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
			setResult(e.getMessage());
			if (tx != null)
				tx.rollback();
			return "failure";
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			if (session != null && session.isOpen())
				session.close();
			if (detFile != null && detFile.exists())
				detFile.delete();
		}

		return "success";
	}

	public UIData getDataTable() {
		return dataTable;
	}

	public void setDataTable(UIData dataTable) {
		this.dataTable = dataTable;
	}

	public SortableModel getDataModel() {
		return dataModel;
	}

	public void setDataModel(SortableModel dataModel) {
		this.dataModel = dataModel;
	}
	
    public String downLoadExcel(){
    	Session session = null;
    	try{
    		session = HibernateSessionFactory.getSession();
    		List<Area> areas = session.createCriteria(Area.class).addOrder(Order.asc("areaId")).list();
    		String fileNm = GenerateReport.generateAreaExcel(areas);
    		this.getRequest().setAttribute("reportNameForDownload", fileNm);
    		
    	}catch(Exception e){
    		logger.error("", e);
    	}finally{
    		if(session != null)
    			session.close();
    	}
		
		
	    return "download";
	}	

}
