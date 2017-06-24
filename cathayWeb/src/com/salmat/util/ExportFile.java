package com.salmat.util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;

import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*
 * Thor新增，將傳入的List(必須內含Object[])轉成csv檔儲存
 */
public class ExportFile {

	private static int pagerow = 60000; //如果筆數超過60000筆則會分頁

	public static synchronized List export(String outputPath, List list, String title, String columnNms) {

		// System.out.println("size:"+list.size());


		Date now = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String fileName = "outCsv_" + dateFormat.format(now);
		String fileextname = ".csv";

		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		// FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		List<String> out = new ArrayList();

		try {
			int a = (list.size()) / pagerow;
			int b = (list.size()) % pagerow;
			if (b > 0) {
				a++;
			}
            String sLine = "";
			if(title != null)      //講座名
			   sLine = "" + title;
			sLine += "\n";
			if(columnNms != null)  //欄位名
			   sLine += columnNms;
			sLine += "\n";

			String sBody = "";
			//System.out.println("list size =" + list.size());
			for (int i = 0; i < a; i++) {
				String fullFileName = outputPath + "/" + fileName + "_" + i
						+ fileextname;
				out.add(fileName + "_" + i + fileextname);
				// fileWriter = new FileWriter(fullFileName);
				fos = new FileOutputStream(fullFileName);
				osw = new OutputStreamWriter(fos, "ms950");
				bufferedWriter = new BufferedWriter(osw);
				bufferedWriter.write(sLine);

				StringBuffer sb = new StringBuffer("");
				int limit = (pagerow * (i + 1)) - 1;
				if(i == a-1)
					limit = list.size();
				for (int z = (pagerow * i); z < limit; z++) {
					try {
						Object[] str = (Object[]) list.get(z);
						for (int w = 0; w < str.length; w++) {
							if(str[w] != null){
							   if (w == 0) {
								  sb.append(((Object) str[w]).toString());
							    } else {
							      sb.append("," + quote(((Object) str[w]).toString()));	
							    }
							}else{
								sb.append("," + " ");
							}
						}
                        sb.append("\n");
						//sBody = sBody + "\n";

					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
				// System.out.println(sBody);
				bufferedWriter.write(sb.toString());
				bufferedWriter.flush();
				osw.flush();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
//			LogUtil.getLogger().error(ex.getMessage());
		} finally {
			try {
				if (bufferedWriter != null)
					bufferedWriter.close();
				if (osw != null)
					osw.close();
				if (fos != null)
					fos.close();
			} catch (Exception ex) {
//				LogUtil.getLogger().error(ex.getMessage());
			}
		}
		return out;
	}

	private static String quote(String data) {
		if (data == null || "".equals(data.trim())) {
			data = " ";
		}
		String quoteData = "\"" + data + "\"";
		return quoteData;
	}

}
