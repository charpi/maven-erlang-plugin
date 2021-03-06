package eu.lindenbaum.maven.erlang;

import java.io.File;

import eu.lindenbaum.maven.util.ErlUtils;

import com.ericsson.otp.erlang.OtpErlangObject;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * A {@link Script} that creates an initial RELEASES file from a specific
 * release file.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public class CreateRELEASESScript extends AbstractScript<String> {
  private final File rootDir;
  private final File relFile;

  public CreateRELEASESScript(File rootDir, File relFile) throws MojoExecutionException {
    super();
    this.rootDir = rootDir;
    this.relFile = relFile;
  }

  @Override
  public String get() {
    String rootDirectory = this.rootDir.getAbsolutePath();
    String relDirectory = this.relFile.getParentFile().getAbsolutePath();
    String relFile = this.relFile.getAbsolutePath();
    return String.format(this.script, rootDirectory, relDirectory, relFile);
  }

  /**
   * Converts the result of the {@link Script} execution into an error message.
   * 
   * @return "ok" in case of success, a failure message in case of errors
   */
  @Override
  public String handle(OtpErlangObject result) {
    return ErlUtils.toString(result);
  }
}
