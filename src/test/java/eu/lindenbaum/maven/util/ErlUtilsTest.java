package eu.lindenbaum.maven.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

public class ErlUtilsTest {
  private static final Log log = new SystemStreamLog();

  @Test
  public void testExec() throws Exception {
    List<String> command = Arrays.asList(new String[]{ "dir" });
    assertEquals("ok", ErlUtils.exec(command, log, null, new ProcessListener() {
      @Override
      public String processCompleted(int exitValue, List<String> processOutput) throws MojoExecutionException {
        assertEquals(exitValue, 0);
        assertFalse(processOutput.isEmpty());
        return "ok";
      }
    }));
  }

  @Test
  public void testEval2() throws Exception {
    assertEquals("ok", ErlUtils.eval(log, "io:format(\"ok~n\")"));
  }

  @Test
  public void testEval3() throws Exception {
    assertEquals("ok", ErlUtils.eval(log, "io:format(\"ok~n\")", null));
  }

  @Test
  public void testEval4() throws Exception {
    assertEquals("ok", ErlUtils.eval(log, "io:format(\"ok~n\")", null, null));
  }
}
