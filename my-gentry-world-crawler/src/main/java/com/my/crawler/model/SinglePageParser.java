package com.my.crawler.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class SinglePageParser extends AbstractPageParser {
	public static void main(String[] args) throws MalformedURLException {

		String urlStr = "https://thehentaiworld.com/hentai-cosplay-images/keira-metz-lada-lyumos-witcher-3/";
		URL url = new URL(urlStr);
		File outputFolder = new File("C:\\360安全浏览器下载");
		Downloader downloader = new Downloader(url, outputFolder);
		downloader.startDownload(false,new NullProgressMonitor());
	}

	public SinglePageParser(final WebClient webClient, URL url) {
		super(webClient, url);
	}

	protected void initImgList(HtmlPage htmlPage,IProgressMonitor monitor) {
		if(monitor.isCanceled() || htmlPage==null) {
			return;
		}
		HtmlElement document = htmlPage.getDocumentElement();
		List<HtmlElement> elements = document.getElementsByAttribute("div", "id", "miniThumbContainer");
		if (elements.size() == 1) {
			HtmlDivision div = (HtmlDivision) elements.get(0);
			for (DomElement divSub : div.getChildElements()) {
				if(monitor.isCanceled()) {
					break;
				}
				String imgUrlStr = divSub.getElementsByTagName("img").get(0).getAttribute("src");
				imgUrlStr = getRawImgUrl(imgUrlStr);
				this.imgUrlList.add(imgUrlStr);
				System.out.println(imgUrlStr);
			}
		}
	}
}
