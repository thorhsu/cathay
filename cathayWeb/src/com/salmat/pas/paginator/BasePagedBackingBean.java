package com.salmat.pas.paginator;

import javax.faces.model.DataModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.salmat.pas.beans.BaseBean;

public abstract class BasePagedBackingBean extends BaseBean {
	private final Log log = LogFactory.getLog("BasePagedBackingBean");
	protected abstract DataPage getDataPage(int startRow, int pageSize);
	public abstract int getTotalCount();
	private DataModel dataModel;
	private int i = 0;

	public DataModel getDataModel() {
		i++;
		log.info("第" + i + "次呼叫 getDataModel.");
		if (dataModel == null) {
			dataModel = new LocalDataModel(10);
		}
		return dataModel;
	}

	private class LocalDataModel extends PagedListDataModel {
		public LocalDataModel(int pageSize) {
			super(pageSize);
		}

		public int fetchRowCount() {
			return getTotalCount();
		}

		public DataPage fetchPage(int startRow, int pageSize) {
			// call enclosing managed bean method to fetch the data
			return getDataPage(startRow, pageSize);
		}
	}
}
