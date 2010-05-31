package lavit.runner;

import java.io.*;

import lavit.FrontEnd;

public class ErrorStreamPrinter extends Thread {
	InputStream is;

	ErrorStreamPrinter(InputStream is){
		this.is = is;
	}

	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			try {
				String line;
				while ((line = br.readLine()) != null){
					FrontEnd.mainFrame.toolTab.systemPanel.outputPanel.errPrintln(line);
				}
			} finally {
				is.close();
			}
		} catch (Exception e) {
			FrontEnd.printException(e);
		}
	}
}
