package com.haiyiyang.treasures.english;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyClass {

	private static BufferedReader bufferedReader;

	public static void main(String[] args) {
		List<String> list = MyClass.readFileByLine("words.txt");
		MyClass.writeInFile("output.txt", list);
	}

	public static List<String> readFileByLine(String strFile) {
		try {
			File file = new File(strFile);
			bufferedReader = new BufferedReader(new FileReader(file));
			List<String> list = new ArrayList<String>(18000);
			String strLine = null;
			int lineCount = 1;
			while (null != (strLine = bufferedReader.readLine())) {
				list.add(strLine);
				System.out.println("第[" + lineCount + "]行数据:[" + strLine + "]");
				lineCount++;
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	private static void writeInFile(String strFile, List<String> list) {
		Writer writer = null;
		StringBuilder outputString = new StringBuilder();
		try {
			for(String str: list) {
				outputString.append(str + "\r\n");
			}
			writer = new FileWriter(new File(strFile), true); // true表示追加
			writer.write(outputString.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}

}
