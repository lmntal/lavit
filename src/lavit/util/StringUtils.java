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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class StringUtils
{
	private StringUtils() { }

	public static boolean nullOrEmpty(String s)
	{
		return s == null || s.isEmpty();
	}

	public static <T> String join(Collection<T> items, String delim)
	{
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (T item : items)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				buf.append(delim);
			}
			buf.append(item);
		}
		return buf.toString();
	}

	public static Set<String> splitToSet(String s, String regex)
	{
		Set<String> set = new LinkedHashSet<String>();
		for (String item : s.split(regex))
		{
			set.add(item);
		}
		return set;
	}

	public static List<String> splitToList(String s, String regex)
	{
		return new ArrayList<String>(Arrays.asList(s.split(regex)));
	}

	public static String convertLineDelimiter(String text, LineDelimiter mode)
	{
		StringBuilder buf = new StringBuilder(text.length());
		String delim = getLineDelimiter(mode);
		boolean cr = false;
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == '\r')
			{
				if (cr) buf.append(delim);
				cr = true;
			}
			else if (c == '\n')
			{
				buf.append(delim);
				cr = false;
			}
			else
			{
				if (cr)
				{
					buf.append(delim);
					cr = false;
				}
				buf.append(c);
			}
		}
		return buf.toString();
	}

	private static String getLineDelimiter(LineDelimiter mode)
	{
		switch (mode)
		{
		case CR:
			return "\r";
		case LF:
			return "\n";
		}
		return "\r\n";
	}
}
