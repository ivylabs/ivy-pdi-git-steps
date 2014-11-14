package com.ivyis.di.trans.steps.git.info;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.ivyis.di.trans.steps.git.command.GitInfoCommand;

/**
 * This class is responsible to processing the data rows.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class GitInfoStep extends BaseStep implements StepInterface {

  private GitInfoStepMeta meta;
  private GitInfoStepData data;

  public GitInfoStep(StepMeta s, StepDataInterface stepDataInterface, int c,
      TransMeta t, Trans dis) {
    super(s, stepDataInterface, c, t, dis);
  }

  /**
   * {@inheritDoc}
   * 
   * @throws KettleException
   */
  @Override
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
      throws KettleException {
    this.meta = (GitInfoStepMeta) smi;
    this.data = (GitInfoStepData) sdi;

    if (first) {
      first = false;
      data.outputRowMeta = new RowMeta();
      data.nrPrevFields = data.outputRowMeta.size();
      meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

      try {
        final Repository repository = FileRepositoryBuilder
            .create(new File(data.gitRepoFolderPath, ".git"));
        final Git git = new Git(repository);
        logBasic("Starting, command "
            + data.gitCommand.getCommandType());
        data.result = ((GitInfoCommand) data.gitCommand).call(this,
            git, data.cp, data.gitRepoUrl, data.gitRepoFolderPath);
        git.close();
        logBasic("Finished, command "
            + data.gitCommand.getCommandType());
      } catch (InvalidRemoteException e) {
        throw new KettleException(e);
      } catch (TransportException e) {
        throw new KettleException(e);
      } catch (GitAPIException e) {
        throw new KettleException(e);
      } catch (IllegalArgumentException e) {
        throw new KettleException(e);
      } catch (IOException e) {
        throw new KettleException(e);
      }
      cachePosition();
    } // end if first

    if (data.rowNumber >= data.result.size()) {
      setOutputDone(); // signal end to receiver(s)
      logDebug("Finished, processing all rows of git command "
          + data.gitCommand.getCommandType());
      return false; // end of data or error.
    }

    final String[] row = data.result.get(data.rowNumber++);
    final Object[] outputRowData = RowDataUtil.allocateRowData(row.length);
    for (int i = 0; i < row.length; i++) {
      outputRowData[i] = row[i];
    }
    putRow(data.outputRowMeta, outputRowData);

    if (checkFeedback(getLinesRead())) {
      if (log.isBasic()) {
        logBasic("Linenr " + getLinesRead()); // Some basic logging
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (GitInfoStepMeta) smi;
    data = (GitInfoStepData) sdi;

    if (super.init(smi, sdi)) {
      if (meta.getUsername() != null && meta.getPassword() != null) {
        data.cp = new UsernamePasswordCredentialsProvider(
            environmentSubstitute(meta.getUsername()),
            environmentSubstitute(meta.getPassword()));
      }
      if (meta.getGitRepoUrl() != null) {
        data.gitRepoUrl = environmentSubstitute(meta.getGitRepoUrl());
      }
      if (meta.getGitRepoFolderPath() != null) {
        try {
          data.gitRepoFolderPath = new File(
              KettleVFS.getFilename(KettleVFS
                  .getFileObject(environmentSubstitute(meta
                      .getGitRepoFolderPath()))));
        } catch (KettleFileException e) {
          logError(e.getMessage(), e);
        }
      }
      if (meta.getGitCommand() != null) {
        data.gitCommand = meta.getGitCommand();
      }
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
    super.dispose(smi, sdi);
  }

  /**
   * Run is were the action happens.
   */
  public void run() {
    logBasic("Starting to run...");
    try {
      while (processRow(meta, data) && !isStopped()) {
        continue;
      }
    } catch (Exception e) {
      logError("Unexpected error : " + e.toString());
      logError(Const.getStackTracker(e));
      setErrors(1);
      stopAll();
    } finally {
      dispose(meta, data);
      logBasic("Finished, processing " + getLinesRead() + " rows");
      markStop();
    }
  }

  /**
   * Checks the fields positions.
   * 
   * @throws KettleStepException the kettle step exception.
   */
  private void cachePosition() throws KettleStepException {

  }
}
