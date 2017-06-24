package com.fxdms.cathy.vo.impl;


import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.fxdms.rmi.service.VoService;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.ImgFile;
import com.salmat.pas.vo.Properties;
import com.fxdms.util.HibernateSessionFactory;

public class VoServiceImpl implements VoService{
	private static final String pdfPwd = "cathayFxdmsPreview";
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	    	if(session != null)
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
	        return new HashSet(applyDatas);

	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	if(session != null)
	           session.close();
	    }	
		
	}

	@Override
	public String getPdfPwd() {
		// TODO Auto-generated method stub
		return pdfPwd;
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
	    	if(session != null)
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
	    	if(session != null)
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
						serviceCenter.setAddress("");
						serviceCenter.setAreaName("");
						serviceCenter.setIndependent(false);
						serviceCenter.setServiceCenter("");
						serviceCenter.setServiceCenterNm("");
						serviceCenter.setTel("");
						serviceCenter.setZipCode("");
						session.save(serviceCenter);
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
			if (session != null)
				session.close();
		}
	}
	@Override
	public Map<String, String> getCenterAreaMap() {
		//服務中心的地址對照表
		 HashMap<String, String> centerMap = new HashMap<String, String>();
		 Session session = null;
         try{
        	session = HibernateSessionFactory.getHibernateTemplate()
 					.getSessionFactory().openSession();
		    List<Area> serviceCenters = session.getNamedQuery("Area.findServiceCenter").list();
			if(serviceCenters != null){
				for(Area area : serviceCenters){
					centerMap.put(area.getAreaId(), area.getAddress());
				}
			}
			return centerMap;	
         }catch(Exception e){
        	 logger.error("", e);
        	 return centerMap;
         }finally{
        	 if(session != null )
        		 session.close();
         }
	}


}
