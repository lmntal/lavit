package lavit.config;

import java.util.Map;

public abstract class ConfigPatch
{
	private int version;

	public ConfigPatch(int version)
	{
		this.version = version;
	}

	public int getVersion()
	{
		return version;
	}

	public abstract void apply(Map<String, String> table);
}
