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

public abstract class CharCondition
{
	private static final CharCondition ANY = new Any();
	private static final CharCondition UPPER_ALPHA = range('A', 'Z');
	private static final CharCondition LOWER_ALPHA = range('a', 'z');
	private static final CharCondition DIGIT = range('0', '9');

	public abstract boolean test(char c);

	public CharCondition negate()
	{
		return new Negate(this);
	}

	public CharCondition union(CharCondition cond)
	{
		return new Union(this, cond);
	}

	public CharCondition intersect(CharCondition cond)
	{
		return new Intersect(this, cond);
	}

	public static CharCondition any()
	{
		return ANY;
	}

	public static CharCondition just(char c)
	{
		return new Single(c);
	}

	public static CharCondition range(char begin, char end)
	{
		return new Range(begin, end);
	}

	public static CharCondition upperAlpha()
	{
		return UPPER_ALPHA;
	}

	public static CharCondition lowerAlpha()
	{
		return LOWER_ALPHA;
	}

	public static CharCondition alpha()
	{
		return upperAlpha().union(lowerAlpha());
	}

	public static CharCondition digit()
	{
		return DIGIT;
	}

	public static CharCondition alphaOrDigit()
	{
		return alpha().union(digit());
	}

	private static class Any extends CharCondition
	{
		public boolean test(char c)
		{
			return true;
		}
	}

	private static class Single extends CharCondition
	{
		private char c;

		public Single(char c)
		{
			this.c = c;
		}

		public boolean test(char c)
		{
			return this.c == c;
		}
	}

	private static class Range extends CharCondition
	{
		private char begin;
		private char end;

		public Range(char begin, char end)
		{
			this.begin = begin;
			this.end = end;
		}

		public boolean test(char c)
		{
			return begin <= c && c <= end;
		}
	}

	private static class Negate extends CharCondition
	{
		private CharCondition cond;

		public Negate(CharCondition cond)
		{
			this.cond = cond;
		}

		public boolean test(char c)
		{
			return !cond.test(c);
		}
	}

	private static class Union extends CharCondition
	{
		private CharCondition cond1;
		private CharCondition cond2;

		public Union(CharCondition cond1, CharCondition cond2)
		{
			this.cond1 = cond1;
			this.cond2 = cond2;
		}

		public boolean test(char c)
		{
			return cond1.test(c) || cond2.test(c);
		}
	}

	private static class Intersect extends CharCondition
	{
		private CharCondition cond1;
		private CharCondition cond2;

		public Intersect(CharCondition cond1, CharCondition cond2)
		{
			this.cond1 = cond1;
			this.cond2 = cond2;
		}

		public boolean test(char c)
		{
			return cond1.test(c) && cond2.test(c);
		}
	}
}
