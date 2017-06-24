package com.salmat.util;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/*
 * Thor wrote it，專門用來處理傳給jqGrid的JSON字串的類別
 */
public class ToJqGridString {

	/*
	 * local data專用
	 */
	public static <T> String beansToJqGridLocalData(List<T> javaBeans){
		String returnData = "[";
		int listSize = javaBeans.size();
		for(int i = 0 ; i < listSize; i++){
		   Object object = javaBeans.get(i);
		   JSONObject ja  = JSONObject.fromObject((T)object);
		   returnData += ja.toString();
		   if(i != listSize - 1)
			   returnData += ",";
		}
		returnData += "]";
		return returnData;
	}
	/*
	 * id有給值時專用
	 */
	public static String toJqGridString(String[] id, List queryList) {
		List list = new ArrayList();

		if (id.length != queryList.size())
			return null;
		for (int i = 0; i < id.length; i++) {
			InnerJqBean jqBean = new InnerJqBean();
			jqBean.setId(id[i]);

			if (!(queryList.get(i) instanceof Object[]))
				return null;
			else
				jqBean.setCell((Object[]) queryList.get(i));

			list.add(jqBean);
		}
		JSONArray ja = JSONArray.fromObject(list);
		return ja.toString();
	}

	/*
	 * id無給值時使用，自動生成id
	 */
	public static String toJqGridString(List queryList) {
		List list = new ArrayList();

		for (int i = 0; i < queryList.size(); i++) {
			InnerJqBean jqBean = new InnerJqBean();
			jqBean.setId(i + "");

			if (!(queryList.get(i) instanceof Object[]))
				return null;
			else
				jqBean.setCell((Object[]) queryList.get(i));

			list.add(jqBean);
		}
		JSONArray ja = JSONArray.fromObject(list);
		return ja.toString();
	}

	// inner bean，傳入的list必須內含Object[]才能進入此InnerBean去處理
	public static class InnerJqBean {

		private String id;
		private Object[] cell;

		public InnerJqBean() {

		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setCell(Object[] cell) {
			this.cell = cell;
		}

		public Object[] getCell() {
			return cell;
		}
	}
}
