package com.ivyis.di.trans.steps.git.dialog;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.trans.TransMeta;

import com.ivyis.di.trans.steps.git.GitCommandType;
import com.ivyis.di.trans.steps.git.command.GitCommand;
import com.ivyis.di.trans.steps.git.info.dialog.impl.ListBranchsGitCommandDialog;
import com.ivyis.di.trans.steps.git.info.dialog.impl.LogGitCommandDialog;
import com.ivyis.di.trans.steps.git.operations.dialog.impl.AddGitCommandDialog;
import com.ivyis.di.trans.steps.git.operations.dialog.impl.CheckoutGitCommandDialog;
import com.ivyis.di.trans.steps.git.operations.dialog.impl.CloneGitCommandDialog;
import com.ivyis.di.trans.steps.git.operations.dialog.impl.CommitGitCommandDialog;
import com.ivyis.di.trans.steps.git.operations.dialog.impl.InitGitCommandDialog;
import com.ivyis.di.trans.steps.git.operations.dialog.impl.PullGitCommandDialog;
import com.ivyis.di.trans.steps.git.operations.dialog.impl.PushGitCommandDialog;
import com.ivyis.di.trans.steps.git.operations.dialog.impl.TagGitCommandDialog;

/**
 * The Git commands factory for UI.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class GitCommandDialogFactory {

  public static BaseGitCommandDialog getGitCommandDialog(Shell parent,
      TransMeta transMeta, GitCommand gitCommand,
      GitCommandType gitCommandType) {
    switch (gitCommandType) {
      case LIST_BRANCHS:
        return new ListBranchsGitCommandDialog(parent, transMeta,
            gitCommand);
      case LOG:
        return new LogGitCommandDialog(parent, transMeta, gitCommand);
      case CLONE:
        return new CloneGitCommandDialog(parent, transMeta, gitCommand);
      case ADD:
        return new AddGitCommandDialog(parent, transMeta, gitCommand);
      case INIT:
        return new InitGitCommandDialog(parent, transMeta, gitCommand);
      case PULL:
        return new PullGitCommandDialog(parent, transMeta, gitCommand);
      case COMMIT:
        return new CommitGitCommandDialog(parent, transMeta, gitCommand);
      case PUSH:
        return new PushGitCommandDialog(parent, transMeta, gitCommand);
      case TAG:
        return new TagGitCommandDialog(parent, transMeta, gitCommand);
      case CHECKOUT:
        return new CheckoutGitCommandDialog(parent, transMeta, gitCommand);

      default:
        throw new IllegalArgumentException("Unkomnown command type dialog.");
    }
  }
}
