package com.yuyaa.awashcar.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

import com.yuyaa.awashcar.MyApplication;

public class GOVHttp {
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";
	private static final String CTYPE = "CTYPE";
	private static final String CVERSION = "CVERSION";
	public static DefaultHttpClient client = null;
	public static String url = null;
	private String httpMethod;
	private HttpPost httpPost;
	private HttpGet httpGetRequest;
	private HttpDelete httpDelete;
	private Map<String, String> data = new Hashtable<String, String>();
	public static final double CTYPE_value = 1.0;
	public static String CVERSION_Value = getVersionID();
	public String result;
	public static Map<String, String> urlSessionIDMap = new Hashtable<String, String>();

	/**
	 * 
	 */
	public GOVHttp() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param userURLString
	 */
	public GOVHttp(String userURLString) {
		super();
	}

	public static void clearSessionMap() {
		urlSessionIDMap.clear();
		client = null;
	}

	private static String getVersionID() {
		PackageManager packageManager = MyApplication.getInstance()
				.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(MyApplication
					.getInstance().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String version = packInfo.versionName;
		return version;
	}

	public static void createHttpClient() {

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params,
				HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);
		// 超时设置
		/* 从连接池中取连接的超时时间 */
		ConnManagerParams.setTimeout(params, 1000);
		/* 连接超时 */
		HttpConnectionParams.setConnectionTimeout(params, 20000);
		/* 请求超时 */
		HttpConnectionParams.setSoTimeout(params, 30000);
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				params, schReg);
		client = new DefaultHttpClient(conMgr, params);
		client.addRequestInterceptor(new HttpRequestInterceptor() {

			public void process(HttpRequest request, HttpContext context) {
				// Add header to accept gzip content
				if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
					request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
				}
				if (!request.containsHeader(CTYPE)) {
					request.addHeader(CTYPE, "" + GOVHttp.CTYPE_value);
				}
				if (!request.containsHeader(CVERSION)) {
					if (GOVHttp.CVERSION_Value.equals("")) {
						GOVHttp.CVERSION_Value = getVersionID();
					}
					request.addHeader(CVERSION, GOVHttp.CVERSION_Value);
				}
			}

		});

		client.addResponseInterceptor(new HttpResponseInterceptor() {

			public void process(HttpResponse response, HttpContext context) {
				// Inflate any responses compressed with gzip
				final HttpEntity entity = response.getEntity();
				final Header encoding = entity.getContentEncoding();
				if (encoding != null) {
					for (HeaderElement element : encoding.getElements()) {
						if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
							response.setEntity(new InflatingEntity(response
									.getEntity()));
							break;
						}
					}
				}
			}
		});

		client.setRedirectHandler(new RedirectHandler() {
			// 这里的意思是覆盖系统默认的重定向方法，改成手动处理
			@Override
			public boolean isRedirectRequested(HttpResponse response,
					HttpContext context) {
				// TODO Auto-generated method stub
				// 不需要重定向，因为我们会在后面自己手动处理状态值为302的情况
				return false;
			}

			@Override
			public URI getLocationURI(HttpResponse response, HttpContext context)
					throws ProtocolException {
				// TODO Auto-generated method stub
				// 同样，不需要重定向
				return null;
			}
		});

	}

	public synchronized static void InitializeHttpClient(String urlString) {
		if (url == null || !(url.equals(urlString))) {
			createHttpClient();
		} else if (client == null) {
			createHttpClient();
		}
		url = urlString;

	}

	public static synchronized GOVHttp requestWithURL(String urlString,
			String httpMethod) {
		return requestWithURL(urlString, httpMethod, null, null);
	}

	public static synchronized GOVHttp requestWithURL(String urlString,
			String httpMethod, Map<String, String> tmp) {
		return requestWithURL(urlString, httpMethod, null, tmp);
	}

	public static synchronized GOVHttp requestWithURL(String urlString,
			String httpMethod, String userUrlString, Map<String, String> tmp) {

		InitializeHttpClient(Const.SERVER_BASE_PATH);
		GOVHttp request = new GOVHttp(userUrlString);
		if (tmp != null) {
			request.setData(tmp);
		}
		request.httpMethod = httpMethod;
		if (httpMethod.equals("POST")) {
			request.httpPost = new HttpPost(urlString);
			if (urlSessionIDMap.containsKey(GOVHttp.url)) {
				request.httpPost.setHeader("Cookie",
						urlSessionIDMap.get(GOVHttp.url));
			} else {
				String session = MyApplication.getInstance()
						.getStringGlobalData("session", null);
				if (session != null) {
					request.httpPost.setHeader("Cookie", session);
				}
			}

		} else if (httpMethod.equals("GET")) {
			request.httpGetRequest = new HttpGet(urlString);
			if (urlSessionIDMap.containsKey(GOVHttp.url)) {
				request.httpGetRequest.setHeader("Cookie",
						urlSessionIDMap.get(GOVHttp.url));
			} else {
				String session = MyApplication.getInstance()
						.getStringGlobalData("session", null);
				if (session != null) {
					request.httpGetRequest.setHeader("Cookie", session);
				}
			}
		} else if (httpMethod.equals("DELETE")) {
			request.httpDelete = new HttpDelete(urlString);
			if (urlSessionIDMap.containsKey(GOVHttp.url)) {
				request.httpDelete.setHeader("Cookie",
						urlSessionIDMap.get(GOVHttp.url));
			} else {
				String session = MyApplication.getInstance()
						.getStringGlobalData("session", null);
				if (session != null) {
					request.httpDelete.setHeader("Cookie", session);
				}
			}

		}

		return request;
	}

	public void setValueForKey(String key, String value) {
		data.put(key, value);
	}

	public void setData(Map<String, String> tmp) {
		for (Map.Entry<String, String> m : tmp.entrySet()) {
			data.put(m.getKey(), m.getValue());
		}
	}

	private String execute() throws ConnectionPoolTimeoutException,
			ConnectTimeoutException, SocketTimeoutException,
			ClientProtocolException, IOException, JSONException, Exception {
		HttpResponse response = null;
		if (this.httpMethod.equals("POST")) {
			ArrayList<BasicNameValuePair> postData = new ArrayList<BasicNameValuePair>();
			for (Map.Entry<String, String> m : data.entrySet()) {
				postData.add(new BasicNameValuePair(m.getKey(), m.getValue()));
			}
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData,
					HTTP.UTF_8);
			httpPost.setEntity(entity);
			response = client.execute(httpPost);
			// Log.i("status", response.getStatusLine().getStatusCode() + "");

		} else if (this.httpMethod.equals("GET")) {

			response = client.execute(httpGetRequest);

		} else if (this.httpMethod.equals("DELETE")) {
			response = client.execute(httpDelete);
		}
		if (response.getStatusLine().getStatusCode() == 200) {
			InputStream is = response.getEntity().getContent();
			result = StringUtils.convertStreamToString(is);

			Header[] headers = response.getHeaders("set-cookie");
			try {
				// 保存服务器返回的session
				for (int i = 0; i < headers.length; i++) {
					// Log.e("sessionid", headers<i>.getValue());
					String value = headers[i].getValue();
					value = value.substring(0, value.indexOf(";"));
					urlSessionIDMap.put(GOVHttp.url, value);
				}
			} catch (Exception e) {
				// TODO: handle exception
				throw e;
			}

			return result;
		} else if (response.getStatusLine().getStatusCode() == 500) {
			result = null;
			return result;
		} else if (response.getStatusLine().getStatusCode() == 900) {
			MyApplication.getInstance().removeTempGlobalData("session");
			return "No_Session";
		} else if (response.getStatusLine().getStatusCode() == 503) {
			InputStream is = response.getEntity().getContent();
			result = StringUtils.convertStreamToString(is);
			return "Maintain";
		}
		result = null;
		return null;
	}

	public Object execForJSONObject(Context context) {
		String json;
		try {
			json = this.execute();
			if (json != null) {
				if (json.equalsIgnoreCase("No_session")) {
					SysUtil sys = new SysUtil(context);
					sys.Time_out();

				} else if (json.equalsIgnoreCase("Maintain")) {
					SysUtil sys = new SysUtil(context);
					sys.Maintain();
					return new JSONObject(result);
				} else {
					return new JSONObject(result);
				}

			}
		} catch (ConnectionPoolTimeoutException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JSONObject();
	}

	public Object execForJSONArray(Context context) {
		String json;
		try {
			json = this.execute();
			if (json != null) {
				if (json.equalsIgnoreCase("No_session")) {
					SysUtil sys = new SysUtil(context);
					sys.Time_out();

				} else if (json.equalsIgnoreCase("Maintain")) {
					SysUtil sys = new SysUtil(context);
					sys.Maintain();
				} else {
					return new JSONArray(result);
				}
			}
		} catch (ConnectionPoolTimeoutException e) {
			e.printStackTrace();
			Toast.makeText(context, "网络请求超时，请重试!", Toast.LENGTH_SHORT).show();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JSONArray();
	}

	/**
	 * Simple {@link HttpEntityWrapper} that inflates the wrapped
	 * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
	 */
	private static class InflatingEntity extends HttpEntityWrapper {
		public InflatingEntity(HttpEntity wrapped) {
			super(wrapped);
		}

		public InputStream getContent() throws IOException {
			return new GZIPInputStream(wrappedEntity.getContent());
		}

		public long getContentLength() {
			return -1;
		}
	}

}
