package com.fxdms.rmi.service.impl;


import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.fxdms.rmi.service.VoService;
import com.salmat.pas.bo.SchedulerService;
import com.salmat.pas.conf.Constant;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.BankReceipt;
import com.salmat.pas.vo.ImgFile;
import com.salmat.pas.vo.PackStatus;
import com.salmat.pas.vo.Properties;
import com.salmat.util.HibernateSessionFactory;

public class VoServiceImpl implements VoService{
	public VoServiceImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	Logger logger = Logger.getLogger(VoServiceImpl.class);
	
	@Override
	public boolean persist(Object obj) {
		Session session = null;
		Transaction tx = null;		
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        tx = session.beginTransaction();
	        session.saveOrUpdate(obj);
	        tx.commit();
	        return true;
	    }catch(Exception e){
	    	if(tx != null)
	    		tx.rollback();
	    	logger.error("", e);
	    	e.printStackTrace();
            return false;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}
	
	@Override
	public boolean update(Object obj){
		Session session = null;
		Transaction tx = null;		
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        tx = session.beginTransaction();
	        session.update(obj);
	        tx.commit();
	        return true;
	    }catch(Exception e){
	    	if(tx != null)
	    		tx.rollback();
	    	logger.error("", e);
	    	e.printStackTrace();
            return false;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}
	@Override
	public boolean save(Object obj){
		Session session = null;
		Transaction tx = null;		
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        tx = session.beginTransaction();
	        session.save(obj);
	        tx.commit();
	        return true;
	    }catch(Exception e){
	    	if(tx != null)
	    		tx.rollback();
	    	logger.error("", e);
	    	e.printStackTrace();
            return false;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}

	@Override
	public ApplyData getApplyData(String oldBatchName) {
		Session session = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return (ApplyData)session.get(ApplyData.class, oldBatchName);		
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}

	@Override
	public Properties getProperties() {
	    Session session = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return (Properties)session.get(Properties.class, 1);		
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}

	@Override
	public List<Area> getAreaList() {
		//Area.findHaveAddress
		Session session = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("Area.findHaveAddress").list();		
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}

	@Override
	public List<ImgFile> getImgFiles() {
		Session session = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.createQuery("from ImgFile").list();		
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}

	@Override
	public Object get(Class inClass, Object obj) {
		Session session = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.get(inClass, (Serializable) obj);		
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}

	@Override
	public List<ImgFile> getImgFilesByNm(String imgFileNm) {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ImgFile.findByFileNm").setString(0, imgFileNm).list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
		
	}

	@Override
	public List<ImgFile> findByImage() {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ImgFile.findByImage").list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }		
	}

	@Override
	public List<ImgFile> findByLaw() {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ImgFile.findByLaw").list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
	}

	@Override
	public List<Integer> findAfpMaxSerialNo(Calendar cal, String center,
			String batchOrTest) {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("AfpFile.findMaxSerialNo").setDate(0, cal.getTime()).setString(1, center).setString(2, batchOrTest).list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
	}

	@Override
	public List<Long> findMaxBatNo() {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("AfpFile.findMaxBatNo").list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
	}

	@Override
	public List<Integer> findAfpMaxReceiptSerialNo(Calendar cal, String center,
			String batchOrTest) {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("AfpFile.findMaxReceiptSerialNo").setDate(0, cal.getTime()).setString(1, center).setString(2, batchOrTest).list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
	}

	@Override
	public AfpFile getAfp(String newBatchName) {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return (AfpFile) session.get(AfpFile.class, newBatchName);
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	

	}

	@Override
	public List<ApplyData> findByApplyNoAndPolicyNoAndCenter(String applyNo,
			String policyNo, String center, boolean receipt) {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ApplyData.findByApplyNoAndPolicyNoAndCenter")
			         .setString(0, applyNo).setString(1, "%," + policyNo + ",%").setString(2, center).setBoolean(3, receipt).list();

	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	

	}
	
	@Override
	public Set<ApplyData> getApplyDataByNewBatchNm(String newBatchName){
		Session session = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        List<ApplyData> applyDatas = session.getNamedQuery("ApplyData.findByNewBatchName")
			         .setString(0, newBatchName).list();
	        return new LinkedHashSet(applyDatas);

	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
		
	}

	@Override
	public String getPdfPwd() {
		// TODO Auto-generated method stub
		return Constant.getPdfpwd();
	}

	@Override
	public List<AfpFile> findNotFeedBack() {
		Session session = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        List<AfpFile> afpFiles = session.getNamedQuery("AfpFile.findNotFeedBack").list();
	        return afpFiles;

	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
	}

	@Override
	public Area getArea(String areaId) {
		Session session = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        Area area = (Area) session.get(Area.class, areaId);
            return area;

	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	

	}

	@Override
	public void updateAreaCenter(Map<String, String> areaMap) {
		// areaMap<areaId, serviceCenter>
		if (areaMap == null)
			return;
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateSessionFactory.getHibernateTemplate()
					.getSessionFactory().openSession();

			Set<String> keys = areaMap.keySet();
			List<Set<String>> keysList = new ArrayList<Set<String>>();
			List<Set<String>> serviceCenterList = new ArrayList<Set<String>>();
			Set<String> splitKeys = new HashSet<String>();
			int i = 0;
			for (String key : keys) {
				i++;
				if (i > 1999) {
					i = 0;
					keysList.add(splitKeys);
					splitKeys = new HashSet<String>();
				}
				splitKeys.add(key);				
			}
			keysList.add(splitKeys);
			for (Set<String> keySeperates : keysList) {
				tx = session.beginTransaction();
				Query query = session
						.createQuery("from Area where areaId in(:areaIds)");
				query.setParameterList("areaIds", keySeperates);
				List<Area> areas = query.list();
				for (Area area : areas) {
					String areaId = area.getAreaId();
					String serviceCenterId = areaMap.get(areaId);
					Area serviceCenter = null;
					if(serviceCenterId != null)
						serviceCenter = (Area) session.get(Area.class, serviceCenterId);
					if (serviceCenter == null && serviceCenterId != null && serviceCenterId.length() == 7) {
						serviceCenter = new Area();
						serviceCenter.setAreaId(serviceCenterId);
						serviceCenter.setSubAreaId(serviceCenterId.substring(0, 4));
						serviceCenter.setAddress(serviceCenterId + "服務中心無地址");
						serviceCenter.setAreaName("");
						serviceCenter.setIndependent(false);
						serviceCenter.setServiceCenter("");
						serviceCenter.setServiceCenterNm("");
						serviceCenter.setTel("");
						serviceCenter.setZipCode("");
						session.save(serviceCenter);
					}else if(serviceCenter != null && (serviceCenter.getAddress() == null || serviceCenter.getAddress().trim().equals(""))){
						serviceCenter.setAddress(serviceCenterId + "服務中心無地址");
						session.update(serviceCenter);
					}
					if(serviceCenter != null){
					   area.setServiceCenter(serviceCenterId);
					   area.setServiceCenterNm(serviceCenter.getAreaName());
					}else{
						area.setServiceCenter("");
						area.setServiceCenterNm("");
					}
					session.update(area);
				}
				tx.commit();
			}

		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
			if (tx != null)
				tx.rollback();
		} finally {
			if(session != null && session.isOpen())
				session.close();
		}
	}

	@Override
	public Map<String, Area> getCenterAreaMap() {
		//服務中心的地址對照表
		 HashMap<String, Area> centerMap = new HashMap<String, Area>();
		 Session session = null;
         try{
        	session = HibernateSessionFactory.getHibernateTemplate()
 					.getSessionFactory().openSession();
		    List<Area> serviceCenters = session.getNamedQuery("Area.findServiceCenter").list();
			if(serviceCenters != null){
				for(Area area : serviceCenters){
					centerMap.put(area.getAreaId(), area);
				}
			}
			return centerMap;	
         }catch(Exception e){
        	 logger.error("", e);
        	 return centerMap;
         }finally{
        	 if(session != null && session.isOpen())
        		 session.close();
         }
	}

	@Override
	public void deleteImg(String imgNm) {
		Session session = null;
		Transaction tx = null;
        try{
       	    session = HibernateSessionFactory.getHibernateTemplate()
					.getSessionFactory().openSession();       	           	           	    
            Query query = session.getNamedQuery("ImgFile.findByFileNm");
            query.setString(0, imgNm);
            List<ImgFile> list = query.list();
            if(list != null && list.size() > 0){
            	for(ImgFile imgFile : list){
            		tx = session.beginTransaction();
            		//先刪中介table的資料
            		SQLQuery deleteQuery = (SQLQuery) session.createSQLQuery("delete from imgMetaTable where imgId = " + imgFile.getImgId());
            		deleteQuery.executeUpdate();
            		tx.commit();
            		tx = session.beginTransaction();
            		Query imgDelete = session.createQuery("delete from ImgFile where imgId = " + imgFile.getImgId());
            		imgDelete.executeUpdate();
            		tx.commit();
            		logger.info("delete image :" + imgNm);
            	}
            }       	    
        }catch(Exception e){
       	   logger.error("", e);
       	    if(tx != null)
       	    	tx.rollback();
        }finally{
       	    if(session != null && session.isOpen())
       		    session.close();
        }
		
	}

	@Override
	public List<ApplyData> findByApplyNoAndPolicyNoAndCenerCycle(
			Date cycleDate, String applyNo, String policyNo, String center,
			boolean receipt) {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ApplyData.findByApplyNoAndPolicyNoAndCenerCycle")
			         .setString(0, applyNo).setString(1, "%," + policyNo + ",%").setString(2, center).setBoolean(3, receipt).setDate(4, cycleDate).list();

	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
	}
    
	@Override
	public List<ApplyData> findByApplyNoAndPolicyNoAndCenerCycleReprint(
			Date cycleDate, String applyNo, String policyNo, String center,
			boolean receipt, Integer reprint) {
		Session session = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ApplyData.findByApplyNoAndPolicyNoAndCenerCycleReprint")
			         .setString(0, applyNo).setString(1, "%," + policyNo + ",%").setString(2, center).setBoolean(3, receipt).setDate(4, cycleDate).setInteger(5, reprint).list();

	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
   }

	@Override
	public Map<String, Integer> getReportNum(Set<String> packIds) {
		Session session = null;
	    try{
	    	HashMap<String, Integer> returnMap = new HashMap<String, Integer>();
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        //"ApplyData.groupByPackIdAndSource", query="select count(*), packId, sourceCode, center, cycleDate from ApplyData where packId in (:packIds) group by packId, sourceCode, center, cycleDate order by sourceCode"
	        List<Object[]> list = session.getNamedQuery("ApplyData.groupByPackIdAndSource").setParameterList("packIds", packIds).list();	        
	        for(Object[] result: list){
	        	String sourceCode = (String)result[2];
	        	String center = (String)result[3];
	        	String cycleDate = Constant.yyyyMMdd.format((Date)result[4]);
	        	String key = sourceCode + "_" + center + "_" + cycleDate;
	        	Integer count = returnMap.get(key) == null? 0 : returnMap.get(key);
	        	count++;
	        	returnMap.put(key, count);	        	
	        }
	        return returnMap;

	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}

	@Override
	public List<PackStatus> findNonReported() {
		// PackStatus.findNonReported", query="from PackStatus where reported = false
		Session session = null;
	    try{	    	
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();	        
	        return session.getNamedQuery("PackStatus.findNonReported").list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}

	@Override
	public Map<String, Integer> generateReport(Set<String> newBatchNames) {
		Session session = null;
	    try{
	    	HashMap<String, Integer> returnMap = new HashMap<String, Integer>();
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        //"ApplyData.generateReport", query="select count(*), cycleDate, sourceCode, center, subAreaId from applyData where newBatchName in (:newBatchNames) group by cycleDate, sourceCode, center, subAreaId"
	        List<Object[]> list = session.getNamedQuery("ApplyData.generateReport").setParameterList("newBatchNames", newBatchNames).list();	        
	        for(Object[] result: list){
	        	String sourceCode = (String)result[2];
	        	String center = (String)result[3];
	        	String cycleDate = Constant.yyyyMMdd.format((Date)result[1]);
	        	String key = sourceCode + "_" + center + "_" + cycleDate;
	        	Integer count = returnMap.get(key) == null? 0 : returnMap.get(key);
	        	count++;
	        	returnMap.put(key, count);	        	
	        }
	        return returnMap;

	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }

	}
	
	@Override
	public Set<ApplyData> getApplyDataNewBatchNm(String newBatchName){
		Session session = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        List<ApplyData> applyDatas = session.getNamedQuery("ApplyData.findNewBatchName")
			         .setString(0, newBatchName).list();
	        return new HashSet(applyDatas);

	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
		
	}

	@Override
	public boolean deleteApplyData(ApplyData applyData) {
		Session session = null;
		Transaction tx = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        tx = session.beginTransaction();
	        SQLQuery deleteQuery = (SQLQuery) session.createSQLQuery("delete from imgMetaTable where oldBatchName = '" + applyData.getOldBatchName() + "'");
    		deleteQuery.executeUpdate();
    		tx.commit();
    		tx = session.beginTransaction();
	        applyData = (ApplyData) session.load(ApplyData.class, applyData.getOldBatchName());
	        if(applyData != null){
	           session.delete(applyData);
	        }
	        tx.commit();
	        return true;

	    }catch(Exception e){
	    	logger.error("", e);
	    	if(tx != null)
	    		tx.rollback();
	    	e.printStackTrace();
	    	return false;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	

	}

	@Override
	public List<ApplyData> findForPdf(Date cycleDate) {
		Session session = null;		
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ApplyData.findForPdf").setDate(0, cycleDate).list();
	        
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}

	@Override
	public List<ApplyData> findPklike(String forSearch, Boolean receipt,
			Boolean groupInsure) {
		Session session = null;		
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(ApplyData.class);
            criteria.add(Restrictions.like("oldBatchName", forSearch));
            if(receipt != null)
            	criteria.add(Restrictions.eq("receipt", receipt));
            else 
            	criteria.add(Restrictions.isNull("receipt"));
            if(groupInsure != null)
            	criteria.add(Restrictions.eq("groupInsure", groupInsure));
            else
            	criteria.add(Restrictions.isNull("groupInsure"));
            return criteria.list();            
	        
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }
	}
	
	@Override
	public List<ImgFile> findGroupImage() throws Exception{
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ImgFile.findGroupImage").list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	throw e;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }		
	}

	@Override
	public List<ImgFile> findGroupLaw() throws Exception {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ImgFile.findGroupLaw").list();	        
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	throw e;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
	}

	@Override
	public List<ImgFile> findNormImage() throws Exception{
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ImgFile.findNormImage").list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	throw e;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }		
	}

	@Override
	public List<ImgFile> findNormLaw() throws Exception{
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ImgFile.findNormLaw").list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	throw e;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
	}
	
	@Override
	public List<ImgFile> findTestImage() {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ImgFile.findTestImage").list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }		
	}

	@Override
	public List<ImgFile> findTestLaw() {
		Session session = null;
	    try{
	    	//ImgFile.findByFileNm
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("ImgFile.findTestLaw").list();
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
	}

	@Override
	public String updateArea(List<Area> areas) {
		Session session = null;
		Transaction tx = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();	        
	        for(Area updateArea : areas){
	           tx = session.beginTransaction();
	           String areaId = updateArea.getAreaId();
	           Area area = (Area) session.get(Area.class, areaId);
         	   if(area == null){
         		   area = new Area();
         		   area.setAreaId(areaId);
         	   }  
         	   if(updateArea.getAreaName() != null)
         	      area.setAreaName(updateArea.getAreaName());
         	   if(updateArea.getZipCode() != null)
         	      area.setZipCode(updateArea.getZipCode());
         	   if(updateArea.getAddress() != null)
         	      area.setAddress(updateArea.getAddress());
         	   if(updateArea.getTel() != null)
         	      area.setTel(updateArea.getTel());
         	   
         	   String serviceCenterId = updateArea.getServiceCenter();
         	   area.setServiceCenter(serviceCenterId);
         	   Area serviceCenter = null;
         	   if(serviceCenterId != null && !"".equals(serviceCenterId))
         		   serviceCenter = (Area) session.get(Area.class, serviceCenterId);
         	   String serviceCenterNm = updateArea.getServiceCenterNm();
         	   
         	   //如果無此服務中心時，新增一個服務中心 
         	   if(serviceCenter  == null && serviceCenterId != null && !"".equals(serviceCenterId)){
         		   serviceCenter = new Area();
         		   serviceCenter.setServiceCenterNm(serviceCenterNm);
         		   serviceCenter.setAreaId(serviceCenterId);
         		   if(serviceCenterId.length() >= 4)
            		  serviceCenter.setSubAreaId(serviceCenterId.substring(0, 4));
            	   serviceCenter.setIndependent(false);
            	   serviceCenter.setServiceCenter("");
            	   serviceCenter.setServiceCenterNm("");
         		   session.save(serviceCenter);
         	   }
               
               if((serviceCenterNm == null || serviceCenterNm.equals("")) 
            		   && serviceCenter != null && serviceCenter.getServiceCenterNm() != null && !"".equals(serviceCenter.getServiceCenterNm())){
            	   serviceCenterNm = serviceCenter.getServiceCenterNm();
               }
               
         	   area.setServiceCenterNm(serviceCenterNm);
         	   boolean independent = updateArea.getIndependent() == null? false : updateArea.getIndependent();
         	   area.setIndependent(independent);
         	   if(independent && areaId.length() >= 5){
         		   area.setSubAreaId(areaId.substring(0, 5));
         	   }else if(!independent && areaId.length() >= 4){
         		   area.setSubAreaId(areaId.substring(0, 4));
         	   }else{
         		   area.setSubAreaId(null);
         	   }
         	   
         	   session.saveOrUpdate(area);
         	   if(serviceCenterId != null && !"".equals(serviceCenterId.trim()) && serviceCenter != null){
         		   //update Area set serviceCenter = ?, serviceCenterNm = ? where subAreaId = ?
         		   session.getNamedQuery("Area.updateAllCenter").setString(0, serviceCenterId).setString(1, serviceCenterNm).setString(2, area.getSubAreaId()).executeUpdate();
         	   }
   	           tx.commit();
	        }
	        return null;
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	if(tx != null)
	    		tx.rollback();
	    	return e.getMessage();
	    }finally{
	    	if(session != null && session.isOpen())
	           session.close();
	    }	
		
	}

	//找送金單
	@Override
	public BankReceipt findBk(String bankReceiptId) {
		Session session = null;
	    try{
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        BankReceipt br = (BankReceipt) session.get(BankReceipt.class, bankReceiptId);
	        return br;
	    }catch(Exception e){
		   	logger.error("", e);
		   	e.printStackTrace();
		   	return null;
		
		}finally{
		   	if(session != null && session.isOpen())
		       session.close();
		}	

	}

	@Override
	public List<AfpFile> getCycleDateAfpfiles(Date cycleDate) {
		Session session = null;
	    try{
	    	//"AfpFile.findByCycleDate", query="from AfpFile where cycleDate = ?"
	        session = HibernateSessionFactory.getHibernateTemplate().getSessionFactory().openSession();
	        return session.getNamedQuery("AfpFile.findByCycleDate").setDate(0, cycleDate).list();
	    }catch(Exception e){
		   	logger.error("", e);
		   	e.printStackTrace();
		   	return null;
		
		}finally{
		   	if(session != null && session.isOpen())
		       session.close();
		}
	}

	@Override
	public void commonReport() {
        SchedulerService.commonReport();		
	}

	@Override
	public void returnReport() {
		SchedulerService.returnReport();
		
	}

	
}
