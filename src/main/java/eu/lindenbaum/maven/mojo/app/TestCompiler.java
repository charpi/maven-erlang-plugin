package eu.lindenbaum.maven.mojo.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.lindenbaum.maven.ErlangMojo;
import eu.lindenbaum.maven.Properties;
import eu.lindenbaum.maven.erlang.BeamCompilerScript;
import eu.lindenbaum.maven.erlang.CompilerResult;
import eu.lindenbaum.maven.erlang.MavenSelf;
import eu.lindenbaum.maven.erlang.Script;
import eu.lindenbaum.maven.util.ErlConstants;
import eu.lindenbaum.maven.util.FileUtils;
import eu.lindenbaum.maven.util.MavenUtils;
import eu.lindenbaum.maven.util.MojoUtils;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Compile erlang test sources and recompile erlang sources with the options
 * {@code debug_info}, {@code export_all} and <code>{d, 'TEST'}</code>. This
 * will also compile the supporting erlang sources provided along with the
 * plugin.
 * 
 * @goal test-compile
 * @phase test-compile
 * @author Olivier Sambourg
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
 */
public final class TestCompiler extends ErlangMojo {
  /**
   * Setting this to {@code true} will skip the test compilation.
   * 
   * @parameter expression="${skipTests}" default-value=false
   */
  private boolean skipTests;

  /**
   * Additional compiler options (comma separated) for test compilation that are
   * directly passed to <code>compile:file/2</code>, e.g. <code>"{d, Macro},
   * nowarn_unused_function"</code>. Note: The user may not specifiy one of the
   * {@code report} options since the {@link Mojo} itself uses the
   * {@code return} option internally. Warnings and Errors will be printed
   * without specifying extra options.
   * 
   * @parameter expression="${testCompilerOptions}"
   */
  private String testCompilerOptions;

  @Override
  protected void execute(Log log, Properties p) throws MojoExecutionException, MojoFailureException {
    log.info(MavenUtils.SEPARATOR);
    log.info(" T E S T - C O M P I L E R");
    log.info(MavenUtils.SEPARATOR);

    if (this.skipTests) {
      log.info("Test compilation is skipped.");
      return;
    }

    FileUtils.ensureDirectories(p.targetTestEbin());
    int removed = FileUtils.removeFilesRecursive(p.targetTestEbin(), ErlConstants.BEAM_SUFFIX);
    log.debug("Removed " + removed + " stale " + ErlConstants.BEAM_SUFFIX + "-files from "
              + p.targetTestEbin());

    log.debug("Looking up test sources under " + p.test_src() + " with file suffix "
              + ErlConstants.ERL_SUFFIX);
    List<File> files = FileUtils.getFilesRecursive(p.test_src(), ErlConstants.ERL_SUFFIX);
    if (!files.isEmpty()) {
      File plugin = MavenUtils.getPluginFile("maven-erlang-plugin", p.project(), p.repository());
      FileUtils.extractFilesFromJar(plugin, ErlConstants.ERL_SUFFIX, p.targetTestEbin());

      files.addAll(FileUtils.getFilesRecursive(p.src(), ErlConstants.ERL_SUFFIX));

      List<File> supportFiles = getTestSupportFiles(p);
      files.addAll(supportFiles);

      List<String> options = new ArrayList<String>();
      options.add("debug_info");
      options.add("export_all");
      options.add("{d, 'TEST'}");
      if (this.testCompilerOptions != null && !this.testCompilerOptions.isEmpty()) {
        log.info("Using additinal test compiler options: " + this.testCompilerOptions);
        options.add(this.testCompilerOptions);
      }

      List<File> includes = MojoUtils.getTestIncludeDirectories(p);
      Script<CompilerResult> script = new BeamCompilerScript(files, p.targetTestEbin(), includes, options);
      CompilerResult result = MavenSelf.get(p.testCookie()).exec(p.testNode(), script);
      result.logOutput(log);
      String failedCompilationUnit = result.getFailed();
      if (failedCompilationUnit != null) {
        throw new MojoFailureException("Failed to compile " + failedCompilationUnit + ".");
      }

      int numberOfTestFiles = files.size() - supportFiles.size();
      log.info("Successfully compiled " + numberOfTestFiles + " test source file(s).");
    }
    else {
      log.info("No test source files to compile.");
    }
  }

  private static List<File> getTestSupportFiles(Properties p) {
    List<File> supportFiles = new ArrayList<File>();
    supportFiles.add(new File(p.targetTestEbin(), "surefire.erl"));
    supportFiles.add(new File(p.targetTestEbin(), "cover2.erl"));
    supportFiles.add(new File(p.targetTestEbin(), "ttycapture.erl"));
    return supportFiles;
  }
}
