package com.imnak0.translationhelper;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class XLSCreator {
	public static final String SAVEFILE_NAME = File.separator + "translate_request.xls";

	private WritableWorkbook workBook;
	private WritableSheet sheet;
	private File saveFile;
	private LinkedHashMap<String, LinkedHashSet<String>> hashMap;

	public XLSCreator(File saveFile, LinkedHashMap<String, LinkedHashSet<String>> hashmapList) {
		this.saveFile = saveFile;
		this.hashMap = hashmapList;
	}

	public void create() {
		try {
			initializeJXLInterface();
			writeDataIntoExcel();
			closeJXLInterface();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the JXL interface
	 */
	private void initializeJXLInterface() throws Exception {
		// Check the savefile's validation
		if (saveFile == null) {
			String resultFileName = new File(".").getCanonicalPath() + File.separator + "dropbox" + SAVEFILE_NAME;
			saveFile = new File(resultFileName);
		} else if (saveFile.exists()) {
			saveFile.delete();
		}

		workBook = Workbook.createWorkbook(saveFile);
		sheet = workBook.createSheet("Sheet1", 0);
	}

	/**
	 * Write the "string ID" and the text into the excel file.
	 */
	private void writeDataIntoExcel() throws Exception {
		createHeader();

		Iterator<String> keyItr = hashMap.keySet().iterator();
		int i = 1;
		while (keyItr.hasNext()) {
			String stringID = keyItr.next();

			LinkedHashSet<String> textSet = hashMap.get(stringID);
			Iterator<String> valueItr = textSet.iterator();

			while (valueItr.hasNext()) {
				String text = valueItr.next();

				sheet.addCell(new Label(0, i, stringID));
				sheet.addCell(new Label(1, i, text));

				i++;
			}
		}
	}

	/**
	 * Create Header for the excel (String ID / Text)
	 */
	private void createHeader() throws Exception {
		sheet.addCell(new Label(0, 0, "String ID"));
		sheet.addCell(new Label(1, 0, "Text"));
	}

	/**
	 * close the JXL Interface
	 */
	private void closeJXLInterface() throws Exception {
		workBook.write();
		workBook.close();
	}

	/**
	 * return savefile instance
	 */
	public File getSavefile() {
		return saveFile;
	}
}
