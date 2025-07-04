package lavit.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import lavit.Env;
import lavit.util.OuterRunner;

public class ProbabilisticTranslatorRunner implements OuterRunner {
  private ThreadRunner runner;
  private StringBuffer buffer;

  private File targetSlimDumpFile;
  private File targetWeightsFile;
  private boolean success;

  public ProbabilisticTranslatorRunner(File targetSlimDumpFile, File targetWeightsFile) {
    this.targetSlimDumpFile = targetSlimDumpFile;
    this.targetWeightsFile = targetWeightsFile;
    if (targetSlimDumpFile == null || targetWeightsFile == null) {
      throw new IllegalArgumentException("Target files cannot be null.");
    }
    this.runner = new ThreadRunner();
    this.buffer = new StringBuffer();
    this.success = false;
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

  private class ThreadRunner extends Thread {
    private Process p;

    public void run() {
      try {
        // TODO: PROB_TRANSLATOR_EXE_PATH を GUI から設定できるようにする．
        String translatorPath = Env.get("PROB_TRANSLATOR_EXE_PATH");

        if (translatorPath == null || translatorPath.trim().isEmpty()) {
          throw new IllegalStateException("Probabilistic translator executable path is not set.");
        }
        
        String outputFileName = targetSlimDumpFile.getAbsolutePath().replace("_slim.txt", "_dtmc.tra");

        ArrayList<String> command = new ArrayList<>();
        command.add(translatorPath);
        command.add(targetSlimDumpFile.getAbsolutePath());
        command.add("--model_type");
        command.add("dtmc");
        command.add("--weight");
        command.add(targetWeightsFile.getAbsolutePath());
        command.add("--output");
        command.add(outputFileName);

        ProcessBuilder pb = new ProcessBuilder(command);
        Env.setProcessEnvironment(pb.environment());
        pb.redirectErrorStream(true);
        p = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String str;
        while ((str = in.readLine()) != null) {
          buffer.append(str + "\n");
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
