package lavit.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import lavit.Env;
import lavit.FrontEnd;
import lavit.util.OuterRunner;

public class PrismRunner implements OuterRunner {
  private ThreadRunner runner;
  private StringBuffer buffer;
  private String targetBasePath;

  private File targetTransitionFile;
  private File targetPredicatesFile;
  private File targetLabelsFile;
  private File targetStateRewardsFile;

  private boolean success;

  public static final String PRISM_OPTIONS = "PRISM_OPTIONS";

  public PrismRunner(File targetTransitionFile) {
    this.targetTransitionFile = targetTransitionFile;
    if (targetTransitionFile == null) {
      throw new IllegalArgumentException("Target transition file cannot be null.");
    }
    this.targetBasePath = getBasePath(targetTransitionFile);
    if (this.targetBasePath == null) {
      throw new IllegalArgumentException("Target base path cannot be determined from the transition file.");
    }
    setInputFiles(this.targetBasePath);
    this.runner = new ThreadRunner();
    this.buffer = new StringBuffer();
  }

  private void setInputFiles(String targetBasePath) {
    this.targetPredicatesFile = new File(targetBasePath + ".pctl");
    this.targetLabelsFile = new File(targetBasePath + ".lab");
    this.targetStateRewardsFile = new File(targetBasePath + ".srew");
  }

  public void run() {
    runner.start();
  }

  public String getOutput() {
    return buffer.toString();
  }

  public boolean isRunning() {
    if (runner == null) 
      return false;
    return true;
  }

  public void exit() {
    runner = null;
  }

  public boolean isSucceeded() {
    return success;
  }

  public void kill() {
    if (runner != null) {
      runner.interrupt();
      runner = null;
    }
  }

  private String getBasePath(File file) {
    if (file == null) {
      return null;
    }
    String filePath = file.getAbsolutePath();
    int lastDotIndex = filePath.lastIndexOf('.');
    if (lastDotIndex > 0) {
      return filePath.substring(0, lastDotIndex);
    } else {
      return filePath; // No extension found
    }
  }

  private class ThreadRunner extends Thread {
    private Process p;

    public void run() {
      try {
        // TODO: PRISM_EXE_PATH を GUI から設定できるようにする．
        String prismExePath = Env.get("PRISM_EXE_PATH");

        if (prismExePath == null || prismExePath.trim().isEmpty()) {
          throw new IllegalStateException("PRISM executable path is not set.");
        }

        String targetBasePath = getBasePath(targetTransitionFile);

        if (targetBasePath == null) {
          throw new IllegalArgumentException("Target base path cannot be determined from the transition file.");
        }

        if (targetPredicatesFile != null && !targetPredicatesFile.exists()) {
          throw new IllegalArgumentException("Target predicates file does not exist: " + targetPredicatesFile.getAbsolutePath());
        }

        ArrayList<String> command = new ArrayList<>();
        String importModelString = targetTransitionFile.getAbsolutePath();

        if (targetLabelsFile != null && targetLabelsFile.exists()) {
          importModelString += ",lab";
        }
        if (targetStateRewardsFile != null && targetStateRewardsFile.exists()) {
          importModelString += ",srew";
        }

        command.add(prismExePath);
        if( !Env.get(PRISM_OPTIONS, "").isEmpty()) {
          command.addAll(Env.getList(PRISM_OPTIONS));
        }
        command.add("-importmodel");
        command.add(importModelString);
        command.add(targetPredicatesFile.getAbsolutePath());

        FrontEnd.mainFrame.toolTab.systemPanel.outputPanel.printTitle("> " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        Env.setProcessEnvironment(pb.environment());
        pb.redirectErrorStream(true);
        p = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String str;
        while ((str = in.readLine()) != null) {
          buffer.append(str + "\n");
          FrontEnd.mainFrame.toolTab.systemPanel.outputPanel.println(str);
        }

        in.close();
        p.waitFor();

        success = true;

      } catch (Exception e) {

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        buffer.append(sw.toString());

      } finally {
        exit();
      }
    }
  }
}
