package com.imnak0.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtility {

	/**
	 * Project resource 에서 original file 을 읽어와 임시 파일로 생성한다.
	 * (Java Executable jar 파일에서는 resource file의 direct access가 안되기 때문에 임시로 파일을 복사해서 사용해야 함)
	 * 임시로 생성한 파일은 앱이 종료될 때 자동으로 삭제된다.
	 * 
	 * @return temporary copied file
	 */
	public static File createFileFromResource(Class<?> mClass, String fileInResource) throws Exception {
		InputStream is = mClass.getResource(fileInResource).openStream();
		File tempFile = new File(new File(".").getCanonicalPath() + fileInResource); // 임시 파일 이름은 귀찮으니 원본이랑 동일하게.
		FileOutputStream os = new FileOutputStream(tempFile);

		byte[] buffer = new byte[4096];
		int length;
		while ((length = is.read(buffer)) != -1)
			os.write(buffer, 0, length);

		is.close();
		os.close();

		try {
			Runtime.getRuntime().exec("attrib +H +R " + tempFile.getAbsolutePath());
		} catch (Exception e) {
		}
		tempFile.deleteOnExit();

		return tempFile;
	}
}
