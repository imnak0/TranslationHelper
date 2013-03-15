package com.imnak0.translationhelper;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * XML을 읽어와 비교 대상 순서대로 정렬한다.
 * @author Nakyung Lim
 *
 */
public class XMLSort {
	private File openFile;
	private File savePath;

	private ArrayList<ArrayList<Integer>> sameStringListArray = new ArrayList<ArrayList<Integer>>();

	private LinkedHashMap<String, LinkedHashSet<String>> originalHashMap;
	private LinkedHashMap<String, LinkedHashSet<String>> translatedHashMap;
	private LinkedHashMap<String, LinkedHashSet<String>> sortedHashMap = new LinkedHashMap<String, LinkedHashSet<String>>();

	public XMLSort(File openFile, File savePath) {
		super();

		this.openFile = openFile;
		this.savePath = savePath;
	}

	/**
	 * XML 을 parsing하고 기준 XML과 비교하여 스트링 순서를 정렬한다.
	 */
	public void parseAndSort() {
		parse();

		checkIfSameStringExists();
		addOverlapedStringsIntoTargetText();

		sort();
	}

	/**
	 * XML 파일 2개를 parsing한다.
	 */
	private void parse() {
		// parse
		XMLParser xmlParser = new XMLParser(openFile);
		xmlParser.parse();

		XMLParser xmlParser2 = new XMLParser(savePath);
		xmlParser2.parse();

		originalHashMap = xmlParser.getHashmapList();
		translatedHashMap = xmlParser2.getHashmapList();
	}

	/**
	 * 원본 XML에 String ID는 다르지만 Text가 동일한 스트링이 존재하는지 확인한다.
	 * 중복되는 string id 는 sameStringListArray 에 추가한다.
	 */
	private void checkIfSameStringExists() {
		/*
		 LinkedHashMap<String, LinkedHashSet<String>>
		*/
		ArrayList<String> keyList = new ArrayList<String>(originalHashMap.keySet());
		int size = keyList.size();
		for (int i = 0; i < size; i++) {
			ArrayList<Integer> temp = new ArrayList<Integer>();
			LinkedHashSet<String> valueSet = originalHashMap.get(keyList.get(i));

			// string-array 는 중복 여부 확인 안함. 너무 골치아픔.
			if (valueSet.size() > 1)
				continue;

			for (int j = i + 1; j < size; j++) {
				LinkedHashSet<String> valueSet2 = originalHashMap.get(keyList.get(j));

				if (valueSet2.size() == 1 && valueSet.toString().equals(valueSet2.toString()))
					temp.add(j);
			}

			if (temp.size() > 0) {
				temp.add(i);
				sameStringListArray.add(temp);
			}
		}
	}

	/**
	 * 중복되는 Text가 있다면, 이 text의 ID가 번역된 stringID 목록에 포함되는지를 확인한다.
	 * 번역이 된 text라면, 번역된 string ID에 원본에서 중복되는 ID와 번역된 text를 추가한다.
	 * 
	 * ex)
	 * string ID가 a,b,c,d 인 4개의 string이 있는데 모두 value가 "text"로 동일.
	 * 번역 string 에는 a - "텍스트" 하나 존재.
	 * 이 경우 번역 string 에 b - "텍스트", c - "텍스트", d - "텍스트" 를 추가해 준다는 소리.
	 * 이렇게 해 줘야 나중에 sorting 할 때 편하기 때문.
	 */
	private void addOverlapedStringsIntoTargetText() {
		ArrayList<String> keyList = new ArrayList<String>(originalHashMap.keySet());

		int size = sameStringListArray.size();
		for (int i = 0; i < size; i++) {
			ArrayList<Integer> sameStringID = sameStringListArray.get(i);

			// Check whether the stringID exists in the translated ID list
			int size2 = sameStringID.size();
			for (int j = 0; j < size2; j++) {
				int tempIdx = sameStringID.get(j);
				String compareID = keyList.get(tempIdx);

				if (translatedHashMap.containsKey(compareID)) {
					LinkedHashSet<String> copyText = translatedHashMap.get(compareID);
					sameStringID.remove(j);

					int size3 = sameStringID.size();
					for (int k = 0; k < size3; k++) {
						String key = keyList.get(sameStringID.get(k));

						translatedHashMap.put(key, copyText);
					}
					break;
				}
			}
		}
	}

	/**
	 * criteria 를 기준으로 target의 순서를 정렬한다.
	 */
	private void sort() {
		ArrayList<String> originalID = new ArrayList<String>(originalHashMap.keySet());
		ArrayList<String> translatedID = new ArrayList<String>(translatedHashMap.keySet());

		// sort
		int size = originalID.size();
		int size2 = translatedID.size();

		/*
		 * criteriaID 를 기준으로 targetID 와 비교하여,
		 * 동일할 경우 criteriaText 를 targetText 의 해당 값으로 교체해버린다.
		 * 만약 동일한 ID가 없는 경우에는 해당 criteriaID를 삭제해버린다.
		 * 
		 * 단, stringArray 는 이 기능을 지원하지 않으므로 유의하도록 한다.
		 */
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size2; j++) {
				if (originalID.get(i).equals(translatedID.get(j))) {
					String key = translatedID.get(j);
					LinkedHashSet<String> value = translatedHashMap.get(key);
					
					sortedHashMap.put(key, value);
					translatedHashMap.remove(key);

					break;
				}
			}
		}

		// original 에 존재하지 않지만 translation 에 존재하는 관계로 정렬할 수 없었던 string을 마지막으로 추가한다.
		if (translatedHashMap.size() > 0)
			sortedHashMap.putAll(translatedHashMap);
	}

	/**
	 * 정렬된 hashmap 을 return한다.
	 */
	public LinkedHashMap<String, LinkedHashSet<String>> getSortedHashMap() {
		return sortedHashMap;
	}
}
