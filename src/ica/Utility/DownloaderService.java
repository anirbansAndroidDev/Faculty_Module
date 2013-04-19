package ica.Utility;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class DownloaderService {

	public static boolean remoteHTTPDownloader(String VirtualPath,
			String PhysicalPath) {

		boolean idDownloadSuccessful = false;

		File PhysicalFile = new File(PhysicalPath);

		if (PhysicalFile.exists()) {
			PhysicalFile.delete();
		}

		String urlString = VirtualPath;
		BufferedInputStream bis = null;

		try {
			URI uri = new URI(urlString.replace(" ", "%20"));

			HttpGet httpRequest = null;

			httpRequest = new HttpGet(uri);

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = (HttpResponse) httpclient
					.execute(httpRequest);

			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
			InputStream input = bufHttpEntity.getContent();

			bis = new BufferedInputStream(input);

			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;

			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			FileOutputStream fos = new FileOutputStream(PhysicalFile);
			fos.write(baf.toByteArray());
			fos.close();
			bis.close();

			if (PhysicalFile.exists()) {
				idDownloadSuccessful = true;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return idDownloadSuccessful;
	}

	public static Bitmap downloadBitmap(String url) {
		final AndroidHttpClient client = AndroidHttpClient
				.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);

		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode
						+ " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					final Bitmap bitmap = BitmapFactory
							.decodeStream(inputStream);
					return bitmap;
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			getRequest.abort();
			e.printStackTrace();
		}

		finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}

}
