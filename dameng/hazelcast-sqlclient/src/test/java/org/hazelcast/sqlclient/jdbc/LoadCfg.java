package org.hazelcast.sqlclient.jdbc;

import java.io.File;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class LoadCfg {
	public static void main(String[] args) {
		SAXReader reader = new SAXReader();
		String base = System.getProperty("user.dir");
		Document doc = null;
		try {
			doc = reader.read(new File(base + "\\target\\configuration.xml"));
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		Element root = doc.getRootElement();
		Element mysql = root.element("mysql");
		String driver = mysql.elementText("driver");
		String url = mysql.elementText("url");
		String userName = mysql.elementText("userName");
		String passwd = mysql.elementText("password");
		System.out.println(driver);
		System.out.println(url);
		System.out.println(userName);
		System.out.println(passwd);
	}
}
