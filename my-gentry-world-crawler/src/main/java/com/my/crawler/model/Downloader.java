package com.my.crawler.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.gargoylesoftware.htmlunit.WebClient;
import com.my.crawler.util.DownloadUtil;

public class Downloader {
	private URL url;
	private File outputFolder;

	public Downloader(final URL url, File outputFolder) throws MalformedURLException {
		this.url = url;
		String subFolder = url.getQuery();
    	if(subFolder!= null) {
    		subFolder = subFolder.substring(subFolder.lastIndexOf("=")+1, subFolder.length());
    	} else {
    		subFolder = new Path(url.getPath()).lastSegment();
    	}
		this.outputFolder = new File(outputFolder, subFolder);
	}

	public void startDownload(boolean isGroup, IProgressMonitor monitor) {

		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
		final WebClient webClient = new WebClient();
		webClient.getOptions().setUseInsecureSSL(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);

		try {

			List<String> imgList = getImgList(webClient, url, outputFolder);
			if (imgList == null) {
				AbstractPageParser pageParser;
				if (isGroup) {
					pageParser = new SinglePageParser(webClient, url);
				} else {
					pageParser = new MultiPageParser(webClient, url);
				}
				pageParser.parsePage(monitor);
				imgList = pageParser.getImgUrlList();
			}

			writeLogFile(url.toString(), imgList, outputFolder);
			monitor.beginTask("正在下载:", imgList.size());
			for (int i = 0; i < imgList.size(); i++) {
				if (monitor.isCanceled()) {
					break;
				}
				String imgUrlStr = imgList.get(i);
				monitor.setTaskName("正在下载:"+(i + 1) + "/" + imgList.size());
				monitor.subTask(imgUrlStr);
				System.out.println(i + 1 + "/" + imgList.size());
				DownloadUtil.downloadImg(webClient, imgUrlStr, outputFolder);
				monitor.worked(1);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		webClient.close();
	}

	public static List<String> getImgList(WebClient webClient, URL url, File outputFolder) {
		File logFile = new File(outputFolder, "list.log");
		if (logFile.exists()) {
			try {
				Properties properties = new Properties();
				properties.load(new BufferedReader(new InputStreamReader(new FileInputStream(logFile), "UTF-8")));
				String urlStr = properties.getProperty("url");
				if (url.equals(new URL(urlStr))) {
					String liStr = properties.getProperty("list");
					if (liStr != null && !liStr.isEmpty()) {
						return Arrays.asList(liStr.split("\n"));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public static void writeLogFile(String url, List<String> imgList, File outputFolder) {
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		Properties properties = new Properties();
		properties.setProperty("url", url);
		properties.setProperty("size", Integer.toString(imgList.size()));
		properties.setProperty("list", String.join("\n", imgList));

		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(outputFolder, "list.log")), "UTF-8"));) {
			properties.store(writer, "Sacn " + url + " and get the following list. ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
