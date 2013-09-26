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

package lavit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Lang {

	static final String LANG_DIR = "lang";
	static final String LANG_EXT = ".xml";
	public static String[] m = new String[40];
	public static String[] d = new String[30];
	public static String[] f = new String[30];
	public static String[] w = new String[30];

	public static void set(String lang){
		Properties prop = new Properties();
		String fn = LANG_DIR + "/" + lang + LANG_EXT;
		try {
			InputStream in = Env.getInputStreamOfFile(fn);
			prop.loadFromXML(in);
            in.close();
        } catch (IOException e) {
            System.err.println("check "+fn);
            System.exit(0);
        }
        set(prop);
	}

	public static void set(Properties prop){
		for(int i=0;i<m.length;++i){
			String str = prop.getProperty("m"+i);
			if(str!=null){ m[i] = str; }
		}
		for(int i=0;i<d.length;++i){
			String str = prop.getProperty("d"+i);
			if(str!=null){ d[i] = str; }
		}
		for(int i=0;i<f.length;++i){
			String str = prop.getProperty("f"+i);
			if(str!=null){ f[i] = str; }
		}
		for(int i=0;i<w.length;++i){
			String str = prop.getProperty("w"+i);
			if(str!=null){ w[i] = str; }
		}
	}

}
