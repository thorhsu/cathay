package com.salmat.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JSonTest {

//    public void ObjectList2json(){
//        Map map = new HashMap();
//        
//        List jlist = new ArrayList();
//        JSONTestBean bean1 = new JSONTestBean("zhangbo","123123");
//        JSONTestBean bean2 = new JSONTestBean("lisi","65489");
//        Props props = new Props("127.0.0.1","8008");
//        
//        jlist.add(bean1);
//        jlist.add(bean2);
//        
//        map.put("Props", props);
//        map.put("jsonObjectList", jlist);
//        
//        JSONArray jsonArray = JSONArray.fromObject(map);   
//        System.out.println(jsonArray);   
//    }
    
    public void arr2json() {   
//        boolean[] boolArray = new boolean[] { true, false, true };   
//        JSONArray jsonArray = JSONArray.fromObject(boolArray);   
//        System.out.println(jsonArray);   
//        // prints [true,false,true]
    	List<String[]> list = new ArrayList<String[]>();
    	list.add(new String[]{"0", "首頁"});
    	list.add(new String[]{"51", "ooXX專區"});
    	list.add(new String[]{"21", "00頁頁"});
    	System.out.println(JSONArray.fromObject(list)); 
    	
    }   
   
    public void list2json() {   
        List list = new ArrayList();  
        list.add("first");   
        list.add("second");   
        JSONArray jsonArray = JSONArray.fromObject(list);   
        System.out.println(jsonArray);   
        // prints ["first","second"]   
    }   
   
    public void createJson() {   
        JSONArray jsonArray = JSONArray.fromObject("['json','is','easy']");   
        System.out.println(jsonArray);   
        // prints ["json","is","easy"]   
    }   
   
    public void map2json() {   
        Map map = new HashMap();
        map.put("name", "json");   
        map.put("bool", Boolean.TRUE);   
        map.put("int", new Integer(1));   
        map.put("arr", new String[] { "a", "b" });   
        map.put("func", "function(i){ return this.arr[i]; }");   
   
        JSONObject json = JSONObject.fromObject(map);   
        System.out.println(json);   
        // prints   
        // ["name":"json","bool":true,"int":1,"arr":["a","b"],"func":function(i){   
        // return this.arr[i]; }]   
    }   
   
    public void bean2json() {   
//        JSONObject jsonObject = JSONObject.fromObject(new JSONTestBean("zhangbo","234234"));   
//        System.out.println(jsonObject);   
        /*  
         * prints   
         * {"func1":function(i){ return this.options[i];  
         * },"pojoId":1,"name":"json","func2":function(i){ return  
         * this.options[i]; }}  
         */   
    }   
   
    public void json2bean() {   
        String json = "{name=\"json2\",func1:true,pojoId:1,func2:function(a){ return a; },options:['1','2']}";   
//        JSONObject jb = JSONObject.fromString(json);   
//        JSONObject.toBean(jb, MyBean.class);   
        System.out.println();   
    }
   
    public static void main(String[] args) {   
    	JSonTest j = new JSonTest();
        j.arr2json();
        j.map2json();
    }    
   
}
