/*
 *   Copyright (c) 2008, Ueda Laboratory LMNtal Group <lmntal@ueda.info.waseda.ac.jp>
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are
 *   met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *    3. Neither the name of the Ueda Laboratory LMNtal Group nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package lavit;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lavit.util.FileUtils;
import lavit.util.StringUtils;

public final class Env
{
	public static final String APP_NAME    = "LaViT";
	public static final String APP_VERSION = "2.5.4";
	public static final String APP_DATE    = "2012/10/21";
	public static final String APP_HREF    = "http://www.ueda.info.waseda.ac.jp/lmntal/lavit/";

	public static final String LMNTAL_VERSION = "LMNtal : 1.21 (2011/12/26)";
	public static final String SLIM_VERSION   = "SLIM : 2.2.2 (2012/05/12)";
	public static final String UNYO_VERSION   = "UNYO UNYO : 1.1.1 (2010/03/07)";

	public static final String DIR_NAME_SLIM = "slim-2.2.2";
	public static final String DIR_NAME_UNYO = "unyo1_1_1";
	public static final String DIR_NAME_LTL2BA = "ltl2ba-1.1";

	public static final String LMNTAL_LIBRARY_DIR = "lmntal";

	public static final String[] FONT_SIZE_LIST = {"8","9","10","11","12","14","16","18","20","24","28","32","36","40","44","48","54","60","66","72","80","88","96","106"};

	private static final String ENV_FILE = "env.txt";
	private static final String ENV_DEFAULT_FILE = "env_default.txt";

	private static Env env = null;
	private static Properties prop = new Properties();

	private static String cachedLMNtalVersion = null;
	private static String cachedSLIMVersion = null;

	private static List<Image> appIcons;

	public Env()
	{
		env = this;

		//ファイルの作成
		boolean firstMake = false;
		try
		{
			File e = new File(ENV_FILE);
			if (!e.exists())
			{
				System.out.println("make " + ENV_FILE);
				e.createNewFile();
				firstMake = true;
			}
		}
		catch (IOException e)
		{
			System.err.println(ENV_FILE + " make error. check " + new File(".").getAbsolutePath());
			System.exit(0);
		}

		//ファイルの読み込み
		try
		{
			InputStream in = getInputStreamOfFile(ENV_FILE);
			prop.load(in);
			in.close();
		}
		catch (IOException e)
		{
			System.err.println("read error. check " + ENV_FILE);
			System.exit(0);
		}

		//バージョンアップの処理（設定値がない場合はその値を入れる）
		Properties default_prop = new Properties();
		try
		{
			InputStream in = getInputStreamOfFile(ENV_DEFAULT_FILE);
			default_prop.load(in);
			in.close();
			for (Object k : default_prop.keySet())
			{
				String key = (String)k;
				String value = default_prop.getProperty(key);
				if (!prop.containsKey(key))
				{
					prop.setProperty(key, value);
					if (!firstMake)
					{
						System.out.println("auto update " + ENV_FILE + " : " + key + "=" + value);
					}
				}
			}
		}
		catch (IOException e)
		{
			System.err.println("read error. check " + ENV_DEFAULT_FILE);
		}
	}

	public static void save()
	{
		try
		{
			//普通に保存
			FileOutputStream out = new FileOutputStream(ENV_FILE);
			prop.store(out, APP_NAME + " " + APP_VERSION);
			out.close();

			//ソートで再保存
			LineNumberReader reader = new LineNumberReader(new FileReader(ENV_FILE));
			ArrayList<String> lines = new ArrayList<String>();
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				lines.add(line);
			}
			reader.close();
			Collections.sort(lines);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ENV_FILE)));
			for (String str : lines)
			{
				writer.write(str+"\n");
			}
			writer.close();
		}
		catch (IOException e)
		{
			System.err.println("save error. check "+ENV_FILE);
		}
	}

	public static String get(String key)
	{
		return prop.getProperty(key);
	}

	public static String get(String key, String defaultValue)
	{
		String value = prop.getProperty(key);
		return StringUtils.nullOrEmpty(value) ? defaultValue : value;
	}

	public static int getInt(String key) throws NumberFormatException
	{
		return Integer.valueOf(prop.getProperty(key));
	}

	public static int getInt(String key, int defaultValue)
	{
		int value = defaultValue;
		try
		{
			value = Integer.parseInt(get(key));
		}
		catch (NumberFormatException e)
		{
		}
		return value;
	}

	public static boolean is(String key)
	{
		if (!prop.containsKey(key))
		{
			System.err.println("read error. check " + key + " in " + ENV_FILE);
		}
		return Boolean.valueOf(prop.getProperty(key));
	}

	public static boolean isSet(String key)
	{
		return !StringUtils.nullOrEmpty(get(key));
	}

	public static void setProcessEnvironment(Map<String, String> map)
	{
		map.put("LMNTAL_HOME", getLmntalLinuxPath());

		if (isWindows())
		{
			char sep = File.separatorChar;
			String cygwinDir = get("WINDOWS_CYGWIN_DIR");
			String pathes = "";
			pathes += cygwinDir + sep + "bin";
			pathes += cygwinDir + sep + "usr" + sep + "bin";
			pathes += cygwinDir + sep + "usr" + sep + "local" + sep + "bin";

			char pathSep = File.pathSeparatorChar;
			boolean put = false;
			for (String key : new String[] { "path", "Path", "PATH" })
			{
				String value = map.get(key);
				if (value != null)
				{
					map.put(key, pathes + pathSep + value);
					put = true;
				}
			}
			if (!put)
			{
				map.put("PATH", pathes);
			}
		}
	}

	public static String getDirNameOfSlim()
	{
		return get("DIR_NAME_SLIM", DIR_NAME_SLIM);
	}

	public static String getDirNameOfUnyo()
	{
		return get("DIR_NAME_UNYO", DIR_NAME_UNYO);
	}

	public static String getDirNameOfLtl2ba()
	{
		return get("DIR_NAME_LTL2BA", DIR_NAME_LTL2BA);
	}

	/**
	 * 文字列 {@code path} に半角空白文字 (0x20) が含まれる場合、この文字列を二重引用符で囲んだ文字列を返す。
	 * 半角空白文字が含まれない場合、{@code path} をそのまま返す。
	 */
	public static String getSpaceEscape(String path)
	{
		if (path.indexOf(" ") == -1)
		{
			return path;
		}
		else
		{
			return "\"" + path + "\"";
		}
	}

	public static String getSlimInstallPath()
	{
		return get("path.slim.install", LMNTAL_LIBRARY_DIR + File.separator + "installed");
	}

	public static String getSlimInstallLibraryPath()
	{
		char sep = File.separatorChar;
		return getSlimInstallPath() + sep + "share" + sep + "slim" + sep + "lib";
	}

	public static String getLmntalLinuxPath()
	{
		String path = new File(LMNTAL_LIBRARY_DIR).getAbsolutePath();
		path = getLinuxStylePath(path);
		return path;
	}

	public static String getSlimInstallLinuxPath()
	{
		String path = new File(getSlimInstallPath()).getAbsolutePath();
		path = getLinuxStylePath(path);
		return path;
	}

	public static String getLinuxStylePath(String path)
	{
		if (File.separatorChar == '\\')
		{
			path = path.replace('\\', '/');
			if (path.contains(":"))
			{
				String[] part = path.split(":");
				if (1 < part.length && !part[0].equals(""))
				{
					path = "/cygdrive/" + part[0] + part[1];
				}
			}
		}
		return path;
	}

	public static String getLmntalCmd()
	{
		char sep = File.separatorChar;
		String cmd = "java";
		cmd += " -classpath ";
		cmd += LMNTAL_LIBRARY_DIR + sep + "bin" + sep + "lmntal.jar" + File.pathSeparator;
		cmd += LMNTAL_LIBRARY_DIR + sep + "lib" + sep + "std_lib.jar";
		cmd += " -DLMNTAL_HOME=" + LMNTAL_LIBRARY_DIR;
		cmd += " runtime.FrontEnd --interpret ";
		return cmd;
    }

	public static String getBinaryAbsolutePath(String cmd)
	{
		char sep = File.separatorChar;
		String cygwinDir = get("WINDOWS_CYGWIN_DIR");
		String[] pathes =
		{
			sep + "usr" + sep + "local" + sep + "bin" + sep + cmd,
			sep + "usr" + sep + "bin" + sep + cmd,
			sep + "bin" + sep + cmd,
		};
		for (String path : pathes)
		{
			if (isWindows())
			{
				path = cygwinDir + path + ".exe";
			}
			if (FileUtils.exists(path))
			{
				return path;
			}
		}
		return cmd;
	}

	/**
	 * Finds a directory whose name starts with "slim" in "./lmntal".
	 * @return If found, returns path string of the directory found first.
	 * Otherwise, returns empty string.
	 */
	public static String estimateSlimSourcePath()
	{
		File lmntalDir = new File("lmntal");
		if (lmntalDir.exists() && lmntalDir.isDirectory())
		{
			for (File file : lmntalDir.listFiles())
			{
				if (file.isDirectory() && file.getName().startsWith("slim"))
				{
					return file.getPath();
				}
			}
		}
		return "";
	}

	public static String getSlimBinaryName()
	{
		return isWindows() ? "slim.exe" : "slim";
	}

	public static String getLtl2baBinaryName()
	{
		return isWindows() ? "ltl2ba.exe" : "ltl2ba";
	}

	public static void set(String key, String value)
	{
		prop.setProperty(key, value);
	}

	public static void set(String key, boolean value)
	{
		prop.setProperty(key, String.valueOf(value));
	}

	public static void set(String key, int value)
	{
		prop.setProperty(key, String.valueOf(value));
	}

	// jarファイル化した場合のファイル入力の差を吸収
	public static InputStream getInputStreamOfFile(String filename)
	{
		InputStream in = null;
		try
		{
			if (FileUtils.exists(filename))
			{
				in = new FileInputStream(filename);
			}
			else
			{
				in = env.getClass().getResourceAsStream("/" + filename);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return in;
	}

	// jarファイル化した場合のファイル入力の差を吸収
	public static Image getImageOfFile(String filename)
	{
		if (FileUtils.exists(filename))
		{
			return Toolkit.getDefaultToolkit().getImage(filename);
		}
		else
		{
			URL fileUrl = Env.class.getResource("/" + filename);
			return Toolkit.getDefaultToolkit().getImage(fileUrl);
		}
	}

	public static boolean isWindows()
	{
		return File.pathSeparatorChar == ';';
	}

	public static List<Image> getApplicationIcons()
	{
		if (appIcons == null)
		{
			List<Image> icons = new ArrayList<Image>();
			icons.add(Env.getImageOfFile("img/app_icons/app_icon_16.png"));
			icons.add(Env.getImageOfFile("img/app_icons/app_icon_32.png"));
			icons.add(Env.getImageOfFile("img/app_icons/app_icon_48.png"));
			icons.add(Env.getImageOfFile("img/app_icons/app_icon_64.png"));
			appIcons = Collections.unmodifiableList(icons);
		}
		return appIcons;
	}

	/**
	 * Gets LMNtal version by executing {@code java -jar lmntal.jar --version}.
	 */
	public static String getLMNtalVersion()
	{
		if (cachedLMNtalVersion != null)
		{
			return cachedLMNtalVersion;
		}

		String lmntalPath = LMNTAL_LIBRARY_DIR + File.separator + "bin" + File.separatorChar + "lmntal.jar";

		ProcessBuilder pb = new ProcessBuilder("java", "-classpath", lmntalPath, "runtime.FrontEnd", "--version");
		pb.redirectErrorStream(true);

		String version = "";
		try
		{
			Process p = pb.start();
			p.getOutputStream().close();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = reader.readLine();
			p.getInputStream().close();
			p.getErrorStream().close();
			p.waitFor();

			if (!StringUtils.nullOrEmpty(line))
			{
				Pattern pat = Pattern.compile("\\d+\\.\\d+\\.\\d+");
				Matcher m = pat.matcher(line);
				if (m.find())
				{
					version = m.group();
				}
			}
		}
		catch (IOException e)
		{
		}
		catch (InterruptedException e)
		{
		}
		if (StringUtils.nullOrEmpty(version))
		{
			version = "1.21 (2011/12/26)"; // previous version of --version implementation
		}
		cachedLMNtalVersion = version;
		return version;
	}

	/**
	 * Gets SLIM version by executing {@code slim --version}.
	 */
	public static String getSlimVersion()
	{
		if (cachedSLIMVersion == null)
		{
			String slimPath = get("SLIM_EXE_PATH");
			String version = "";
			if (!StringUtils.nullOrEmpty(slimPath))
			{
				ProcessBuilder pb = new ProcessBuilder(slimPath, "--version");
				setProcessEnvironment(pb.environment());
				pb.redirectErrorStream(true);

				try
				{
					Process p = pb.start();
					p.getOutputStream().close();
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line = reader.readLine();
					p.getInputStream().close();
					p.getErrorStream().close();
					p.waitFor();

					if (!StringUtils.nullOrEmpty(line))
					{
						Pattern pat = Pattern.compile("\\d+\\.\\d+\\.\\d+");
						Matcher m = pat.matcher(line);
						if (m.find())
						{
							version = m.group();
						}
					}
				}
				catch (IOException e)
				{
				}
				catch (InterruptedException e)
				{
				}
			}
			cachedSLIMVersion = version;
		}
		return cachedSLIMVersion;
	}

	// TODO: expel timer logics

	private static Map<String, Long> watchNowTimes = new HashMap<String, Long>();
	private static Map<String, Long> watchSumTimes = new HashMap<String, Long>();
	private static Map<String, Integer> watchCount = new HashMap<String, Integer>();

	public static void startWatch(String key)
	{
		watchNowTimes.put(key, System.currentTimeMillis());
	}

	public static void stopWatch(String key)
	{
		long t;
		if (watchNowTimes.containsKey(key))
		{
			t = System.currentTimeMillis() - watchNowTimes.get(key);
		}
		else
		{
			t = 0;
		}
		if (watchSumTimes.containsKey(key))
		{
			long sum = watchSumTimes.get(key);
			watchSumTimes.put(key, sum + t);
			watchCount.put(key, watchCount.get(key) + 1);
		}
		else
		{
			watchSumTimes.put(key, t);
			watchCount.put(key, 1);
		}
	}

	public static void dumpWatch()
	{
		DecimalFormat f = new DecimalFormat("####.##");
		if (watchSumTimes.size() > 0)
		{
			System.out.println("---- watch = " + watchSumTimes.size() + " ----");
		}
		for (String key : watchSumTimes.keySet())
		{
			double t = watchSumTimes.get(key) / 1000.0;
			System.out.println("watch[" + key + "] : " + f.format(t) + " (" + watchCount.get(key) + ")");
		}
		if (watchSumTimes.size() > 0)
		{
			System.out.println();
		}
		watchSumTimes.clear();
	}
}
