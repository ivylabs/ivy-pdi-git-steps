package com.ivyis.di.trans.steps.git.command;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.transport.CredentialsProvider;

import com.ivyis.di.trans.steps.git.operations.GitOperationsStep;

/**
 * Interface that describes the methods of Git operations commands.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public interface GitOperationsCommand extends GitCommand {

  Git call(final GitOperationsStep gitOperationsStep, Git git,
      CredentialsProvider cp, String gitRepoUrl, File gitRepoFolderPath)
      throws IllegalArgumentException, IOException,
      NoFilepatternException, GitAPIException;
}
