package org.dom4j.pack;

import java.io.File;
import java.io.FilenameFilter;

public class XmlMerge {

	/**
	 * 说明，数组第一个参数必须是demo的AndroidManifest.xml文件
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		String demoAndroidManifestFile = null;
		String libXmlArrays[] = null;
		String libXmlPath = null;
		String outputAndroidManifestPath = null;
		boolean justPrintUsage = false;
		if (args.length <= 0) {
			String msg = "You must specify -f -d -o arguments";
			throw new RuntimeException(msg);
		}
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-h")) {
				justPrintUsage = true;
			} else if (arg.equals("-f")) {
				try {
					File file = new File(args[i + 1]);
					if (!file.exists()) {
						String msg = "You must specify a demo AndroidManifest.xml file which is exist";
						throw new RuntimeException(msg);
					}
					if (file.isDirectory()) {
						String msg = "You must specify a demo AndroidManifest.xml file not a dir";
						throw new RuntimeException(msg);
					}
					if (!args[i + 1].endsWith(".xml")) {
						String msg = "You must specify a demo AndroidManifest.xml file";
						throw new RuntimeException(msg);
					}
					demoAndroidManifestFile = args[i + 1];
				} catch (ArrayIndexOutOfBoundsException e) {
					String msg = "You must specify a demo AndroidManifest.xml file when using the "
							+ arg + " argument";
					throw new RuntimeException(msg);
				} catch (NullPointerException e) {
					String msg = "You must specify a demo AndroidManifest.xml file which is exist";
					throw new RuntimeException(msg);
				}

			} else if (arg.equals("-d")) {
				try {
					File file = new File(args[i + 1]);
					if (!file.exists() || file.isFile()) {
						String msg = "You must specify a dir when using the "
								+ arg + " argument";
						throw new RuntimeException(msg);
					}
					// lib's AndroidManifest.xml
					libXmlArrays = file.list(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String name) {
							boolean flag = false;
							if (name != null && name.endsWith(".xml")) {
								flag = true;
							}
							return flag;
						}
					});
					if (null == libXmlArrays || libXmlArrays.length <= 0) {
						String msg = "You must specify a dir which has xml files when using the "
								+ arg + " argument";
						throw new RuntimeException(msg);
					}
					libXmlPath = args[i + 1];
				} catch (ArrayIndexOutOfBoundsException e) {
					String msg = "You must specify a dir when using the " + arg
							+ " argument";
					throw new RuntimeException(msg);
				} catch (NullPointerException e) {
					String msg = "You must specify a dir when using the " + arg
							+ " argument";
					throw new RuntimeException(msg);
				}

			} else if (arg.equals("-o")) {
				try {
					File file = new File(args[i + 1]);
					if (!file.exists() || file.isFile()) {
						String msg = "You must specify a dir when using the "
								+ arg + " argument";
						throw new RuntimeException(msg);
					}
					outputAndroidManifestPath = args[i + 1];
				} catch (ArrayIndexOutOfBoundsException e) {
					String msg = "You must specify a dir when using the " + arg
							+ " argument";
					throw new RuntimeException(msg);
				} catch (NullPointerException e) {
					String msg = "You must specify a dir when using the " + arg
							+ " argument";
					throw new RuntimeException(msg);
				}

			}
		}
		if (justPrintUsage) {
			printUsage();
			return;
		}
		if (null != libXmlArrays) {
			int length = 1 + libXmlArrays.length + 1;
			String[] xmlFiles = new String[length];
			xmlFiles[0] = demoAndroidManifestFile;
			for (int j = 0; j < libXmlArrays.length; j++) {
				xmlFiles[j + 1] = libXmlPath + File.separatorChar
						+ libXmlArrays[j];
			}
			xmlFiles[length - 1] = outputAndroidManifestPath;
			XmlTool.mergeAllXmlFile(xmlFiles);
		}
	}

	private static void printUsage() {
		System.out.println("XmlMerge [options]");
		System.out.println("Options: ");
		System.out.println("  -h                   print this message");
		System.out
				.println("  -f <file>            specifies a demo's AndroidManifest.xml");
		System.out
				.println("  -d <path>            specifies a path which has many xml file to be merged,such as plugin lib's AndroidManifest.xml");
		System.out
				.println("  -o <path>            specifies a path which to store AndroidManifest.xml which has be merged");
	}
}
