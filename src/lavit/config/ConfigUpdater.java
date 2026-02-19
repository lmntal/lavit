package lavit.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import lavit.config.patch.V001_AddVersionAndRewriteLang;

public class ConfigUpdater
{
	public static final String KEY_CONFIG_VERSION = "config.version";

	private TreeMap<Integer, ConfigPatch> patches = new TreeMap<Integer, ConfigPatch>();

	public ConfigUpdater()
	{
		registerPatch(new V001_AddVersionAndRewriteLang());
	}

	public void applyPatches(Map<String, String> table)
	{
		int version = 0;
		String versionText = table.get(KEY_CONFIG_VERSION);
		if (versionText != null)
		{
			try
			{
				version = Integer.parseInt(versionText);
			}
			catch (NumberFormatException e)
			{
			}
		}

		for (ConfigPatch patch : patches.tailMap(version, false).values())
		{
			System.err.println("Updating config to version " + patch.getVersion());
			patch.apply(table);
			table.put(KEY_CONFIG_VERSION, Integer.toString(patch.getVersion()));
		}
	}

	@Deprecated
	private void applyPatches(Properties prop)
	{
		Map<String, String> map = new HashMap<String, String>();
		for (Map.Entry<Object, Object> e : prop.entrySet())
		{
			map.put((String)e.getKey(), (String)e.getValue());
		}

		applyPatches(map);

		for (Map.Entry<String, String> e : map.entrySet())
		{
			prop.setProperty(e.getKey(), e.getValue());
		}
	}

	private void registerPatch(ConfigPatch patch)
	{
		patches.put(patch.getVersion(), patch);
	}

	public static void update(Map<String, String> table)
	{
		new ConfigUpdater().applyPatches(table);
	}

	@Deprecated
	public static void update(Properties prop)
	{
		new ConfigUpdater().applyPatches(prop);
	}
}
