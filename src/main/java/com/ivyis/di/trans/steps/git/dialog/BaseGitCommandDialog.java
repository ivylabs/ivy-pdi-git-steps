package com.ivyis.di.trans.steps.git.dialog;

import com.ivyis.di.trans.steps.git.command.GitCommand;

/**
 * Interface thats describes the methods of Git command for the UI in Spoon.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public interface BaseGitCommandDialog {
  public static final boolean NO_CONFIGURATION = false;

  public abstract GitCommand open();

}
