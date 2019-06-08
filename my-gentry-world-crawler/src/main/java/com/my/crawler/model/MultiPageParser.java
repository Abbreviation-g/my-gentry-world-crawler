package com.my.crawler.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.core.runtime.IProgressMonitor;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.my.crawler.util.DownloadUtil;

public class MultiPageParser extends AbstractPageParser {
	public MultiPageParser(final WebClient webClient, URL url) {
		super(webClient, url);
	}

	protected void initImgList(HtmlPage htmlPage, IProgressMonitor monitor) {
		if (monitor.isCanceled() || htmlPage == null) {
			return;
		}

		HtmlElement document = htmlPage.getDocumentElement();
		List<HtmlElement> elements = document.getElementsByAttribute("img", "itemprop", "thumbnail");
		for (HtmlElement element : elements) {
			if(monitor.isCanceled()) {
				break;
			}
			String imgUrl = element.getAttribute("src");
			imgUrl = getRawImgUrl(imgUrl);
			this.imgUrlList.add(imgUrl);
			System.out.println(imgUrl);
		}
		List<HtmlDivision> navigation = document.getElementsByAttribute("div", "class", "navigation");
		if (navigation.size() == 1) {

			HtmlDivision navigationDiv = navigation.get(0);
			List<HtmlElement> curTemp = navigationDiv.getElementsByAttribute("span", "class", "page current");
			if (curTemp.size() == 1) {
				HtmlSpan currentSpan = (HtmlSpan) curTemp.get(0);
				DomNode nextEle = currentSpan.getParentNode().getNextSibling();
				if (nextEle != null) {
					HtmlListItem nextLi = (HtmlListItem) currentSpan.getParentNode().getNextSibling();
					String nextPageHref = nextLi.getElementsByTagName("a").get(0).getAttribute("href");

					System.out.print("PageParser2.initImgList()" + "-->>");
					System.out.println(nextPageHref);
					try {
						AbstractPageParser pageParser = new MultiPageParser(webClient, new URL(nextPageHref));
						pageParser.parsePage(monitor);
						List<String> imgUrlList = pageParser.getImgUrlList();
						this.imgUrlList.addAll(imgUrlList);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}

				}
			}
		}
	}

	public static void main(String[] args) {
		final String baseUrlStr = "https://thehentaiworld.com";
		final String searchWord = "Sarada";
		final File outputFolder = new File("C:\\360安全浏览器下载\\thehentaiworld");

		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");

		final WebClient webClient = new WebClient();
		webClient.getOptions().setUseInsecureSSL(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);

		try {
			URIBuilder uriBuilder = new URIBuilder(baseUrlStr);
			uriBuilder.addParameter("s", searchWord);
			URL url = uriBuilder.build().toURL();

			List<String> imgList = new MultiPageParser(webClient, url).getImgUrlList();
			System.out.println(imgList);

			for (String imgUrlStr : imgList) {
				DownloadUtil.downloadImg(webClient, imgUrlStr, outputFolder);
			}
		} catch (URISyntaxException | MalformedURLException e) {
			e.printStackTrace();
		}

		webClient.close();
	}
}
