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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.apache.tools.tar.*;

public class TarGetter {
	String saveDir = "lmntal/";

	public void download() throws IOException{
		URL url = new URL("http://www.lsv.ens-cachan.fr/~gastin/ltl2ba/ltl2ba-1.1.tar.gz");
		InputStream in = url.openConnection().getInputStream();
		FileOutputStream out = new FileOutputStream(new File(saveDir+"ltl2ba-1.1.tar.gz"));
		int size;
		byte bytes[] = new byte[1024];
		while((size = in.read(bytes)) >= 0) {
			out.write(bytes,0,size);
		}
		out.close();
		in.close();
	}

	public void unTar() throws IOException{
		TarInputStream tar = new TarInputStream(new GZIPInputStream(new FileInputStream(new File(saveDir+"ltl2ba-1.1.tar.gz"))));
		TarEntry tarEnt = tar.getNextEntry();
		while (tarEnt != null) {

			String name = tarEnt.getName();

			if(name.endsWith("/")){
				(new File(saveDir+name)).mkdir();
			}else{
				ByteArrayOutputStream bos = new ByteArrayOutputStream((int)tarEnt.getSize());
				tar.copyEntryContents(bos);
				InputStream in = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
				FileOutputStream out = new FileOutputStream(saveDir+name);
				int size;
				byte bytes[] = new byte[1024];
				while((size = in.read(bytes)) >= 0) {
					out.write(bytes,0,size);
				}
				out.close();
				in.close();
			}
			tarEnt = tar.getNextEntry();
		}
		tar.close();
	}

}
