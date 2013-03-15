package com.imnak0.translationhelper;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * strings.xml 파일을 parsing 하여 string ID와 텍스트를 Hashmap 형태로 생성한다.
 * 
 * parse() method 를 실행하면 자동으로 parsing 이 완료되며,
 * 이후 getHashmapList method 를 이용해서 LinkedHashMap<String, LinkedHashSet<String>> 를 읽어갈 수 있다.
 * 
 * @author Nakyung Lim
 *
 */
public class XMLParser {
	private Element root;
	private File openFile;

	private LinkedHashMap<String, LinkedHashSet<String>> hashMap = new LinkedHashMap<String, LinkedHashSet<String>>();

	public XMLParser() {
		super();
	}

	public XMLParser(File openFile) {
		super();
		this.openFile = openFile;
	}

	/**
	 * Hashmap arrayList를 return 한다.
	 */
	public LinkedHashMap<String, LinkedHashSet<String>> getHashmapList() {
		return hashMap;
	}

	/**
	 * XML Parsing 을 수행한다.
	 */
	public void parse() {
		try {
			initialize();
			getNode(root);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOM 객체 생성 및 초기화
	 */
	private void initialize() throws Exception {
		// DOM을 준비한다
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = dbf.newDocumentBuilder();

		// 문서 읽어들이기
		if (openFile == null) {
			String fileName = new File(".").getCanonicalPath() + File.separator + "dropbox" + File.separator + "strings.xml";
			openFile = new File(fileName);
		}
		Document xmldoc = parser.parse(openFile);

		// 문서의 출발점을 얻는다
		root = xmldoc.getDocumentElement();
	}

	/**
	 * for-loop 을 돌려서 각 Node 를 확인하며 string ID 와 text를 arrayList 로 생성한다.
	 * String-array 의 경우에는 이 메소드를 재귀 호출하여 처리한다.
	 * 
	 * @param Node
	 */
	private void getNode(Node aNode) {
		for (Node node = aNode.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String stringID = getStringID(node);

				// String Array 인 경우 (parent) recursive
				if (node.getNodeName().equals("string-array")) {
					getNode(node);
				} else {
					String text = null;
					LinkedHashSet<String> tempSet = null;

					// String 인 경우 StringID 와 text를 arrayList 에 추가
					if (node.getNodeName().equals("string")) {
						text = node.getTextContent();
						tempSet = new LinkedHashSet<String>();
					}

					// String-Array 인 경우 (child)
					else if (node.getNodeName().equals("item")) {
						text = node.getTextContent();
						tempSet = hashMap.get(stringID);
						
						if (tempSet == null)
							tempSet = new LinkedHashSet<String>();
					}

					tempSet.add(text);
					hashMap.put(stringID, tempSet);
				}
			}
		}
	}

	/**
	 * NamedNodeMap 에서 stringID 를 추출하여 리턴한다.
	 * 
	 * input argument 는 다음과 같은 형식으로 들어온다.
	 * 	string/name="xxxx"
	 * 여기에서 다른 수식어를 제외한 xxxx 만을 return한다.
	 * 
	 * @return stringID
	 */
	private String getStringID(Node node) {
		String name = null;
		Node nodeAttr = node.getAttributes().getNamedItem("name");

		// normal string
		if (nodeAttr != null)
			name = nodeAttr.toString();

		// String-array인 경우 Parent node 의 name을 읽어온다.
		else
			name = node.getParentNode().getAttributes().getNamedItem("name").toString();
		return name.substring(6, name.length() - 1);
	}
}
