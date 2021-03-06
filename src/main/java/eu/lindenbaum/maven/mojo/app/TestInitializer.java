package eu.lindenbaum.maven.mojo.app;

import java.io.File;

import eu.lindenbaum.maven.ErlangMojo;
import eu.lindenbaum.maven.Properties;
import eu.lindenbaum.maven.util.FileUtils;
import eu.lindenbaum.maven.util.MojoUtils;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * {@link Mojo} that starts the test erlang node used as a backend for rpcs made
 * by the plugin. The node will only be started if it is not already running.
 * The node will be shutdown when the executing JVM exits. This is done by a
 * {@link Runtime#addShutdownHook(Thread)} which will only be added <b>once</b>
 * each JVM execution.
 * 
 * @goal test-initialize
 * @phase generate-test-sources
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public class TestInitializer extends ErlangMojo {
  /**
   * Setting this to {@code true} will skip the test initialization.
   * 
   * @parameter expression="${skipTests}" default-value=false
   */
  private boolean skipTests;

  @Override
  protected void execute(Log log, Properties p) throws MojoExecutionException {
    if (this.skipTests) {
      return;
    }
    File buildDir = p.targetLayout().base();
    FileUtils.ensureDirectories(buildDir);
    File backendLog = p.targetLayout().backendLog();
    MojoUtils.startBackend(log, p.erlCommand(), p.testNode(), p.cookie(), buildDir, backendLog);
  }
}
