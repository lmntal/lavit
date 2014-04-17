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

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class Option<T> implements Iterable<T>
{
	public abstract <TRet> TRet accept(IVisitor<T, TRet> visitor);

	public static <T> Option<T> none()
	{
		return new None<T>();
	}

	public static <T> Option<T> some(T x)
	{
		return new Some<T>(x);
	}

	public interface IVisitor<T, TRet>
	{
		public TRet visitNone();
		public TRet visitSome(T item);
	}

	private static class None<T> extends Option<T>
	{
		public Iterator<T> iterator()
		{
			return new NullIterator<T>();
		}

		public <TRet> TRet accept(IVisitor<T, TRet> visitor)
		{
			return visitor.visitNone();
		}

		public String toString()
		{
			return "None";
		}
	}

	private static class Some<T> extends Option<T>
	{
		private final T x;

		public Some(T x)
		{
			this.x = x;
		}

		public Iterator<T> iterator()
		{
			return new UnitIterator<T>(x);
		}

		public <TRet> TRet accept(IVisitor<T, TRet> visitor)
		{
			return visitor.visitSome(x);
		}

		public String toString()
		{
			return "Some[" + x + "]";
		}
	}

	private static class NullIterator<T> implements Iterator<T>
	{
		public boolean hasNext()
		{
			return false;
		}

		public T next()
		{
			throw new NoSuchElementException();
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	private static class UnitIterator<T> implements Iterator<T>
	{
		private boolean hasNext = true;
		private T x;

		public UnitIterator(T x)
		{
			this.x = x;
		}

		public boolean hasNext()
		{
			return hasNext;
		}

		public T next()
		{
			if (hasNext)
			{
				hasNext = false;
				return x;
			}
			throw new NoSuchElementException();
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
