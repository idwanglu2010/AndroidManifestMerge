package org.dom4j.pack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class XmlFormater {
	/**
	 * 将srcFileName指定的xml格式文件格式化输出到destFileName的xml文件
	 * 
	 * @param destFileName
	 *            格式化后的xml文件
	 * @param srcFileName
	 *            需要被格式化的文件
	 * @return true表示格式化成功，false表示格式化失败
	 */
	public static boolean writeFormatXmlToFile(String destFileName,
			String srcFileName) {
		boolean flag = false;
		FileOutputStream out = null;
		try {
			// 从源文件读取需要被格式化的xml文件
			String sourceStr = readString(srcFileName);
			// System.out.print("sourceStr" + sourceStr + "\n");
			// 读取临时文件后，删除临时文件
			File file = new File(srcFileName);
			if (file.exists()) {
				file.delete();
			}
			String formatedXmlStr = formatXML(sourceStr);
			// 删除已经存在的目的文件
			file = new File(destFileName);
			if (file.exists()) {
				file.delete();
			}
			// 写入到目的文件
			out = new FileOutputStream(destFileName);
			out.write(formatedXmlStr.getBytes());
			flag = true;
			System.out.println("output " + destFileName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return flag;
	}

	/**
	 * 将org.dom4j.Document的Document类格式化输出
	 * 
	 * @param doc
	 *            org.dom4j.Document的Document，注意，不是org.w3c.dom.Document类型
	 * @return 返回格式化后的内容
	 * @throws Exception
	 */
	public static String formatXML(org.dom4j.Document doc) throws Exception {
		StringWriter out = null;
		try {
			OutputFormat formate = OutputFormat.createPrettyPrint();
			formate.setEncoding("utf-8");
			out = new StringWriter();
			XMLWriter writer = new XMLWriter(out, formate);
			writer.write(doc);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != out) {
				out.close();
			}
		}
		return out.toString();
	}

	/**
	 * 将输入的xml字符串内容格式化
	 * 
	 * @param string
	 *            输入的未经格式化的xml内容
	 * @return 格式化后的xml的内容
	 * @throws Exception
	 */
	public static String formatXML(String string) throws Exception {
		SAXReader reader = new SAXReader();
		StringReader in = new StringReader(string);
		org.dom4j.Document doc = (org.dom4j.Document) reader.read(in);
		return formatXML(doc);
	}

	/**
	 * 从xml文件中读取内容到String
	 * 
	 * @param fileName
	 *            文件名，全路径
	 * @return 文件内容
	 */
	private static String readString(String fileName) {
		int len = 0;
		FileInputStream is = null;
		BufferedReader in = null;
		StringBuffer str = new StringBuffer("");
		File file = new File(fileName);
		try {
			is = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(is);
			in = new BufferedReader(isr);
			String line = null;
			while ((line = in.readLine()) != null) {
				if (len != 0) // 处理换行符的问题
				{
					str.append("\r\n" + line);
				} else {
					str.append(line);
				}
				len++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != in) {
					in.close();
				}
				if (null != is) {
					is.close();
				}
			} catch (Exception e2) {
			}
		}
		return str.toString();
	}
}
