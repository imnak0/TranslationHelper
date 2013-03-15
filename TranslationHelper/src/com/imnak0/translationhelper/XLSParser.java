package com.imnak0.translationhelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

public class XLSParser {
	public static final String LANGUAGE = "_language_";
	private Sheet sheet;
//	private int languageCount = 0; // 번역언어 개수
	private File openFile;
	private ArrayList<LinkedHashMap<String, LinkedHashSet<String>>> hashmapList = new ArrayList<LinkedHashMap<String, LinkedHashSet<String>>>();

	/**
	 * Hashmap arrayList 를 return한다.
	 */
	public ArrayList<LinkedHashMap<String, LinkedHashSet<String>>> getHashmapList() {
		return hashmapList;
	}

	public XLSParser(File openFile) {
		super();
		this.openFile = openFile;
	}

	/**
	 * Excel 문서를 parsing 한다.
	 */
	public void parse() {
		// excel file open
		initializeJXLInterface();

		// string ID 와 각 번역 스트링의 column을 읽어와 ArrayList 형태로 생성
		createEachLanguageAsLinkedHashmap();
	}

	/**
	 * JXL library 를 이용하여 excel file을 읽어온다.
	 */
	private void initializeJXLInterface() {
		try {
			WorkbookSettings settings = new WorkbookSettings();
			settings.setEncoding("Cp1252"); // 이 값으로 인코딩하지 않으면 영어 외의 언어가 깨진다.

			if (openFile == null) {
				String fileName = new File(".").getCanonicalPath() + File.separator + "dropbox" + File.separator + "translate_result.xls";
				openFile = new File(fileName);
			}

			Workbook workbook = Workbook.getWorkbook(openFile, settings);
			Sheet[] sheetArr = workbook.getSheets();
			sheet = sheetArr[0];
		} catch (FileNotFoundException e) {
			System.out.println("dropbox\\translate_result.xls 파일을 찾을 수 없습니다. 파일 이름을 올바르게 변경하였는지 확인해 주세요.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 각각의 번역 스트링들을 ArrayList<Cell[]> 형태로 생성한다.
	 */
	private void createEachLanguageAsLinkedHashmap() {
		int languageCount = 1;
		while (checkIfLanguageExist(languageCount)) {
			createLangueCellsAsLinkedHashmap(languageCount++);
		}
	}

	/**
	 * 해당 index의 번역 언어가 존재하는지 확인한다.
	 * 존재 여부는 해당 언어의 첫번째 셀 값의 유무로 판단한다.
	 * 
	 * @param index
	 * @return
	 */
	private boolean checkIfLanguageExist(int index) {
		try {
			String checkString = sheet.getCell(index, 0).getContents();

			if (checkString == null || checkString.equals(""))
				return false;
			else
				return true;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	/**
	 * 번역 string column을 읽어와 ArrayList 로 생성 후 반환한다.
	 * 
	 * @param index : 번역 언어 순서
	 */
	private void createLangueCellsAsLinkedHashmap(int index) {
		Cell[] stringIDCell = sheet.getColumn(0);
		Cell[] textCell = sheet.getColumn(index);

		LinkedHashMap<String, LinkedHashSet<String>> tempMap = new LinkedHashMap<String, LinkedHashSet<String>>();
		tempMap.put(LANGUAGE, getLanguageName(index));

		int indexLimit = Math.min(stringIDCell.length, textCell.length);
		for (int i = 1; i < indexLimit; i++) {
			String stringID = stringIDCell[i].getContents();
			String text = textCell[i].getContents();

			// String ID가 존재하지 않는 경우 중단. JXL 라이브러리 오류로 인해 빈 cell도 읽어오는 경우가 종종 있다.
			if (stringID == null || stringID.equals(""))
				break;

			// string-array. Hashmap 에 기 존재하는 String array 에 새 항목을 추가한다.
			else if (tempMap.containsKey(stringID)) {
				LinkedHashSet<String> oldSet = tempMap.get(stringID);
				oldSet.add(text);
				tempMap.put(stringID, oldSet);
			}

			// 일반적인 string.
			else {
				LinkedHashSet<String> tempSet = new LinkedHashSet<String>();
				tempSet.add(text);
				tempMap.put(stringID, tempSet);
			}
		}

		hashmapList.add(tempMap);
	}

	/**
	 * Language name 을 Hashset 형태로 return 한다.
	 */
	private LinkedHashSet<String> getLanguageName(int index) {
		LinkedHashSet<String> tempSet = new LinkedHashSet<String>();
		tempSet.add(sheet.getCell(index, 0).getContents());

		return tempSet;
	}
}
