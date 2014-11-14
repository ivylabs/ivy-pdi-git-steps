package com.ivyis.di.trans.steps.git.operations.dialog.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.ivyis.di.trans.steps.git.command.GitCommand;
import com.ivyis.di.trans.steps.git.command.impl.CommitGitCommand;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;

/**
 * Commit command UI class.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class CommitGitCommandDialog extends Dialog implements
    BaseGitCommandDialog {
  private static final Class<?> PKG = CommitGitCommandDialog.class; // for i18n

  private Button wAmend, wInsertChangeId, wAll;
  private TextVar wAuthorName, wAuthorEmail, wCommitterName, wCommitterEmail,
      wCommitMessage;

  private Shell shell;
  private boolean modal;
  private PropsUI props;
  private TransMeta transMeta;
  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;
  private CommitGitCommand commitCommand;

  public Shell getShell() {
    return shell;
  }

  public CommitGitCommandDialog(Shell parent, TransMeta transMeta,
      GitCommand gitCommand) {
    super(parent, SWT.NONE);
    this.modal = true;
    this.transMeta = transMeta;
    props = PropsUI.getInstance();
    if (gitCommand != null && gitCommand instanceof CommitGitCommand) {
      this.commitCommand = (CommitGitCommand) gitCommand;
    } else {
      this.commitCommand = new CommitGitCommand();
    }
  }

  public GitCommand open() {
    final Shell parent = getParent();
    final Display display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM
        | (modal ? SWT.APPLICATION_MODAL | SWT.SHEET : SWT.NONE)
        | SWT.RESIZE | SWT.MIN | SWT.MAX);
    props.setLook(shell);

    final FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    final int margin = Const.MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG,
        "Git.CommitCommandDialog.Title"));
    shell.setImage(GUIResource.getInstance().getImageSpoon());

    // Git author name
    final Label wlAuthorName = new Label(shell, SWT.RIGHT);
    wlAuthorName.setText(BaseMessages
        .getString(PKG, "Git.AuthorName.Label"));
    props.setLook(wlAuthorName);
    final FormData fdlAuthorName = new FormData();
    fdlAuthorName.left = new FormAttachment(0, 25);
    fdlAuthorName.top = new FormAttachment(shell, margin);
    wlAuthorName.setLayoutData(fdlAuthorName);

    wAuthorName = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wAuthorName);
    final FormData fdAuthorName = new FormData();
    fdAuthorName.left = new FormAttachment(wlAuthorName, margin);
    fdAuthorName.top = new FormAttachment(shell, margin);
    fdAuthorName.right = new FormAttachment(100, 0);
    wAuthorName.setLayoutData(fdAuthorName);

    // Git author email
    final Label wlAuthorEmail = new Label(shell, SWT.RIGHT);
    wlAuthorEmail.setText(BaseMessages.getString(PKG,
        "Git.AuthorEmail.Label"));
    props.setLook(wlAuthorEmail);
    final FormData fdlAuthorEmail = new FormData();
    fdlAuthorEmail.left = new FormAttachment(0, 25);
    fdlAuthorEmail.top = new FormAttachment(wAuthorName, margin);
    wlAuthorEmail.setLayoutData(fdlAuthorEmail);

    wAuthorEmail = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wAuthorEmail);
    final FormData fdAuthorEmail = new FormData();
    fdAuthorEmail.left = new FormAttachment(wlAuthorEmail, margin);
    fdAuthorEmail.top = new FormAttachment(wAuthorName, margin);
    fdAuthorEmail.right = new FormAttachment(100, 0);
    wAuthorEmail.setLayoutData(fdAuthorEmail);

    // Git committer name
    final Label wlCommitterName = new Label(shell, SWT.RIGHT);
    wlCommitterName.setText(BaseMessages.getString(PKG,
        "Git.CommitterName.Label"));
    props.setLook(wlCommitterName);
    final FormData fdlCommitterName = new FormData();
    fdlCommitterName.left = new FormAttachment(0, 25);
    fdlCommitterName.top = new FormAttachment(wAuthorEmail, margin);
    wlCommitterName.setLayoutData(fdlCommitterName);

    wCommitterName = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wCommitterName);
    final FormData fdCommitterName = new FormData();
    fdCommitterName.left = new FormAttachment(wlCommitterName, margin);
    fdCommitterName.top = new FormAttachment(wAuthorEmail, margin);
    fdCommitterName.right = new FormAttachment(100, 0);
    wCommitterName.setLayoutData(fdCommitterName);

    // Git committer email
    final Label wlCommitterEmail = new Label(shell, SWT.RIGHT);
    wlCommitterEmail.setText(BaseMessages.getString(PKG,
        "Git.CommitterEmail.Label"));
    props.setLook(wlCommitterEmail);
    final FormData fdlCommitterEmail = new FormData();
    fdlCommitterEmail.left = new FormAttachment(0, 25);
    fdlCommitterEmail.top = new FormAttachment(wCommitterName, margin);
    wlCommitterEmail.setLayoutData(fdlCommitterEmail);

    wCommitterEmail = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wCommitterEmail);
    final FormData fdCommitterEmail = new FormData();
    fdCommitterEmail.left = new FormAttachment(wlCommitterEmail, margin);
    fdCommitterEmail.top = new FormAttachment(wCommitterName, margin);
    fdCommitterEmail.right = new FormAttachment(100, 0);
    wCommitterEmail.setLayoutData(fdCommitterEmail);

    // Git commit message
    final Label wlCommitMessage = new Label(shell, SWT.RIGHT);
    wlCommitMessage.setText(BaseMessages.getString(PKG,
        "Git.CommitMessage.Label"));
    props.setLook(wlCommitMessage);
    final FormData fdlCommitMessage = new FormData();
    fdlCommitMessage.left = new FormAttachment(0, 25);
    fdlCommitMessage.top = new FormAttachment(wCommitterEmail, margin);
    wlCommitMessage.setLayoutData(fdlCommitMessage);

    wCommitMessage = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wCommitMessage);
    final FormData fdCommitMessage = new FormData();
    fdCommitMessage.left = new FormAttachment(wlCommitMessage, margin);
    fdCommitMessage.top = new FormAttachment(wCommitterEmail, margin);
    fdCommitMessage.right = new FormAttachment(100, 0);
    wCommitMessage.setLayoutData(fdCommitMessage);

    // Git Amend
    final Label wlAmend = new Label(shell, SWT.RIGHT);
    wlAmend.setText(BaseMessages.getString(PKG, "Git.Amend.Label"));
    props.setLook(wlAmend);
    final FormData fdlAmend = new FormData();
    fdlAmend.left = new FormAttachment(0, 25);
    fdlAmend.top = new FormAttachment(wCommitMessage, margin);
    wlAmend.setLayoutData(fdlAmend);

    wAmend = new Button(shell, SWT.CHECK);
    props.setLook(wAmend);
    final FormData fdAmend = new FormData();
    fdAmend.left = new FormAttachment(wlAmend, margin);
    fdAmend.top = new FormAttachment(wCommitMessage, margin);
    fdAmend.right = new FormAttachment(100, 0);
    wAmend.setLayoutData(fdAmend);

    // Git insert change id
    final Label wlInsertChangeId = new Label(shell, SWT.RIGHT);
    wlInsertChangeId.setText(BaseMessages.getString(PKG,
        "Git.InsertChangeId.Label"));
    props.setLook(wlInsertChangeId);
    final FormData fdlInsertChangeId = new FormData();
    fdlInsertChangeId.left = new FormAttachment(0, 25);
    fdlInsertChangeId.top = new FormAttachment(wAmend, margin);
    wlInsertChangeId.setLayoutData(fdlInsertChangeId);

    wInsertChangeId = new Button(shell, SWT.CHECK);
    props.setLook(wInsertChangeId);
    final FormData fdInsertChangeId = new FormData();
    fdInsertChangeId.left = new FormAttachment(wlInsertChangeId, margin);
    fdInsertChangeId.top = new FormAttachment(wAmend, margin);
    fdInsertChangeId.right = new FormAttachment(100, 0);
    wInsertChangeId.setLayoutData(fdInsertChangeId);

    // Git all
    final Label wlAll = new Label(shell, SWT.RIGHT);
    wlAll.setText(BaseMessages.getString(PKG, "Git.All.Label"));
    props.setLook(wlAll);
    final FormData fdlAll = new FormData();
    fdlAll.left = new FormAttachment(0, 25);
    fdlAll.top = new FormAttachment(wInsertChangeId, margin);
    wlAll.setLayoutData(fdlAll);

    wAll = new Button(shell, SWT.CHECK);
    props.setLook(wAll);
    final FormData fdAll = new FormData();
    fdAll.left = new FormAttachment(wlAll, margin);
    fdAll.top = new FormAttachment(wInsertChangeId, margin);
    fdAll.right = new FormAttachment(100, 0);
    wAll.setLayoutData(fdAll);

    // Some buttons
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    lsOK = new Listener() {
      public void handleEvent(Event e) {
        ok();
      }
    };
    wOK.addListener(SWT.Selection, lsOK);

    Button[] buttons = new Button[] {wOK};

    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    lsCancel = new Listener() {
      public void handleEvent(Event e) {
        cancel();
      }
    };
    wCancel.addListener(SWT.Selection, lsCancel);

    buttons = new Button[] {wOK, wCancel};

    BaseStepDialog.positionBottomButtons(shell, buttons, margin, null);

    // Add listeners
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    getData();

    BaseStepDialog.setSize(shell);

    wOK.setFocus();

    shell.open();

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }

    return this.commitCommand;

  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  public void getData() {
    if (!Const.isEmpty(this.commitCommand.getAuthorEmail())) {
      wAuthorEmail.setText(this.commitCommand.getAuthorEmail());
    }
    if (!Const.isEmpty(this.commitCommand.getAuthorName())) {
      wAuthorName.setText(this.commitCommand.getAuthorName());
    }
    if (!Const.isEmpty(this.commitCommand.getCommitterEmail())) {
      wCommitterEmail.setText(this.commitCommand.getCommitterEmail());
    }
    if (!Const.isEmpty(this.commitCommand.getCommitterName())) {
      wCommitterName.setText(this.commitCommand.getCommitterName());
    }
    if (!Const.isEmpty(this.commitCommand.getCommitMessage())) {
      wCommitMessage.setText(this.commitCommand.getCommitMessage());
    }
    wAll.setSelection(this.commitCommand.isAll());
    wAmend.setSelection(this.commitCommand.isAmend());
    wInsertChangeId.setSelection(this.commitCommand.isInsertChangeId());
  }

  private void cancel() {
    this.commitCommand = null;
    dispose();
  }

  private void ok() {
    this.commitCommand.setAuthorEmail(wAuthorEmail.getText());
    this.commitCommand.setAuthorName(wAuthorName.getText());
    this.commitCommand.setCommitterEmail(wCommitterEmail.getText());
    this.commitCommand.setCommitterName(wCommitterName.getText());
    this.commitCommand.setCommitMessage(wCommitMessage.getText());
    this.commitCommand.setAll(wAll.getSelection());
    this.commitCommand.setAmend(wAmend.getSelection());
    this.commitCommand.setInsertChangeId(wInsertChangeId.getSelection());
    dispose();
  }
}
