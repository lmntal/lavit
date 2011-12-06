package lavit.multiedit.coloring;

import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

@SuppressWarnings("serial")
public class LmnEditorKit extends StyledEditorKit
{
	protected LmnContext preferences;

	public LmnContext getStylePreferences()
	{
		if (preferences == null)
		{
			preferences = new LmnContext();
		}
		return preferences;
	}

	public void setStylePreferences(LmnContext prefs)
	{
		preferences = prefs;
	}

	@Override
	public Document createDefaultDocument()
	{
		return new LmnDocument();
	}

	@Override
	public final ViewFactory getViewFactory()
	{
		return getStylePreferences();
	}
}
