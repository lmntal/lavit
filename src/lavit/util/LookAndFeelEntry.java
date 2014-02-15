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

package lavit.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * thin wrapper of {@link LookAndFeelInfo} to bind its name and class name.
 * @author Yuuki SHINOBU
 * @since April 1, 2012
 */
public abstract class LookAndFeelEntry
{
	public static final String SYSTEM_DEFAULT_NAME = "System Default";

	/**
	 * @return the class name of this look-and-feel.
	 */
	public abstract String getClassName();

	/**
	 * @return the name of this look-and-feel.
	 */
	public abstract String getName();

	@Override
	public final String toString()
	{
		return getName();
	}

	private static List<LookAndFeelEntry> entries;
	private static LookAndFeelEntry systemDefault;

	/**
	 * <p>
	 * Returns an array of {@code LookAndFeelEntry} that are supported in this system.
	 * the array contains SystemDefault Look & Feel.
	 * </p>
	 */
	public static LookAndFeelEntry[] getSupportedLookAndFeelEntries()
	{
		if (entries == null)
		{
			entries = initEntries();
		}
		LookAndFeelEntry[] arr = new LookAndFeelEntry[entries.size()];
		entries.toArray(arr);
		return arr;
	}

	/**
	 * <p>
	 * Returns {@code LookAndFeelEntry} represented by {@code name} if exists,
	 * or returns system-default {@code LookAndFeelEntry} otherwise.
	 * </p>
	 * @throws NullPointerException the argument {@code name} is {@code null}.
	 */
	public static LookAndFeelEntry getLookAndFeelEntry(String name)
	{
		if (name == null)
			throw new NullPointerException("name");

		if (entries == null)
		{
			entries = initEntries();
		}
		for (LookAndFeelEntry ent : entries)
		{
			if (ent.getName().equals(name))
			{
				return ent;
			}
		}
		return getSystemDefaultLookAndFeelEntry();
	}

	private static LookAndFeelEntry getSystemDefaultLookAndFeelEntry()
	{
		if (systemDefault == null)
		{
			systemDefault = new SystemDefaultLookAndFeelEntry();
		}
		return systemDefault;
	}

	private static List<LookAndFeelEntry> initEntries()
	{
		List<LookAndFeelEntry> entries = new ArrayList<LookAndFeelEntry>();
		for (LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels())
		{
			entries.add(new SpecifiedLookAndFeelEntry(lafInfo));
		}
		entries.add(getSystemDefaultLookAndFeelEntry());
		return entries;
	}

	private static final class SpecifiedLookAndFeelEntry extends LookAndFeelEntry
	{
		private LookAndFeelInfo lafInfo;

		public SpecifiedLookAndFeelEntry(LookAndFeelInfo lafInfo)
		{
			this.lafInfo = lafInfo;
		}

		@Override
		public String getClassName()
		{
			return lafInfo.getClassName();
		}

		@Override
		public String getName()
		{
			return lafInfo.getName();
		}
	}

	private static final class SystemDefaultLookAndFeelEntry extends LookAndFeelEntry
	{
		@Override
		public String getClassName()
		{
			return UIManager.getSystemLookAndFeelClassName();
		}

		@Override
		public String getName()
		{
			return SYSTEM_DEFAULT_NAME;
		}
	}
}
