package com.ivyis.di.trans.steps.git.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.CredentialsProvider;

import com.ivyis.di.trans.steps.git.info.GitInfoStep;

/**
 * Interface responsible for Git commands that extract information from the repository.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public interface GitInfoCommand extends GitCommand {

  List<String[]> call(final GitInfoStep gitInfoStep, Git git,
      CredentialsProvider cp, String gitRepoUrl, File gitRepoFolder)
      throws InvalidRemoteException, TransportException, GitAPIException,
      IllegalArgumentException, IOException;

  Map<String, String> getConfigFields(boolean mainConfigFields);

  boolean isConfigurable();
}
