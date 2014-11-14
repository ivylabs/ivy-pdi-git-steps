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
import com.ivyis.di.trans.steps.git.command.impl.PushGitCommand;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;

/**
 * Push command UI class.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class PushGitCommandDialog extends Dialog implements
    BaseGitCommandDialog {
  private static final Class<?> PKG = PushGitCommandDialog.class; // for i18n

  private Button wDryRun, wForce, wThin, wPushAllBranches, wPushAllTags;
  private TextVar wReferenceToPush, wRemote, wReceivePack;

  private Shell shell;
  private boolean modal;
  private PropsUI props;
  private TransMeta transMeta;
  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;
  private PushGitCommand pushCommand;

  public Shell getShell() {
    return shell;
  }

  public PushGitCommandDialog(Shell parent, TransMeta transMeta,
      GitCommand gitCommand) {
    super(parent, SWT.NONE);
    this.modal = true;
    this.transMeta = transMeta;
    props = PropsUI.getInstance();
    if (gitCommand != null && gitCommand instanceof PushGitCommand) {
      this.pushCommand = (PushGitCommand) gitCommand;
    } else {
      this.pushCommand = new PushGitCommand();
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
    shell.setText(BaseMessages
        .getString(PKG, "Git.PushCommandDialog.Title"));
    shell.setImage(GUIResource.getInstance().getImageSpoon());

    // Reference to push
    final Label wlReferenceToPush = new Label(shell, SWT.RIGHT);
    wlReferenceToPush.setText(BaseMessages.getString(PKG,
        "Git.ReferenceToPush.Label"));
    props.setLook(wlReferenceToPush);
    final FormData fdlReferenceToPush = new FormData();
    fdlReferenceToPush.left = new FormAttachment(0, 25);
    fdlReferenceToPush.top = new FormAttachment(shell, margin);
    wlReferenceToPush.setLayoutData(fdlReferenceToPush);

    wReferenceToPush = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wReferenceToPush);
    final FormData fdReferenceToPush = new FormData();
    fdReferenceToPush.left = new FormAttachment(wlReferenceToPush, margin);
    fdReferenceToPush.top = new FormAttachment(shell, margin);
    fdReferenceToPush.right = new FormAttachment(100, 0);
    wReferenceToPush.setLayoutData(fdReferenceToPush);

    // Remote
    final Label wlRemote = new Label(shell, SWT.RIGHT);
    wlRemote.setText(BaseMessages.getString(PKG, "Git.Remote.Label"));
    props.setLook(wlRemote);
    final FormData fdlRemote = new FormData();
    fdlRemote.left = new FormAttachment(0, 25);
    fdlRemote.top = new FormAttachment(wReferenceToPush, margin);
    wlRemote.setLayoutData(fdlRemote);

    wRemote = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wRemote);
    final FormData fdRemote = new FormData();
    fdRemote.left = new FormAttachment(wlRemote, margin);
    fdRemote.top = new FormAttachment(wReferenceToPush, margin);
    fdRemote.right = new FormAttachment(100, 0);
    wRemote.setLayoutData(fdRemote);

    // Receive Pack
    final Label wlReceivePack = new Label(shell, SWT.RIGHT);
    wlReceivePack.setText(BaseMessages.getString(PKG,
        "Git.ReceivePack.Label"));
    props.setLook(wlReceivePack);
    final FormData fdlReceivePack = new FormData();
    fdlReceivePack.left = new FormAttachment(0, 25);
    fdlReceivePack.top = new FormAttachment(wRemote, margin);
    wlReceivePack.setLayoutData(fdlReceivePack);

    wReceivePack = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wReceivePack);
    final FormData fdReceivePack = new FormData();
    fdReceivePack.left = new FormAttachment(wlReceivePack, margin);
    fdReceivePack.top = new FormAttachment(wRemote, margin);
    fdReceivePack.right = new FormAttachment(100, 0);
    wReceivePack.setLayoutData(fdReceivePack);

    // Dry Run
    final Label wlDryRun = new Label(shell, SWT.RIGHT);
    wlDryRun.setText(BaseMessages.getString(PKG, "Git.DryRun.Label"));
    props.setLook(wlDryRun);
    final FormData fdlDryRun = new FormData();
    fdlDryRun.left = new FormAttachment(0, 25);
    fdlDryRun.top = new FormAttachment(wReceivePack, margin);
    wlDryRun.setLayoutData(fdlDryRun);

    wDryRun = new Button(shell, SWT.CHECK);
    props.setLook(wDryRun);
    final FormData fdDryRun = new FormData();
    fdDryRun.left = new FormAttachment(wlDryRun, margin);
    fdDryRun.top = new FormAttachment(wReceivePack, margin);
    fdDryRun.right = new FormAttachment(100, 0);
    wDryRun.setLayoutData(fdDryRun);

    // Force
    final Label wlForce = new Label(shell, SWT.RIGHT);
    wlForce.setText(BaseMessages.getString(PKG, "Git.Force.Label"));
    props.setLook(wlForce);
    final FormData fdlForce = new FormData();
    fdlForce.left = new FormAttachment(0, 25);
    fdlForce.top = new FormAttachment(wDryRun, margin);
    wlForce.setLayoutData(fdlForce);

    wForce = new Button(shell, SWT.CHECK);
    props.setLook(wForce);
    final FormData fdForce = new FormData();
    fdForce.left = new FormAttachment(wlForce, margin);
    fdForce.top = new FormAttachment(wDryRun, margin);
    fdForce.right = new FormAttachment(100, 0);
    wForce.setLayoutData(fdForce);

    // Thin
    final Label wlThin = new Label(shell, SWT.RIGHT);
    wlThin.setText(BaseMessages.getString(PKG, "Git.Thin.Label"));
    props.setLook(wlThin);
    final FormData fdlThin = new FormData();
    fdlThin.left = new FormAttachment(0, 25);
    fdlThin.top = new FormAttachment(wForce, margin);
    wlThin.setLayoutData(fdlThin);

    wThin = new Button(shell, SWT.CHECK);
    props.setLook(wThin);
    final FormData fdThin = new FormData();
    fdThin.left = new FormAttachment(wlThin, margin);
    fdThin.top = new FormAttachment(wForce, margin);
    fdThin.right = new FormAttachment(100, 0);
    wThin.setLayoutData(fdThin);

    // Push all branches
    final Label wlPushAllBranches = new Label(shell, SWT.RIGHT);
    wlPushAllBranches.setText(BaseMessages.getString(PKG,
        "Git.PushAllBranches.Label"));
    props.setLook(wlPushAllBranches);
    final FormData fdlPushAllBranches = new FormData();
    fdlPushAllBranches.left = new FormAttachment(0, 25);
    fdlPushAllBranches.top = new FormAttachment(wThin, margin);
    wlPushAllBranches.setLayoutData(fdlPushAllBranches);

    wPushAllBranches = new Button(shell, SWT.CHECK);
    props.setLook(wPushAllBranches);
    final FormData fdPushAllBranches = new FormData();
    fdPushAllBranches.left = new FormAttachment(wlPushAllBranches, margin);
    fdPushAllBranches.top = new FormAttachment(wThin, margin);
    fdPushAllBranches.right = new FormAttachment(100, 0);
    wPushAllBranches.setLayoutData(fdPushAllBranches);

    // Push All Tags
    final Label wlPushAllTags = new Label(shell, SWT.RIGHT);
    wlPushAllTags.setText(BaseMessages.getString(PKG,
        "Git.PushAllTags.Label"));
    props.setLook(wlPushAllTags);
    final FormData fdlPushAllTags = new FormData();
    fdlPushAllTags.left = new FormAttachment(0, 25);
    fdlPushAllTags.top = new FormAttachment(wPushAllBranches, margin);
    wlPushAllTags.setLayoutData(fdlPushAllTags);

    wPushAllTags = new Button(shell, SWT.CHECK);
    props.setLook(wPushAllTags);
    final FormData fdPushAllTags = new FormData();
    fdPushAllTags.left = new FormAttachment(wlPushAllTags, margin);
    fdPushAllTags.top = new FormAttachment(wPushAllBranches, margin);
    fdPushAllTags.right = new FormAttachment(100, 0);
    wPushAllTags.setLayoutData(fdPushAllTags);

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

    return this.pushCommand;

  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  public void getData() {
    wDryRun.setSelection(this.pushCommand.isDryRun());
    wForce.setSelection(this.pushCommand.isForce());
    wPushAllBranches.setSelection(this.pushCommand.isPushAllBranches());
    wPushAllTags.setSelection(this.pushCommand.isPushAllTags());
    wThin.setSelection(this.pushCommand.isThin());
    if (!Const.isEmpty(this.pushCommand.getReceivePack())) {
      wReceivePack.setText(this.pushCommand.getReceivePack());
    }
    if (!Const.isEmpty(this.pushCommand.getReferenceToPush())) {
      wReferenceToPush.setText(this.pushCommand.getReferenceToPush());
    }
    if (!Const.isEmpty(this.pushCommand.getRemote())) {
      wRemote.setText(this.pushCommand.getRemote());
    }
  }

  private void cancel() {
    this.pushCommand = null;
    dispose();
  }

  private void ok() {
    this.pushCommand.setDryRun(wDryRun.getSelection());
    this.pushCommand.setForce(wForce.getSelection());
    this.pushCommand.setPushAllBranches(wPushAllBranches.getSelection());
    this.pushCommand.setPushAllTags(wPushAllTags.getSelection());
    this.pushCommand.setThin(wThin.getSelection());
    this.pushCommand.setReceivePack(wReceivePack.getText());
    this.pushCommand.setReferenceToPush(wReferenceToPush.getText());
    this.pushCommand.setRemote(wRemote.getText());
    dispose();
  }
}
