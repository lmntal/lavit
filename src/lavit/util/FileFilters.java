package lavit.util;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import lavit.Lang;

public final class FileFilters
{
	private static final FileFilter lmnFileFilter = new FileNameExtensionFilter("LMNtal " + Lang.d[5] + " (*.lmn)", "lmn");
	private static final FileFilter ilFileFilter = new FileNameExtensionFilter(Lang.d[9] + " (*.il, *.tal)", "il", "tal");

	private FileFilters() { }

	public static FileFilter getLMNFileFilter()
	{
		return lmnFileFilter;
	}

	public static FileFilter getILFileFilter()
	{
		return ilFileFilter;
	}
}
