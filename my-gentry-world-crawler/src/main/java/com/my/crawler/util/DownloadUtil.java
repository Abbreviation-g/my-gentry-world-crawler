package com.my.crawler.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;

public class DownloadUtil {
	public static void downloadImg(final WebClient webClient, String urlStr, File outputFolder) throws MalformedURLException {
		for(int i=0;i<4;i++) {
			try {
				URL url = new URL(urlStr);
				IPath path = new Path(url.toString());
				path = path.makeRelativeTo(new Path("https://thehentaiworld.com/wp-content/uploads"));
				
				File imgFile = new File(outputFolder, String.join("-", path.segments()));
				if (imgFile.exists()) {
					System.out.println(imgFile + " already exist. ");
					return;
				}
				File parentFolder = imgFile.getParentFile();
				if (!parentFolder.exists()) {
					parentFolder.mkdirs();
				}
				
				Thread.sleep(10 * 1000);
				Page page = webClient.getPage(url);
				if (page instanceof UnexpectedPage) {
					UnexpectedPage unexpectedPage = (UnexpectedPage) page;
					InputStream inputStream = unexpectedPage.getInputStream();
					downloadImg(inputStream, imgFile);
					return;
				}
			} catch (FailingHttpStatusCodeException e) {
				System.err.println(e.getMessage());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static void downloadImg(InputStream inputStream, File imgFile) {
		try (FileOutputStream writer = new FileOutputStream(imgFile);) {
			byte[] data = new byte[1024];
			int n = 0;
			while ((n = inputStream.read(data)) != -1) {
				writer.write(data, 0, n);
			}
			inputStream.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Download " + imgFile + " failed. ");
		}

		System.out.println("Download " + imgFile + " is done. ");
	}

}
