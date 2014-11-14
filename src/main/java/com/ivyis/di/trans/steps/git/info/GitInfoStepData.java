package com.ivyis.di.trans.steps.git.info;

import java.io.File;
import java.util.List;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.ivyis.di.trans.steps.git.command.GitCommand;

/**
 * This class contains the methods to set and retrieve the status of the step data.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class GitInfoStepData extends BaseStepData implements StepDataInterface {
  RowMetaInterface outputRowMeta = null;
  RowMetaInterface insertRowMeta = null;
  int fieldnr = 0;
  int nrPrevFields = 0;

  CredentialsProvider cp;
  String gitRepoUrl = null;
  File gitRepoFolderPath = null;
  GitCommand gitCommand = null;
  int rowNumber = 0;
  List<String[]> result = null;
}
