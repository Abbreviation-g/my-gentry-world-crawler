package com.my.crawler.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public abstract class AbstractPageParser {
	protected final WebClient webClient;

	protected URL url;
	protected List<String> imgUrlList;

	public AbstractPageParser(final WebClient webClient, URL url) {
		this.webClient = webClient;
		this.url = url;
		this.imgUrlList = new ArrayList<>();
	}

	public void parsePage(IProgressMonitor monitor) {
		monitor.beginTask("正在解析"+url, IProgressMonitor.UNKNOWN);
		
		HtmlPage htmlPage = getHtmlPage(webClient, url,monitor);
		initImgList(htmlPage,monitor);
	}

	protected HtmlPage getHtmlPage(final WebClient webClient, URL url, IProgressMonitor monitor) {
		for (int i = 0; i < 5; i++) {
			if (monitor.isCanceled()) {
				break;
			}
			try {
				Thread.sleep(10 * 1000);
				Page page = webClient.getPage(url);
				if (page instanceof HtmlPage) {
					HtmlPage htmlPage = (HtmlPage) page;
					return htmlPage;
				}
			} catch (FailingHttpStatusCodeException e) {
				System.err.println(e.getMessage());
			} catch (ConnectTimeoutException | java.net.SocketTimeoutException e) {
				System.err.println(e.getMessage());
				return getHtmlPage(webClient, url, monitor);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	protected abstract void initImgList(HtmlPage htmlPage,IProgressMonitor monitor);

	public List<String> getImgUrlList() {
		return imgUrlList;
	}
	
	static String getRawImgUrl(String imgUrl) {
		imgUrl = imgUrl.replaceAll("-\\d{1,}x\\d{1,}", "");
		return imgUrl;
	}
}
