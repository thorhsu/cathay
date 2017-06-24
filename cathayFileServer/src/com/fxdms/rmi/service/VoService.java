package com.fxdms.rmi.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.ImgFile;
import com.salmat.pas.vo.Properties;

public interface VoService {
   public boolean persist(Object obj); 
   public boolean update(Object obj);
   public boolean save(Object obj);
   public ApplyData getApplyData(String oldBatchName);
   public AfpFile getAfp(String newBatchName);
   public Object get(Class inClass, Object obj);
   public Properties getProperties();
   public List<Area> getAreaList();
   public Area getArea(String areaId);
   public Map<String, String> getCenterAreaMap();
   public List<ImgFile> getImgFiles();    
   public List<ImgFile> getImgFilesByNm(String imgFileNm);
   public List<ImgFile> findByImage();
   public List<ImgFile> findByLaw();
   public List<Integer> findAfpMaxSerialNo(Calendar cal, String center, String batchOrTest);
   public List<Integer> findAfpMaxReceiptSerialNo(Calendar cal, String center, String batchOrTest);
   public List<Long> findMaxBatNo();
   public List<ApplyData> findByApplyNoAndPolicyNoAndCenter(String applyNo, String policyNo, String center, boolean receipt);
   public Set<ApplyData> getApplyDataByNewBatchNm(String newBatchName);
   public String getPdfPwd();
   public List<AfpFile> findNotFeedBack();
   public void updateAreaCenter(Map<String, String> areaMap);
   
}
