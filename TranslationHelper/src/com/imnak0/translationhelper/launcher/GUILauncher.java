package com.imnak0.translationhelper.launcher;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.imnak0.translationhelper.XLSCreator;
import com.imnak0.translationhelper.XLSParser;
import com.imnak0.translationhelper.XMLCreator;
import com.imnak0.translationhelper.XMLParser;
import com.imnak0.translationhelper.XMLSort;
import com.imnak0.utility.FileUtility;

/**
 * The MIT License (MIT) Copyright (c) 2013 Nakyung Lim <imnak0@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class GUILauncher {
	private static final String VERSION = "1.2.0";
	private static final String USER_MANUAL = "/TranslationHelper_Manual.pdf";

	private JFrame frame;
	private JPanel panel;
	private JMenuBar menuBar;

	private File previousFolder;
	private File openFile = null;
	private File saveFile = null;

	public static void main(String[] args) throws IOException {
		GUILauncher launcher = new GUILauncher();
		launcher.go();
	}

	public void go() {
		frame = new JFrame();
		panel = new JPanel();
		menuBar = new JMenuBar();

		// Buttons
		JButton button1 = new JButton("Convert XML to Excel");
		button1.addActionListener(XmlToExcelListener);

		JButton button2 = new JButton("Convert Excel to XML");
		button2.addActionListener(ExcelToXmlListener);

		JButton button3 = new JButton("Sort string order in XML");
		button3.addActionListener(sortListener);

		// Menu
		initializeMenuBar();

		// Panel
		panel.add(button1);
		panel.add(button2);
		panel.add(button3);

		// Frame
		frame.setTitle("Translation Helper");
		frame.setJMenuBar(menuBar);
		frame.getContentPane().add(BorderLayout.CENTER, panel);
		frame.setSize(300, 200);
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent arg0) {
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				System.exit(0);
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowActivated(WindowEvent arg0) {
			}
		});
		frame.setVisible(true);
	}

	/**
	 * Create Menu bar
	 */
	private void initializeMenuBar() {
		// File menu
		JMenu menu1 = new JMenu("File");
		menuBar.add(menu1);

		// menu items
		JMenuItem fileMenuItem1 = new JMenuItem("Convert XML to Excel");
		JMenuItem fileMenuItem2 = new JMenuItem("Convert Excel to XML");
		JMenuItem fileMenuItem3 = new JMenuItem("Sort string order in XML");

		fileMenuItem1.addActionListener(XmlToExcelListener);
		fileMenuItem2.addActionListener(ExcelToXmlListener);
		fileMenuItem3.addActionListener(sortListener);

		menu1.add(fileMenuItem1);
		menu1.add(fileMenuItem2);
		menu1.add(fileMenuItem3);

		// Help menu
		JMenu menu2 = new JMenu("Help");
		menuBar.add(menu2);

		// menu items
		JMenuItem helpMenuItem1 = new JMenuItem("About");
		JMenuItem helpMenuItem2 = new JMenuItem("Manual");

		helpMenuItem1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JOptionPane
						.showMessageDialog(
								frame,
								"Translation Helper ver."
										+ VERSION
										+ "\n\nThis program converts between XML file (Android app's string file) and Excel. \n\nNakyung Lim\nimnak0@gmail.com");
			}
		});
		helpMenuItem2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					Desktop.getDesktop().open(FileUtility.createFileFromResource(getClass(), USER_MANUAL));
				} catch (Exception e) {
				}
			}
		});

		menu2.add(helpMenuItem1);
		menu2.add(helpMenuItem2);
	}

	/**
	 * XML to Excel button's Listener
	 */
	private ActionListener XmlToExcelListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			loadXmlDialog();
			saveExcelDialog();

			if (openFile != null && saveFile != null) {
				XMLParser xmlParser = new XMLParser(openFile);
				xmlParser.parse();

				XLSCreator xlsCreator = new XLSCreator(saveFile, xmlParser.getHashmapList());
				xlsCreator.create();

				try {
					Desktop.getDesktop().open(xlsCreator.getSavefile());
				} catch (IOException e) {
					//TODO: 에러가 난 경우에 대한 처리
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * Excel to XML button's Listener
	 */
	private ActionListener ExcelToXmlListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			loadExcelDialog();
			saveXmlDialog();

			if (openFile != null && saveFile != null) {
				XLSParser xlsParser = new XLSParser(openFile);
				xlsParser.parse();

				XMLCreator xmlCreator = new XMLCreator(saveFile, xlsParser.getHashmapList());
				xmlCreator.create();

				try {
					Desktop.getDesktop().open(saveFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * Compare and sort the string in the XML file 기존의 XML과 동일한 스트링 순서로 신규 생성된 XML의 순서를 변경한다.
	 */
	private ActionListener sortListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			FileFilter filter = new FileNameExtensionFilter("XML files", "xml");
			String criteriaSelectMessage = "스트링 순서의 기준이 될 xml을 선택하세요";
			String targetSelectMesasge = "스트링 순서를 변경할 xml을 선택하세요";

			openFile = fileOpenDialog(filter, criteriaSelectMessage);
			saveFile = fileOpenDialog(filter, targetSelectMesasge);

			if (openFile != null && saveFile != null) {
				XMLSort sorter = new XMLSort(openFile, saveFile);
				sorter.parseAndSort();

				try {
					XMLCreator xmlCreator = new XMLCreator(saveFile, sorter.getSortedHashMap());
					xmlCreator.createSorted();
					Desktop.getDesktop().open(saveFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * Open dialog to load XML file
	 */
	private void loadXmlDialog() {
		FileFilter filter = new FileNameExtensionFilter("XML files", "xml");
		String message = "Select the xml file";
		openFile = fileOpenDialog(filter, message);
	}

	/**
	 * Open dialog to save Excel file
	 */
	private void saveExcelDialog() {
		FileFilter filter = new FileNameExtensionFilter("Excel file", "xls");
		String message = "Enter the excel filel name which will be converted from XML";
		saveFile = fileOpenDialog(filter, message);
	}

	/**
	 * Open dialog to load Excel file
	 */
	private void loadExcelDialog() {
		FileFilter filter = new FileNameExtensionFilter("Excel files", new String[] { "xls", "xlsx" });
		String message = "Select the Excel file";
		openFile = fileOpenDialog(filter, message);
	}

	/**
	 * Open dialog to save XML file
	 */
	private void saveXmlDialog() {
		FileFilter filter = new FileNameExtensionFilter("XML file", "xml");
		String message = "Select the folder where the xml files will be saved in";
		saveFile = folderOpenDialog(filter, message);
	}

	/**
	 * open dialog to select file
	 * 
	 * @param filter
	 * @param fileOpenMessage
	 * @return
	 */
	private File fileOpenDialog(FileFilter filter, String fileOpenMessage) {
		// Open Dialog
		JFileChooser fileOpen = new JFileChooser(previousFolder);
		fileOpen.setDialogTitle(fileOpenMessage);
		fileOpen.addChoosableFileFilter(filter);
		int openRet = fileOpen.showDialog(panel, "Open");

		// Get the selected file
		if (openRet == JFileChooser.APPROVE_OPTION) {
			File file = fileOpen.getSelectedFile();
			previousFolder = new File(file.getAbsolutePath());

			return file;
		} else {
			return null;
		}
	}

	/**
	 * open dialog to select folder
	 * 
	 * @param filter
	 * @param message
	 * @return
	 */
	private File folderOpenDialog(FileFilter filter, String message) {
		// Select the output folder
		JFileChooser fileSave = new JFileChooser(previousFolder) {
			private static final long serialVersionUID = 1L;

			@Override
			public void approveSelection() {
				if (getSelectedFile().isDirectory())
					super.approveSelection();
				else
					return;
			}
		};
		fileSave.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileSave.setDialogTitle(message);
		int saveRet = fileSave.showDialog(panel, "Save");

		// Get the selected file or directory
		if (saveRet == JFileChooser.APPROVE_OPTION) {
			File file = fileSave.getSelectedFile();
			previousFolder = new File(file.getAbsolutePath());

			return file;
		} else {
			return null;
		}
	}
}