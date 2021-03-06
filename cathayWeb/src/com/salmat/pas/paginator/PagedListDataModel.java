package com.salmat.pas.paginator;

import javax.faces.model.DataModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * A special type of JSF DataModel to allow a datatable and datascroller to page 
 * through a large set of data without having to hold the entire set of data in 
 * memory at once. 
 * <p> 
 * Any time a managed bean wants to avoid holding an entire dataset, the managed 
 * bean should declare an inner class which extends this class and implements 
 * the fetchData method. This method is called as needed when the table requires 
 * data that isn\'t available in the current data page held by this object. 
 * <p> 
 * This does require the managed bean (and in general the business method that 
 * the managed bean uses) to provide the data wrapped in a DataPage object that 
 * provides info on the full size of the dataset. 
 * 
 * 此類別被myfaces<t:dataTable>呼叫順序為：<br>
 * 1.public PagedListDataModel(int pageSize) <br>
 * 2.getRowCount()<br>
 * 3.setRowIndex()<br>
 * 4.public boolean isRowAvailable()<br>
 * 5.public Object getRowData()
 */

public abstract class PagedListDataModel extends DataModel {

	private final Log log = LogFactory.getLog("PagedListDataModel");
	int pageSize;
	int rowIndex;
	private int rowCount = -1;
	DataPage page;

	/**
	 * Create a datamodel that pages through the data showing the specified
	 * number of rows on each page.
	 */
	public PagedListDataModel(int pageSize) {
		super();
		this.pageSize = pageSize;
		this.rowIndex = -1;
		this.page = null;
	}

	/**
	 * Not used in this class; data is fetched via a callback to the fetchData
	 * method rather than by explicitly assigning a list.
	 */

	public void setWrappedData(Object o) {
		if (o instanceof DataPage) {
			this.page = (DataPage) o;
		} else {
			throw new UnsupportedOperationException("setWrappedData");
		}
	}

	public int getRowIndex() {
		return rowIndex;
	}


	/**
	 * Specify what the "current row" within the dataset is. Note that the
	 * UIData component will repeatedly call this method followed by getRowData
	 * to obtain the objects to render in the table.
	 */
	public void setRowIndex(int index) {
		rowIndex = index;
	}

	/**
	 * Return the total number of rows of data available (not just the number of
	 * rows in the current page!).
	 */

	public int getRowCount() {
		if (rowCount < 0) {
			log.info("預設rowCount:" + rowCount);
			rowCount = fetchRowCount();
			log.info("初始化rowCount:" + rowCount);
		}
		return rowCount;
	}

	/**
	 * Return a DataPage object; if one is not currently available then fetch
	 * one. Note that this doesn\'t ensure that the datapage returned includes
	 * the current rowIndex row; see getRowData.
	 */
	private DataPage getPage(String name) {
		if (page != null) {
			return page;
		}
		int rowIndex = getRowIndex();
		int startRow = rowIndex;
		if (rowIndex == -1) {
			// even when no row is selected, we still need a page
			// object so that we know the amount of data available.
			startRow = 0;
		}
		// invoke method on enclosing class
		log.info("getPage：" + name + "建立page");
		page = fetchPage(startRow, pageSize);
		return page;

	}

	/**
	 * 
	 * Return the object corresponding to the current rowIndex. If the DataPage
	 * object currently cached doesn\'t include that index then fetchPage is
	 * called to retrieve the appropriate page.
	 */
	public Object getRowData() {
		if (rowIndex < 0) {
			throw new IllegalArgumentException(
				"Invalid rowIndex for PagedListDataModel; not within page");
		}
		// ensure page exists; if rowIndex is beyond dataset size, then
		// we should still get back a DataPage object with the dataset size
		// in it
		if (page == null) {
			page = fetchPage(rowIndex, pageSize);
			rowCount = page.getDatasetSize();// 
			log.info("getRowData：建立page");
		}

		int datasetSize = page.getDatasetSize();
		int startRow = page.getStartRow();
		int nRows = page.getData().size();
		int endRow = startRow + nRows;
		if (rowIndex >= datasetSize) {
			throw new IllegalArgumentException("Invalid rowIndex");
		}
		if (rowIndex < startRow) {
			log.info("fetchPage：向前一頁撈資料,getRowData：建立page,rowIndex:" + rowIndex);
			page = fetchPage(rowIndex, pageSize);
			log.info("翻頁之前rowCount:" + rowCount);
			rowCount = page.getDatasetSize();// 
			log.info("翻頁之後rowCount:" + rowCount);
			startRow = page.getStartRow();
		} else if (rowIndex >= endRow) {
			log.info("fetchPage：向後一頁撈資料,getRowData：建立page,rowIndex:" + rowIndex);
			page = fetchPage(rowIndex, pageSize);
			log.info("翻頁之前rowCount:" + rowCount);
			rowCount = page.getDatasetSize();// 
			log.info("翻页之後rowCount:" + rowCount);
			startRow = page.getStartRow();
		}

		return page.getData().get(rowIndex - startRow);

	}

	public Object getWrappedData() {
		return page.getData();

	}

	/**
	 * Return true if the rowIndex value is currently set to a value that
	 * matches some element in the dataset. Note that it may match a row that is
	 * not in the currently cached DataPage; if so then when getRowData is
	 * called the required DataPage will be fetched by calling fetchData.
	 */
	public boolean isRowAvailable() {
		DataPage page = getPage("isRowAvailable");
		if (page == null) {
			return false;
		}
		int rowIndex = getRowIndex();
		if (rowIndex < 0) {
			return false;
		} else if (rowIndex >= page.getDatasetSize()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Method which must be implemented in cooperation with the managed bean
	 * class to fetch data on demand.
	 */

	public abstract DataPage fetchPage(int startRow, int pageSize);

	public abstract int fetchRowCount();

}