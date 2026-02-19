package lavit.config.patch;

import java.util.Map;

import lavit.config.ConfigPatch;

public class V001_AddVersionAndRewriteLang extends ConfigPatch
{
	public V001_AddVersionAndRewriteLang()
	{
		super(1);
	}

	public void apply(Map<String, String> table)
	{
		String lang = table.get("LANG");
		if ("jp".equals(lang))
		{
			table.put("LANG", "ja");
		}
	}
}
