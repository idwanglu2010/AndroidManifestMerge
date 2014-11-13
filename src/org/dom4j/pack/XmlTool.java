package org.dom4j.pack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlTool {

	/**
	 * 合并所有输入的AndroidManifest.xml文件
	 * 
	 * @param args
	 * @return
	 */
	public static boolean mergeAllXmlFile(String[] args) {
		boolean flag = false;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			document.setXmlVersion("1.0");
			Element manifest = document.createElement("manifest");
			manifest.setNodeValue("manifest");
			document.appendChild(manifest);
			/**
			 * 先合并demo的xml文件
			 */
			Document xmlFile = builder.parse(args[0]);
			mergeDemoXmlFile(document, manifest, xmlFile);
			/**
			 * 合并诸多plugin AndroidManifest.xml文件
			 */
			for (int i = 1; i < args.length - 1; ++i) {
				xmlFile = builder.parse(args[i]);
				System.out.print("input " + args[i] + "\n");
				mergeXmlFile(document, manifest, xmlFile);
			}
			String fileString = args[args.length - 1] + File.separatorChar + "AndroidManifest.xml";
			String tempXml = args[args.length - 1] + File.separatorChar + "temp.xml";
			// 输出到文件
			flag = writeToTempFile(document, tempXml);
			// 输出格式化的xml文件
			flag = XmlFormater.writeFormatXmlToFile(fileString, tempXml);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return flag;
	}

	private static boolean mergeXmlFile(Document mainDocument, Element manifest, Document needMergedXmlDoument) {
		Document filename = needMergedXmlDoument;
		// 构造manifest结点属性
		if (!isManifestExist(manifest)) {
			NamedNodeMap nodeMap = filename.getDocumentElement().getAttributes();
			int len = nodeMap.getLength();
			for (int i = 0; i < len; i++) {
				Node node = nodeMap.item(i);
				manifest.setAttribute(node.getNodeName(), node.getNodeValue());
			}
		}
		// manifest结点下的所有结点
		NodeList list = filename.getDocumentElement().getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; i += 1) {
			Node n = list.item(i);
			if (Node.ELEMENT_NODE == n.getNodeType()) {
				// 构造supports-screens结点
				if ("supports-screens".equals(n.getNodeName())) {
					Element supportsScreens = buildNewNode(mainDocument, (Element) n);
					manifest.appendChild(supportsScreens);
				}
				// 构造uses-sdk结点
				if ("uses-sdk".equals(n.getNodeName())) {
					Element usesSdk = getUsesSdkElement(manifest);
					if (null == usesSdk) {
						usesSdk = buildNewNode(mainDocument, (Element) n);
						manifest.appendChild(usesSdk);
					}
				}
				// 构造诸多权限结点
				if ("uses-permission".equals(n.getNodeName())) {
					Element e = buildNewNode(mainDocument, (Element) n);
					NamedNodeMap attriMap = n.getAttributes();
					boolean exist = false;
					for (int j = 0; j < attriMap.getLength(); ++j) {
						Node attr = attriMap.item(j);
						// System.out.print(attr.getNodeName() + " "
						// + attr.getNodeValue() + "\n");
						String permissionName = attr.getNodeValue();
						if (isPermissionExist(manifest, permissionName)) {
							exist = true;
							break;
						}
						Attr a = mainDocument.createAttribute(attr.getNodeName());
						a.setValue(attr.getNodeValue());
						e.setAttributeNode(a);
					}
					if (!exist) {
						manifest.appendChild(e);
					}
				}
				// 构造application结点
				if ("application".equals(n.getNodeName())) {
					Element application = getApplicationElement(manifest);
					if (null == application) {
						// 添加application结点
						application = buildNewNode(mainDocument, (Element) n);
						manifest.appendChild(application);
					} else {
						// 添加application的子结点
						NodeList activies = n.getChildNodes();
						int len = activies.getLength();
						// System.out.print("activity length:" + len);
						for (int k = 0; k < len; k += 1) {
							Node activity = activies.item(k);
							if (Node.ELEMENT_NODE == activity.getNodeType()) {
								// System.out.print("activity:"
								// + activity.getNodeName() + "\n");
								// System.out.print("activity:"
								// + activity.getNodeValue() + "\n");
								// System.out.print("activity:"
								// + activity.getNodeType() + "\n");
								Element newActivity = buildNewNode(mainDocument, (Element) activity);
								// 该名称的结点不存在才拷贝该结点
								if (!isNodeExist(application, (Element) activity)) {
									application.appendChild(newActivity);
								}
							}
						}
					}

				}
				// System.out.print(n.getNodeName() + "\n");
			}
		}

		return true;
	}

	private static boolean mergeDemoXmlFile(Document mainDocument, Element manifest, Document needMergedXmlDoument) {
		Document filename = needMergedXmlDoument;
		String packageValue = null;
		// 构造manifest结点属性
		if (!isManifestExist(manifest)) {
			NamedNodeMap nodeMap = filename.getDocumentElement().getAttributes();
			int len = nodeMap.getLength();
			for (int i = 0; i < len; i++) {
				Node node = nodeMap.item(i);
				if ("package".equals(node.getNodeName())) {
					packageValue = node.getNodeValue();
				}
				manifest.setAttribute(node.getNodeName(), node.getNodeValue());
			}
		}
		// manifest结点下的一些结点
		NodeList list = filename.getDocumentElement().getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; i += 1) {
			Node n = list.item(i);
			if (Node.ELEMENT_NODE == n.getNodeType()) {
				// 构造supports-screens结点
				// if ("supports-screens".equals(n.getNodeName())) {
				// Element supportsScreens = buildNewNode(mainDocument,
				// (Element) n);
				// manifest.appendChild(supportsScreens);
				// }
				// 构造uses-sdk结点
				if ("uses-sdk".equals(n.getNodeName())) {
					Element usesSdk = getUsesSdkElement(manifest);
					if (null == usesSdk) {
						usesSdk = buildNewNode(mainDocument, (Element) n);
						manifest.appendChild(usesSdk);
					}
				}
				// 构造诸多权限结点
				// if ("uses-permission".equals(n.getNodeName())) {
				// Element e = buildNewNode(mainDocument, (Element) n);
				// NamedNodeMap attriMap = n.getAttributes();
				// boolean exist = false;
				// for (int j = 0; j < attriMap.getLength(); ++j) {
				// Node attr = attriMap.item(j);
				// // System.out.print(attr.getNodeName() + " "
				// // + attr.getNodeValue() + "\n");
				// String permissionName = attr.getNodeValue();
				// if (isPermissionExist(manifest, permissionName)) {
				// exist = true;
				// break;
				// }
				// Attr a = mainDocument.createAttribute(attr.getNodeName());
				// a.setValue(attr.getNodeValue());
				// e.setAttributeNode(a);
				// }
				// if (!exist) {
				// manifest.appendChild(e);
				// }
				// }
				// 构造application结点
				if ("application".equals(n.getNodeName())) {
					// 创建一个application结点
					Element applicationNode = mainDocument.createElement(n.getNodeName());
					// 复制application属性
					NamedNodeMap attri = n.getAttributes();
					for (int j = 0; j < attri.getLength(); ++j) {
						Node attr = attri.item(j);
						// System.out.print(attr.getNodeName() + " " +
						// attr.getNodeValue()
						// + "\n");
						Attr a = mainDocument.createAttribute(attr.getNodeName());
						a.setValue(attr.getNodeValue());
						applicationNode.setAttributeNode(a);
					}
					//
					NodeList nodeList = n.getChildNodes();
					int len = nodeList.getLength();
					for (int j = 0; j < len; j++) {
						Node node = nodeList.item(j);
						if (Node.ELEMENT_NODE == node.getNodeType()) {
							boolean flag = false;
							NamedNodeMap ActAttri = node.getAttributes();
							for (int k = 0; k < ActAttri.getLength(); ++k) {
								Node attr = ActAttri.item(k);
								if ("android:name".equals(attr.getNodeName())) {
									String value = attr.getNodeValue();
									if (value.equals(packageValue + ".LineFollower") || value.equals(".LineFollower")) {
										flag = true;
										break;
									}
								}
							}
							if (flag) {
								Element element = buildNewNode(mainDocument, (Element) node);
								applicationNode.appendChild(element);
							}

						}
					}
					// 添加application结点
					manifest.appendChild(applicationNode);
				}
				// System.out.print(n.getNodeName() + "\n");
			}
		}

		return true;
	}

	/**
	 * 最终生成的xml文件的manifest结点是否已经包含了manifest结点的属性,默认创建的manifest结点无属性，通过结点是否有属性来判定
	 * ， 若有属性，则说明已经拷贝了第一个xml文件的Manifest属性, 后续合并的xml文件中的manifest结点的属性将被忽略
	 * 
	 * @param manifest
	 *            最终生成的xml文件的manifest结点,application结点的父亲结点
	 * @return true表示manifest结点属性已经存在，false不存在
	 */
	private static boolean isManifestExist(Element manifest) {
		boolean flag = false;
		NamedNodeMap nodeMap = manifest.getAttributes();
		int len = nodeMap.getLength();
		flag = len > 0 ? true : false;
		return flag;
	}

	/**
	 * 最终生成的xml文件的manifest结点是否已经包含了application结点,
	 * 若已经包含了application结点，则说明已经拷贝了第一个xml文件的application结点,
	 * 后续合并的xml文件中的application结点将被忽略
	 * 
	 * @param manifest
	 *            最终生成的xml文件的manifest结点,application结点的父亲结点
	 * @return true表示application结点已经存在，false不存在
	 */
	private static boolean isApplicationExist(Element manifest) {
		boolean flag = false;
		NodeList list = manifest.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; i += 1) {
			Node node = list.item(i);
			if (node.ELEMENT_NODE == node.getNodeType()) {
				if ("application".equals(node.getNodeName())) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * 最终生成的的xml文件是否已经包含了该权限
	 * 
	 * @param manifest
	 *            最终生成的xml文件的manifest结点
	 * @param permissionName
	 *            权限名称
	 * @return true表示已存在，flase表示不存在
	 */
	private static boolean isPermissionExist(Element manifest, String permissionName) {
		boolean flag = false;
		NodeList list = manifest.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; i += 1) {
			Node node = list.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				if ("uses-permission".equals(node.getNodeName())) {
					NamedNodeMap attris = node.getAttributes();
					int len = attris.getLength();
					for (int j = 0; j < len; j++) {
						String attriNodeName = attris.item(j).getNodeName();
						if ("android:name".equals(attriNodeName)) {
							String attriNodeValue = attris.item(j).getNodeValue();
							if (permissionName.equals(attriNodeValue)) {
								flag = true;
								break;
							}

						}
					}
				}
			}
		}
		return flag;
	}

	/**
	 * 判断该结点是否已经存在，例如，同一个activity不能在AndroidManifest.xml中声明多次
	 * 
	 * @param destParent
	 *            与xml A中文档平行的source结点所对应的xml B中文档的父亲结点
	 * @param source
	 *            xml A中文档的source结点
	 * @return true表示已经存在，false表示不存在
	 */
	private static boolean isNodeExist(Element destParent, Element source) {
		boolean flag = false;
		String name = source.getAttribute("android:name");
		NodeList nodeList = destParent.getChildNodes();
		int length = nodeList.getLength();
		for (int i = 0; i < length; i++) {
			Node node = nodeList.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				Element element = (Element) node;
				String sourceName = element.getAttribute("android:name");
				if (null != sourceName && !"".equals(sourceName)) {
					if (sourceName.equals(name)) {
						flag = true;
						break;
					}
				}
			}
		}
		return flag;
	}

	/**
	 * 最终生成的xml文件是已经包含了application结点，AndroidManifest.xml中，application结点是唯一的，
	 * 不能有多个
	 * 
	 * @param manifest
	 *            最终生成的xml文件的manifest结点, application结点的父亲结点
	 * @return 如果最终生成的xml文件已经包含了application结点，则返回该结点，否则返回null
	 */
	private static Element getApplicationElement(Element manifest) {
		Element e = null;
		NodeList list = manifest.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			Node node = list.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				// System.out.print("getApplicationElement " +
				// node.getNodeName()
				// + "\n");
				if ("application".equals(node.getNodeName())) {
					e = (Element) node;
					break;
				}
			}
		}
		return e;
	}

	/**
	 * 最终生成的xml文件是已经包含了uses-sdk结点，AndroidManifest.xml中，uses-sdk结点是唯一的， 不能有多个
	 * 
	 * @param manifest
	 *            最终生成的xml文件的manifest结点, uses-sdk结点的父亲结点
	 * @return 如果最终生成的xml文件已经包含了uses-sdk结点，则返回该结点，否则返回null
	 */
	private static Element getUsesSdkElement(Element manifest) {
		Element e = null;
		NodeList list = manifest.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			Node node = list.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				// System.out.print("getApplicationElement " +
				// node.getNodeName()
				// + "\n");
				if ("uses-sdk".equals(node.getNodeName())) {
					e = (Element) node;
					break;
				}
			}
		}
		return e;
	}

	/**
	 * 说明：之所以拷贝，是因为无法使用跨文档的结点,即xml A的文档里的activity结点不能直接放到xml B文档中 从xml
	 * A中的文档结构中生成一个xml B文档中能够用的结点 递归拷贝其他xml文档的结点
	 * 
	 * @param mainDocument
	 *            xml B文档结构,通过xml A的结点，拷贝复制,创建一个xml B文档使用的结点
	 * @param sourceNode
	 *            xml A文档结构中的结点
	 * @return 返回xml B文档结构中可以使用的结点
	 */
	public static Element buildNewNode(Document mainDocument, Element sourceNode) {
		Element newNode = mainDocument.createElement(sourceNode.getNodeName());
		NamedNodeMap activityAttri = sourceNode.getAttributes();
		for (int j = 0; j < activityAttri.getLength(); ++j) {
			Node attr = activityAttri.item(j);
			// System.out.print(attr.getNodeName() + " " + attr.getNodeValue()
			// + "\n");
			Attr a = mainDocument.createAttribute(attr.getNodeName());
			a.setValue(attr.getNodeValue());
			newNode.setAttributeNode(a);
		}
		NodeList nodeList = sourceNode.getChildNodes();
		int length = nodeList.getLength();
		for (int i = 0; i < length; i++) {
			Node node = nodeList.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				Element element = buildNewNode(mainDocument, (Element) node);
				newNode.appendChild(element);
			}
		}
		return newNode;
	}

	/**
	 * 将文档Docuent输出到文件
	 * 
	 * @param document
	 * @param fileName
	 * @return
	 */
	public static boolean writeToTempFile(Document document, String fileName) {
		boolean flag = false;
		FileOutputStream out = null;
		// 输出到文件
		TransformerFactory transFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);
			File file = new File(fileName);
			if (file.exists()) {
				file.delete();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			out = new FileOutputStream(file);
			StreamResult xmlResult = new StreamResult(out);
			transformer.transform(domSource, xmlResult);
			flag = true;
			// System.out.println("output " + file.getAbsolutePath());
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
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

}
