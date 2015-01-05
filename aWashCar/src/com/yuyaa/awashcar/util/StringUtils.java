package com.yuyaa.awashcar.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;

/**
 * 字符串操作工具包
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class StringUtils {
	private final static Pattern emailer = Pattern
			.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
	// private final static SimpleDateFormat dateFormater = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// private final static SimpleDateFormat dateFormater2 = new
	// SimpleDateFormat("yyyy-MM-dd");

	private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		}
	};

	private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
		}
	};

	private final static ThreadLocal<SimpleDateFormat> dateFormater3 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
		}
	};

	/**
	 * 将字符串转位日期类型
	 * 
	 * @param sdate
	 * @return
	 */
	public static Date toDate(String sdate) {
		try {
			return dateFormater.get().parse(sdate);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 将字符串转位日期类型
	 * 
	 * @param mDuration
	 * @return
	 */
	public static String generateTime(String mDuration) {
		try {
			return dateFormater3.get().parse(mDuration).toString();
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 将字符串转位日期类型
	 * 
	 * @param mDuration
	 * @return
	 */
	public static String generateTime(long mDuration) {
		return dateFormater3.get().format(new Date(mDuration)).toString();
	}

	/**
	 * 以友好的方式显示距离
	 * 
	 * @param sdate
	 * @return
	 */
	public static String friendly_distance(int distance) {
		if (distance < 1000) {
			return "距离" + distance + "米";
		} else if (distance < 10000) {
			distance = distance / 100;
			float d = (float) (distance * 0.1);
			return "距离" + d + "千米";
		} else {
			return "距离>10千米";
		}
	}

	/**
	 * 以友好的方式显示时间
	 * 
	 * @param sdate
	 * @return
	 */
	public static String toDate(int time) {
		return dateFormater.get().format(new Date(time * 1000l));
	}

	public static String friendly_time(int time) {
		Date date = new Date(time * 1000l);
		return friendly_time(date);
	}

	public static String friendly_time(String time) {
		Date date = toDate(time);
		if (date == null) {
			return "Unknown";
		} else {
			return friendly_time(date);
		}
	}

	public static String friendly_time(Date time) {

		String ftime = "";
		Calendar cal = Calendar.getInstance();

		// 判断是否是同一天
		String curDate = dateFormater2.get().format(cal.getTime());
		String paramDate = dateFormater2.get().format(time);
		if (curDate.equals(paramDate)) {
			int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
			if (hour == 0)
				ftime = Math.max(
						(cal.getTimeInMillis() - time.getTime()) / 60000, 1)
						+ "分钟前";
			else
				ftime = hour + "小时前";
			return ftime;
		}

		long lt = time.getTime() / 86400000;
		long ct = cal.getTimeInMillis() / 86400000;
		int days = (int) (ct - lt);
		if (days <= 0) {
			int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
			if (hour <= 0) {
				int minute = (int) (Math.max(
						(cal.getTimeInMillis() - time.getTime()) / 60000, 1));
				if (minute <= 5) {
					ftime = "刚刚";
				} else {
					ftime = minute + "分钟前";
				}

			} else
				ftime = hour + "小时前";
		} else if (days == 1) {
			ftime = "昨天 "
					+ new SimpleDateFormat("HH:mm", Locale.CHINA).format(time);
		} else if (days == 2) {
			ftime = "前天 "
					+ new SimpleDateFormat("HH:mm", Locale.CHINA).format(time);
		} else if (days > 2 && days <= 10) {
			ftime = days + "天前 "
					+ new SimpleDateFormat("HH:mm", Locale.CHINA).format(time);
		} else if (days > 10) {
			ftime = dateFormater2.get().format(time);
		}
		return ftime;
	}

	/**
	 * 判断给定字符串时间是否为今日
	 * 
	 * @param sdate
	 * @return boolean
	 */
	public static boolean isToday(String sdate) {
		boolean b = false;
		Date time = toDate(sdate);
		Date today = new Date();
		if (time != null) {
			String nowDate = dateFormater2.get().format(today);
			String timeDate = dateFormater2.get().format(time);
			if (nowDate.equals(timeDate)) {
				b = true;
			}
		}
		return b;
	}

	/**
	 * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
	 * 
	 * @param input
	 * @return boolean
	 */
	public static boolean isEmpty(String input) {
		if (input == null || "".equals(input) || "null".equals(input))
			return true;

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断是不是一个合法的电子邮件地址
	 * 
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {
		if (email == null || email.trim().length() == 0)
			return false;
		return emailer.matcher(email).matches();
	}

	/**
	 * 字符串转整数
	 * 
	 * @param str
	 * @param defValue
	 * @return
	 */
	public static int toInt(String str, int defValue) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
		}
		return defValue;
	}

	/**
	 * 对象转整数
	 * 
	 * @param obj
	 * @return 转换异常返回 0
	 */
	public static int toInt(Object obj) {
		if (obj == null)
			return 0;
		return toInt(obj.toString(), 0);
	}

	/**
	 * 对象转整数
	 * 
	 * @param obj
	 * @return 转换异常返回 0
	 */
	public static long toLong(String obj) {
		try {
			return Long.parseLong(obj);
		} catch (Exception e) {
		}
		return 0;
	}

	/**
	 * 字符串转布尔值
	 * 
	 * @param b
	 * @return 转换异常返回 false
	 */
	public static boolean toBool(String b) {
		try {
			return Boolean.parseBoolean(b);
		} catch (Exception e) {
		}
		return false;
	}

	public static Map<String, String> string2Map(String url) {
		Map<String, String> map = new HashMap<String, String>();
		int index = url.indexOf('?');
		String newURL = url.substring(index + 1);
		try {
			String[] splitStrings = newURL.split("&");
			for (String string : splitStrings) {
				String[] keyValues = string.split("=");
				map.put(keyValues[0], keyValues[1]);
			}
		} catch (NullPointerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return map;
	}

	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public static int getFontHeight(float fontSize) {
		Paint m_paint = new Paint();
		m_paint.setTextSize(fontSize);
		FontMetrics fm = m_paint.getFontMetrics();
		return (int) Math.ceil(fm.descent - fm.top) + 2;
	}

	/**
	 * extract file name form the given file path
	 * 
	 * @param filePath
	 *            path to the file, like 'c:/test.jpg', 'c:\\test.jpg'
	 * @param withExtention
	 *            indicate contain file.extention. true : contain | false :
	 *            ignore
	 * @return fileName file.name;
	 */
	public static String getFileName(String filePath, boolean withExtention) {
		int sep = filePath.lastIndexOf("\\") == -1 ? filePath.lastIndexOf("/")
				: filePath.lastIndexOf("\\");
		if (withExtention)
			return filePath.substring(sep + 1);
		return filePath.substring(sep + 1, filePath.lastIndexOf("."));
	}

	/**
	 * get path to the given file , e.g. : c:\test\aaa.html -> c:\test\
	 * 
	 * @param fileFullPath
	 *            path to file;
	 * @return
	 */
	public static String getFilePath(String fileFullPath) {
		int sep = fileFullPath.lastIndexOf("\\") == -1 ? fileFullPath
				.lastIndexOf("/") : fileFullPath.lastIndexOf("\\");
		return fileFullPath.substring(0, sep + 1);
	}

}
