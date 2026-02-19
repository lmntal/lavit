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

import java.awt.RenderingHints;

public enum TextAntialias
{
	DEFAULT("Default", RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT),
	ON("On", RenderingHints.VALUE_TEXT_ANTIALIAS_ON),
	OFF("Off", RenderingHints.VALUE_TEXT_ANTIALIAS_OFF),
	LCD_H_RGB("LCD-H-RGB", RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB),
	LCD_H_BGR("LCD-H-BGR", RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR),
	LCD_V_RGB("LCD-V-RGB", RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB),
	LCD_V_BGR("LCD-V-BGR", RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR),
	GASP("Gasp", RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

	public final String name;
	public final Object value;

	private TextAntialias(String name, Object value)
	{
		this.name = name;
		this.value = value;
	}

	public String toString()
	{
		return name;
	}

	public static TextAntialias of(String name)
	{
		for (TextAntialias e : values())
		{
			if (name.equals(e.name))
			{
				return e;
			}
		}
		return null;
	}
}
