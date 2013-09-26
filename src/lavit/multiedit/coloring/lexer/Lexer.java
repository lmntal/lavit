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

package lavit.multiedit.coloring.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import lavit.Env;
import lavit.util.TrieTreeMatcher;

public class Lexer
{
	private static final int STATE_LMNTAL = 0;
	private static final int STATE_JAVA   = 1;

	private static final TrieTreeMatcher lmnKwds = new TrieTreeMatcher();
	private static final TrieTreeMatcher javaKwds = new TrieTreeMatcher();

	static
	{
		if (Env.isSet("editor.highlight"))
		{
			for (String s : Env.get("editor.highlight").split("\\s+"))
			{
				lmnKwds.add(s);
			}
		}

		javaKwds.add("abstract");
		javaKwds.add("assert");
		javaKwds.add("boolean");
		javaKwds.add("break");
		javaKwds.add("byte");
		javaKwds.add("case");
		javaKwds.add("catch");
		javaKwds.add("char");
		javaKwds.add("class");
		javaKwds.add("const");
		javaKwds.add("continue");
		javaKwds.add("default");
		javaKwds.add("do");
		javaKwds.add("double");
		javaKwds.add("else");
		javaKwds.add("enum");
		javaKwds.add("extends");
		javaKwds.add("false");
		javaKwds.add("final");
		javaKwds.add("finally");
		javaKwds.add("float");
		javaKwds.add("for");
		javaKwds.add("goto");
		javaKwds.add("if");
		javaKwds.add("implements");
		javaKwds.add("import");
		javaKwds.add("instanceof");
		javaKwds.add("int");
		javaKwds.add("long");
		javaKwds.add("me");
		javaKwds.add("mem");
		javaKwds.add("native");
		javaKwds.add("new");
		javaKwds.add("null");
		javaKwds.add("package");
		javaKwds.add("private");
		javaKwds.add("protected");
		javaKwds.add("public");
		javaKwds.add("return");
		javaKwds.add("short");
		javaKwds.add("static");
		javaKwds.add("strictfp");
		javaKwds.add("super");
		javaKwds.add("switch");
		javaKwds.add("synchronized");
		javaKwds.add("this");
		javaKwds.add("throw");
		javaKwds.add("throws");
		javaKwds.add("transient");
		javaKwds.add("true");
		javaKwds.add("try");
		javaKwds.add("void");
		javaKwds.add("volatile");
		javaKwds.add("while");
	}

	private TreeSet<ColorLabel> _labels = new TreeSet<ColorLabel>();
	private List<Integer> _tabs = new ArrayList<Integer>();
	private char[] _cs;
	private int _column;
	private int _state;
	private int _flags;

	public Lexer(String text, int flags)
	{
		_cs = text.toCharArray();
		_column = 0;
		_state = STATE_LMNTAL;
		_flags = flags;
	}

	public List<Integer> getTabs()
	{
		return _tabs;
	}

	public TreeSet<ColorLabel> lex()
	{
		_labels.clear();
		_state = STATE_LMNTAL;

		while (!end())
		{
			char c = peek();

			if (c == '\t')
				_tabs.add(_column);

			if (c == '/')
			{
				int start = _column;
				succ();
				c = peek();
				if (c == '/')
				{
					succ();
					lex_linecomment(start);
				}
				else if (c == '*')
				{
					succ();
					lex_blockcomment(start);
				}
			}
			else if (c == '\'' || c == '"')
			{
				char q = c;
				int start = _column;
				succ();
				lex_quote(q, start);
			}
			else if (c == '%')
			{
				int start = _column;
				succ();
				lex_linecomment(start);
			}
			else if (c == ':')
			{
				int start = _column;
				succ();
				c = peek();
				if (_state == STATE_LMNTAL && c == '-')
				{
					succ();
					addItem(start, _column - start, TokenLabel.OPERATOR);
				}
				else if (c == ']')
				{
					succ();
					_state = STATE_LMNTAL;
				}
			}
			else if (c == '[')
			{
				succ();
				c = peek();
				if (c == ':')
				{
					succ();
					_state = STATE_JAVA;
				}
			}
			else if (Character.isLetter(c) || c == '_')
			{
				lex_word();
			}
			else
			{
				succ();
			}
		}
		return _labels;
	}

	private void lex_linecomment(int start)
	{
		while (!end() && peek() != '\n')
		{
			succ();
		}
		addItem(start, _column - start, TokenLabel.COMMENT);
	}

	private void lex_blockcomment(int start)
	{
		int s = 0;
		while (!end() && s != 2)
		{
			switch (s)
			{
			case 0:
				if (peek() == '*')
				{
					s = 1;
				}
				succ();
				break;
			case 1:
				if (peek() == '/')
				{
					s = 2;
					succ();
				}
				else
				{
					s = 0;
				}
				break;
			}
		}
		addItem(start, _column - start, TokenLabel.COMMENT);
	}

	private void lex_quote(char q, int start)
	{
		boolean escaped = false;
		while (!end() && peek() != q || escaped)
		{
			if (!escaped && peek() == '\\')
				escaped = true;
			else
				escaped = false;

			succ();
		}
		succ();
		addItem(start, _column - start, TokenLabel.STRING);
	}

	private void lex_word()
	{
		TrieTreeMatcher m = null;

		switch (_state)
		{
		case STATE_LMNTAL:
			m = lmnKwds;
			break;
		case STATE_JAVA:
			m = javaKwds;
			break;
		}

		int start = _column;
		char c = peek();

		m.reset();
		m.transition(c);

		succ();
		c = peek();
		while (!end() && (Character.isLetterOrDigit(c) || c == '_'))
		{
			m.transition(c);
			succ();
			c = peek();
		}
		if (m.accepts())
		{
			addItem(start, _column - start, TokenLabel.KEYWORD);
		}
	}

	private void addItem(int start, int length, int label)
	{
		if ((_flags & label) != 0)
		{
			_labels.add(new ColorLabel(start, length, label));
		}
	}

	private boolean end()
	{
		return _column >= _cs.length;
	}

	private char peek()
	{
		return !end() ? _cs[_column] : '\0';
	}

	private void succ()
	{
		if (!end())
			_column++;
	}
}
