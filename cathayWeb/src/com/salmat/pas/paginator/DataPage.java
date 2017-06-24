package com.salmat.pas.paginator;

import java.util.List;

public class DataPage {

	/**
	 * 將需要的頁的資料封裝到一個DataPage中去， 這個類表示了我們需要的一頁的資料，<br>
	 * 裏面包含有三個元素：datasetSize，startRow，和一個用於表示具體資料的List。<br>
	 * datasetSize表示了這個記錄集的總條數，查詢資料的時候，使用同樣的條件取count即可，<br>
	 * startRow表示該頁的起始行在資料庫中所有記錄集中的位置
	 */
	private int datasetSize;

	private int startRow;

	@SuppressWarnings("unchecked")
	private List data;

	/**
	 * Create an object representing a sublist of a dataset.
	 * @param datasetSize is the total number of matching rows available.
	 * @param startRow is the index within the complete dataset of the first element
	 *        in the data list.
	 * @param data is a list of consecutive objects from the dataset.
	 */
	@SuppressWarnings("unchecked")
	public DataPage(int datasetSize, int startRow, List data) {
		this.datasetSize = datasetSize;
		this.startRow = startRow;
		this.data = data;
	}

	/**
	 * Return the number of items in the full dataset.
	 */
	public int getDatasetSize() {
		return datasetSize;
	}

	/**
	 * Return the offset within the full dataset of the first element in the
	 * list held by this object.
	 */
	public int getStartRow() {
		return startRow;
	}

	/**
	 * Return the list of objects held by this object, which is a continuous
	 * subset of the full dataset.
	 */
	@SuppressWarnings("unchecked")
	public List getData() {
		return data;
	}

}
