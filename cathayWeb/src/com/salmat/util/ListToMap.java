package com.salmat.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.salmat.pas.vo.Area;


//此類別是用來整理List成為Map，可將一個List依property的值整理成一個逐步深入的Map
//注意key值為null時
public class ListToMap {
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<Object, List> listToMap(List list, String propertyName){
    	Map<Object, List> retMap = new HashMap<Object, List>();
    	String property = propertyName;
		property = property.substring(0, 1).toUpperCase() + property.substring(1);		
    	for(Object obj : list){    		
    		try {
    			//取得回傳值
				Object returnVal = obj.getClass().getMethod("get" + property).invoke(obj);
				List retList = retMap.get(returnVal);
				if(retList == null)
					retList = new ArrayList();
				retList.add(obj);
                retMap.put(returnVal, retList);                					
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    			
    		
    	}    	
    	return retMap;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map getNextMap(Map inputMap, String propertyName){
		Set keySet = inputMap.keySet();
    	for(Object key : keySet){
    		Object obj = inputMap.get(key);
    		if(obj instanceof Map){    			
				Map retMap = getNextMap((Map)obj, propertyName);
    			inputMap.put(key, retMap);
    		}else if(obj instanceof List){
    			Map<Object, List> retMap = listToMap((List)obj, propertyName);
    			inputMap.put(key, retMap);
    		}
    	}
    	return inputMap;
    }
    
    
    public static void main(String [] args){
    	List<Area> areas = new ArrayList<Area>();
    	for(int i = 0 ; i < 30 ; i++){
    		Area area = new Area();
    		area.setAreaId(i + "");
    		if(i < 10){
    		   area.setAddress("address01");
    		}else if(i < 20){
    		   area.setAddress("address02");
    		}else{
    		   area.setAddress("address03");
    		}
    		if(i < 15){
     		    area.setServiceCenter("test1");
     		}else {
     			area.setServiceCenter("test2");
     		}
    		if(i < 5){
     		    area.setTel("001");
     		}else if(i < 17){
     			area.setTel("002");
     		}else if(i < 23){
     			area.setTel("003");
     		}else{
     			area.setTel("004");
     		}
    		areas.add(area);
    	}
    	Map<Object, List> returnMap = listToMap(areas, "serviceCenter");
    	Map myMap = getNextMap(returnMap, "address");
    	myMap = getNextMap(myMap, "tel");
    	System.out.println("");
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<Object, Map<Object, List>> deeperMap(Map<Object, List> inputMap, String propertyName){
    	Set keySet = inputMap.keySet();
    	Map<Object, Map<Object, List>> returnMap = new HashMap<Object, Map<Object, List>>(); 
    	for(Object key : keySet){
    		List list = inputMap.get(key);
    		Map regularMap = listToMap(list, propertyName);
    		returnMap.put(key, regularMap);    		
    	}    	
    	return returnMap;
    }
    
    
    
}
