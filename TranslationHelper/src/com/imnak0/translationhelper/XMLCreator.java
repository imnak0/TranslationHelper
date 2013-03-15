package com.imnak0.translationhelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Excel 문서 parsing 결과를 가져와 XML file을 생성한다.
 * @author Nakyung Lim
 *
 */
public class XMLCreator {
	private File savePath;
	private ArrayList<LinkedHashMap<String, LinkedHashSet<String>>> hashmapList;

	// DOM 관련
	private Document doc;
	private Element rootElement;

	public XMLCreator(File savePath, ArrayList<LinkedHashMap<String, LinkedHashSet<String>>> hashmapList) {
		super();

		this.savePath = savePath;
		this.hashmapList = hashmapList;
	}

	public XMLCreator(File savePath, LinkedHashMap<String, LinkedHashSet<String>> hashMap) {
		super();

		this.savePath = savePath;
		this.hashmapList = new ArrayList<LinkedHashMap<String,LinkedHashSet<String>>>();
		hashmapList.add(hashMap);
	}

	/**
	 * XML sorting 결과를 다시 xml로 생성한다.
	 */
	public void createSorted() {
		try {
			createXmlFileUsingDOM(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 번역 스트링을 xml 파일로 생성한다.
	 */
	public void create() {
		int size = hashmapList.size();
		for (int i = 0; i < size; i++) {
			try {
				createXmlFileUsingDOM(i);
			} catch (Exception e) {
				e.printStackTrace();
				// DOM 을 이용한 xml 생성 실패시 custom 으로 xml을 생성한다.
				/*
				 * 아래 방식에서는 string-array 구현 안했음.
				ArrayList<String> xmlString = createXmlString(language);
				createXmlFile(xmlString, i);
				*/
			}
		}
	}

	/**
	 * text가 xml에 적합한지 검토 및 수정한다.
	 */
	private String checkStringValidation(String temp) {
		temp = changeSpecialCharacter(temp);
		temp = changeLFtoASCII(temp);

		return temp;
	}

	/**
	 * String validation 을 확인하여 잘못된 부분을 수정한다.
	 * 예를들면 can't => can\'t
	 */
	private String changeSpecialCharacter(String temp) {
		if (isCDATAtext(temp))
			return temp;
		else
			return temp.replace("\'", "'").replace("'", "\'");
	}

	/**
	 * 엑셀에서 줄바꿈이 되어 있는 문장의 경우 줄바꿈을 ascii code 로 변경한다.
	 */
	private String changeLFtoASCII(String target) {
		if (target == null || target.equals(""))
			return "";

		else if (isCDATAtext(target))
			return target;

		byte[] lfBytes = { 10 };
		String lfStr = null;
		String lfStr1 = new String(lfBytes); // LF : New line
		String lfStr2 = System.getProperty("line.separator"); // CR : Carriage return
		String newLfStr = "\\n";

		if (target.contains(lfStr1))
			lfStr = lfStr1;
		else if (target.contains(lfStr2))
			lfStr = lfStr2;

		if (lfStr == null) {
			return target;
		} else {
			String result = "";
			String splitStr[] = target.split(lfStr);

			int length = splitStr.length;
			int i = 0;
			for (i = 0; i < length - 1; i++) {
				result += splitStr[i];
				result += newLfStr;
			}
			result += splitStr[i];

			return result;
		}
	}

	/**
	 * DOM class 를 이용하여 XML instance를 생성한다.
	 */
	private void createXmlFileUsingDOM(int index) throws Exception {
		createDOMInstance();
		createAndAppendElements(index);
		writeIntoXMLfile(index);
	}

	/**
	 * DOM instance를 생성한다.
	 */
	private void createDOMInstance() throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		doc = docBuilder.newDocument();

		rootElement = doc.createElement("resources");
		doc.appendChild(rootElement);
	}

	/**
	 * for-loop 돌리며 각 string ID element 생성
	 * 
	 * @param textList : 번역 결과를 ArrayList 형태로 전달한다.
	 */
	private void createAndAppendElements(int index) {
		LinkedHashMap<String, LinkedHashSet<String>> hashmap = hashmapList.get(index);
		Iterator<String> hashmapItr = hashmap.keySet().iterator();

		while (hashmapItr.hasNext()) {
			Element string;
			String stringID = hashmapItr.next();
			if (stringID.equals(XLSParser.LANGUAGE))
				stringID = hashmapItr.next();
			
			LinkedHashSet<String> textSet = hashmap.get(stringID);
			if (textSet.size() == 0)
				textSet.add("");

			// Check text validation first.
			LinkedHashSet<String> newTextSet = new LinkedHashSet<String>();
			Iterator<String> textsetItr = textSet.iterator();

			while (textsetItr.hasNext()) {
				String text = textsetItr.next();
				text = checkStringValidation(text);
				newTextSet.add(text);
			}

			// String-array 인 경우
			if (newTextSet.size() > 1) {
				string = doc.createElement("string-array");
				string.setAttribute("name", stringID);

				textsetItr = newTextSet.iterator();
				while (textsetItr.hasNext()) {
					Element item = doc.createElement("item");
					item.setTextContent(textsetItr.next());
					string.appendChild(item);
				}
			}

			// 일반 string 인 경우
			else {
				string = doc.createElement("string");
				string.setAttribute("name", stringID);

				// CDATA section
				String text = newTextSet.iterator().next();
				if (isCDATAtext(text))
					string.appendChild(doc.createCDATASection(text));
				// 일반 스트링
				else
					string.setTextContent(text);
			}

			rootElement.appendChild(string);
		}
	}

	/**
	 * CDATA text 인지 판별한다.
	 * <,  >, />  혹은 <, </, > 를 포함하는지 여부로 확인한다.
	 */
	private boolean isCDATAtext(String text) {
		if (text.contains("<") && text.contains(">") && text.contains("/>"))
			return true;

		else if (text.contains("<") && text.contains(">") && text.contains("</"))
			return true;

		return false;
	}

	/**
	 * DOM instance를 XML로 변환 저장한다.
	 * 
	 * @pararm index : 번역 언어 순서. languageName 에서 언어 이름을 찾아내기 위해 사용한다.
	 */
	private void writeIntoXMLfile(int index) throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);

		String fileName = null;
		File saveFile = null;
		if (savePath == null) {
			fileName = new File(".").getCanonicalPath() + File.separator + "dropbox" + File.separator + "strings_" + getLanguageName(index)
					+ ".xml";
			saveFile = new File(fileName);
		} else if (savePath.isFile()) {
			saveFile = savePath;
		} else {
			String path = savePath.getAbsolutePath();
			// Window, Linux 동시 지원을 위한 코드. Window는 pathSeperator가 자동으로 붙지 않고 리눅스는 자동으로 붙음.
			if (!path.endsWith(File.separator))
				path = path.concat(File.separator);
			saveFile = new File(path + "strings_" + getLanguageName(index) + ".xml");
		}

		if (saveFile.exists())
			saveFile.delete();
		StreamResult result = new StreamResult(new FileOutputStream(saveFile));

		transformer.transform(source, result);
	}

	/**
	 * Langauge name 을 String 형태로 return한다.
	 */
	private String getLanguageName(int index) {
		LinkedHashSet<String> temp = hashmapList.get(index).get(XLSParser.LANGUAGE);

		return temp.iterator().next();
	}
}
