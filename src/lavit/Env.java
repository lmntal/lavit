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

import java.awt.Font;
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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lavit.config.ConfigUpdater;
import lavit.localizedtext.Msg;
import lavit.util.FileUtils;
import lavit.util.FontSizeUtils;
import lavit.util.StringUtils;

public final class Env
{
	public static final String APP_NAME    = "LaViT";
	public static final String APP_VERSION = "2.8.9";
	public static final String APP_DATE    = "2017/06/14";
	public static final String APP_HREF    = "http://www.ueda.info.waseda.ac.jp/lmntal/lavit/";

	public static final String LMNTAL_VERSION = "LMNtal : 1.44 (2017/06/13)";
	public static final String SLIM_VERSION   = "SLIM : 2.3.1 (2017/06/13)";
	public static final String UNYO_VERSION   = "UNYO UNYO : 1.1.1 (2010/03/07)";

	public static final String DIR_NAME_SLIM     = "slim-2.3.1";
	public static final String DIR_NAME_UNYO     = "unyo1_1_1";
	public static final String DIR_NAME_GRAPHENE = "graphene";
	public static final String DIR_NAME_LTL2BA   = "ltl2ba";

	public static final String LMNTAL_LIBRARY_DIR = "lmntal";

	private static final String ENV_FILE = "env.txt";
	private static final String ENV_DEFAULT_FILE = "env_default.txt";
	private static final String DIR_NAME_PROPERTIES = "properties";

	private static Properties prop = new Properties();

	private static String cachedLMNtalVersion = null;
	private static String cachedSLIMVersion = null;

	private static List<Image> appIcons;

	private static Msg msg;

	private Env() { }

	public static boolean loadEnvironment()
	{
		File envFile = new File(ENV_FILE);
		try
		{
			if (!envFile.exists())
			{
				System.err.println("creating " + ENV_FILE);
				envFile.createNewFile();
			}
		}
		catch (IOException e)
		{
			System.err.println("Error: failed to create " + envFile);
			return false;
		}
		try
		{
			InputStream in = getInputStreamOfFile(ENV_FILE);
			prop.load(in);
			in.close();
		}
		catch (IOException e)
		{
			System.err.println("Error: failed to read file " + ENV_FILE);
			return false;
		}
		loadDefault();
		return true;
	}

	private static void loadDefault()
	{
		//バージョンアップの処理（設定値がない場合はその値を入れる）
		Properties default_prop = new Properties();
		try
		{
			InputStream in = Env.class.getResourceAsStream("/resources/" + ENV_DEFAULT_FILE);
			default_prop.load(in);
			in.close();

			for (Map.Entry<Object, Object> ent : default_prop.entrySet())
			{
				String key = (String)ent.getKey();
				String value = (String)ent.getValue();
				if (!prop.containsKey(key))
				{
					prop.setProperty(key, value);
					System.out.println("auto update " + ENV_FILE + " : " + key + "=" + value);
				}
			}
		}
		catch (IOException e)
		{
			System.err.println("read error. check " + ENV_DEFAULT_FILE);
		}
		ConfigUpdater.update(prop);
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

	public static void loadMsg()
	{
		msg = new Msg(Env.get("LANG", ""));
	}

	public static String getMsg(int msgid)
	{
		if (msg == null)
		{
			throw new RuntimeException("Messages are not loaded. Call the method Env.loadMsg().");
		}
		return msg.get(msgid);
	}

	public static String getMsg(int msgid, String ... args)
	{
		if (msg == null)
		{
			throw new RuntimeException("Messages are not loaded. Call the method Env.loadMsg().");
		}
		return msg.get(msgid, args);
	}

	public static Collection<String[]> getEntries()
	{
		List<String[]> entries = new LinkedList<String[]>();
		for (Map.Entry<Object, Object> e : prop.entrySet())
		{
			entries.add(new String[] { (String)e.getKey(), (String)e.getValue() });
		}
		return entries;
	}

	/**
	 * キー値 key に設定されている値を取得する。
	 * キー値 key に対応する項目が存在しない場合は{@code null}を返す。
	 */
	public static String get(String key)
	{
		return prop.getProperty(key);
	}

	/**
	 * キー値 key に設定されている値を取得する。
	 * キー値 key に対応する項目が存在しない場合もしくは値が空文字列の場合は defaultValue を返す。
	 */
	public static String get(String key, String defaultValue)
	{
		String value = prop.getProperty(key);
		return StringUtils.nullOrEmpty(value) ? defaultValue : value;
	}

	/**
	 * キー値 key に設定されている値を整数値として取得する。
	 * 値が有効な整数値でない場合、例外 {@link NumberFormatException} を発生させる。
	 */
	public static int getInt(String key) throws NumberFormatException
	{
		return Integer.valueOf(prop.getProperty(key));
	}

	/**
	 * キー値 key に設定されている値を整数値として取得する。
	 * 値が有効な整数値でない場合にデフォルト値 defaultValue を返す。
	 * このメソッドは例外安全である。
	 */
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

	/**
	 * 値を文字列リストとして取得する。
	 */
	public static List<String> getList(String key)
	{
		return new ArrayList<String>(StringUtils.splitToList(get(key, ""), "\\s+"));
	}

	/**
	 * 値を文字列集合として取得する。
	 */
	public static Set<String> getSet(String key)
	{
		return new HashSet<String>(StringUtils.splitToSet(get(key, ""), "\\s+"));
	}

	public static void setList(String key, Collection<String> values)
	{
		set(key, StringUtils.join(values, " "));
	}

	public static Font getEditorFont()
	{
		String fontFamily = get("EDITER_FONT_FAMILY", Font.MONOSPACED);
		int fontSize = FontSizeUtils.getActualFontSize(getInt("EDITER_FONT_SIZE", 12));
		return new Font(fontFamily, Font.PLAIN, fontSize);
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
			char pathSep = File.pathSeparatorChar;
			String cygwinDir = get("WINDOWS_CYGWIN_DIR");
			String paths = "";
			paths += cygwinDir + sep + "bin" + pathSep;
			paths += cygwinDir + sep + "usr" + sep + "bin" + pathSep;
			paths += cygwinDir + sep + "usr" + sep + "local" + sep + "bin";

			boolean put = false;
			for (String key : new String[] { "path", "Path", "PATH" })
			{
				String value = map.get(key);
				if (value != null)
				{
					map.put(key, paths + pathSep + value);
					put = true;
				}
			}
			if (!put)
			{
				map.put("PATH", paths);
			}
		}

		if (isMac())
		{
			// MacでFinderからjarをダブルクリックしてLaViTを起動した場合は、
			// シェルの環境変数ではなくFinderの環境変数が設定される。
			// その場合、PATHに/usr/loca/binや/opt/local/binが含まれていない(Yosemiteから?)。
			// SLIMをインストールするときに使用するautotoolsのコマンドは、
			// HomebrewやMacPortsでインストールすることが多いので、
			// /usr/local/binや/opt/local/binがパスに含まれていないとコマンドが見つからない。
			// この問題を回避するため、ここでパスに追加する。
			String paths = System.getenv("PATH");
			Set<String> pathsSet = new HashSet<String>(Arrays.asList(paths.split(":")));
			if (!pathsSet.contains("/usr/local/bin"))
			{
				paths = "/usr/local/bin:" + paths;
			}
			if (!pathsSet.contains("/opt/local/bin"))
			{
				paths = "/opt/local/bin:" + paths;
			}
			map.put("PATH", paths);
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

	public static String getDirNameOfGraphene()
	{
		return get("DIR_NAME_GRAPHENE", DIR_NAME_GRAPHENE);
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
		cmd += " -cp ";
		cmd += LMNTAL_LIBRARY_DIR + sep + "lib" + sep + "std_lib.jar";
		cmd += " -DLMNTAL_HOME=" + LMNTAL_LIBRARY_DIR + " -jar ";
		cmd += LMNTAL_LIBRARY_DIR + sep + "bin" + sep + "lmntal.jar ";
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
				in = Env.class.getResourceAsStream("/" + filename);
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

	public static boolean isMac()
	{
		return System.getProperty("os.name").toLowerCase().contains("mac");
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

	public static File getPropertyFile(String fileName)
	{
		File dir = new File(DIR_NAME_PROPERTIES);
		if (!dir.exists())
		{
			if (!dir.mkdir())
			{
				System.err.println("Could not create directory '" + dir + "'");
				return null;
			}
		}
		return new File(dir.getAbsolutePath() + File.separator + fileName);
	}

	public static List<File> loadLastFiles()
	{
		List<File> files = new ArrayList<File>();

		File propFile = getPropertyFile("lastfiles");
		if (propFile == null)
		{
			return files;
		}

		String charset = "UTF-8";
		try
		{
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(propFile), charset));
			String line;
			while ((line = reader.readLine()) != null)
			{
				files.add(new File(line));
			}
			reader.close();
		}
		catch (FileNotFoundException e)
		{
		}
		catch (IOException e)
		{
		}
		return files;
	}

	public static void saveOpenedFilePathes(List<File> files)
	{
		File propFile = getPropertyFile("lastfiles");

		if (propFile == null)
		{
			return;
		}

		String charset = "UTF-8";
		try
		{
			PrintWriter out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(propFile), charset)));
			for (File file : files)
			{
				out.println(file.getAbsolutePath());
			}
			out.close();
		}
		catch (FileNotFoundException e)
		{
		}
		catch (UnsupportedEncodingException e)
		{
		}
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
				version = line;
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
					e.printStackTrace();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			cachedSLIMVersion = version;
		}
		return cachedSLIMVersion;
	}
}
