package lavit.localizedtext;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public final class Msg
{
	private static final String RES = "res";
	private static final String DIR_NAME_LANG = "lang";
	private static final String IDMAP_NAME = "idmap";

	private String[] messages;

	public Msg()
	{
		Map<String, Integer> idmap = loadIDMap();
		messages = new String[idmap.size()];
		loadDefaultMessages(idmap);
	}

	public Msg(String langCode)
	{
		Map<String, Integer> idmap = loadIDMap();
		messages = new String[idmap.size()];
		loadDefaultMessages(idmap);
		loadLocalizedMessages(idmap, langCode);
	}

	/**
	 * @param id message id defined in the class MsgID
	 */
	public String get(int id, String ... args)
	{
		String s = messages[id];
		if (s != null)
		{
			for (int i = 0; i < args.length; i++)
			{
				s = s.replace("$" + (i + 1), args[i]);
			}
			return unescape(s);
		}
		throw new NoSuchElementException("Message ID " + id);
	}

	private void loadDefaultMessages(Map<String, Integer> idmap)
	{
		String fileName = DIR_NAME_LANG + "/messages.txt";
		loadFromResource(fileName, idmap, messages);
	}

	private void loadLocalizedMessages(Map<String, Integer> idmap, String langCode)
	{
		String fileName = DIR_NAME_LANG + "/messages-" + langCode + ".txt";
		load(fileName, idmap, messages);
	}

	private static void load(String path, Map<String, Integer> idmap, String[] table)
	{
		InputStream in = open(path);
		if (in != null)
		{
			loadFromInputStream(in, idmap, table);
		}
	}

	private static void loadFromResource(String name, Map<String, Integer> idmap, String[] table)
	{
		InputStream in = openResource(name);
		if (in != null)
		{
			loadFromInputStream(in, idmap, table);
		}
	}

	private static void loadFromInputStream(InputStream in, Map<String, Integer> idmap, String[] table)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] kv = splitKeyValue(line, '=');
				if (kv != null)
				{
					Integer id = idmap.get(kv[0]);
					if (id == null)
					{
						System.err.println("Warning: message name '" + kv[0] + "' is undefined.");
						continue;
					}
					table[id] = kv[1];
				}
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static Map<String, Integer> loadIDMap()
	{
		Map<String, Integer> idmap = new HashMap<String, Integer>();
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(openResource(IDMAP_NAME), "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] kv = splitKeyValue(line, '=');
				if (kv != null)
				{
					idmap.put(kv[0], Integer.parseInt(kv[1]));
				}
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return idmap;
	}

	private static InputStream open(String path)
	{
		try
		{
			return new FileInputStream("./" + path);
		}
		catch (FileNotFoundException e)
		{
			return openResource(path);
		}
	}

	private static InputStream openResource(String path)
	{
		return Msg.class.getResourceAsStream(RES + "/" + path);
	}

	private static String[] splitKeyValue(String s, char delim)
	{
		int i = s.indexOf(delim);
		if (i != -1)
		{
			return new String[] { s.substring(0, i).trim(), s.substring(i + 1).trim() };
		}
		return null;
	}

	private static String unescape(String s)
	{
		return s.replace("\\n", "\n").replace("\\t", "\t").replace("\\dollars", "$").replace("\\backslash", "\\");
	}
}
