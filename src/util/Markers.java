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

package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Markers
{
	private static class Occurrence
	{
		public final int position;
		public final String text;

		public Occurrence(int position, String text)
		{
			this.position = position;
			this.text = text;
		}
	}

	private List<Occurrence> occurrences = new ArrayList<Occurrence>();
	private Map<String, List<Integer>> positions = new HashMap<String, List<Integer>>();

	public synchronized void clear()
	{
		occurrences.clear();
		positions.clear();
	}

	public synchronized void addMarker(int position, String text)
	{
		occurrences.add(new Occurrence(position, text));
		List<Integer> list = positions.get(text);
		if (list == null)
		{
			positions.put(text, list = new ArrayList<Integer>());
		}
		list.add(position);
	}

	public synchronized Iterable<String> getMarkedWord(int position)
	{
		List<String> ret = new ArrayList<String>();
		for (Occurrence oc : occurrences)
		{
			if (oc.position <= position && position <= oc.position + oc.text.length())
			{
				ret.add(oc.text);
				break;
			}
		}
		return ret;
	}

	public synchronized List<Integer> getPositions(String text)
	{
		List<Integer> list = positions.get(text);
		if (list == null)
		{
			list = Collections.emptyList();
		}
		return Collections.unmodifiableList(list);
	}

	public static Markers create()
	{
		return new Markers();
	}
}
