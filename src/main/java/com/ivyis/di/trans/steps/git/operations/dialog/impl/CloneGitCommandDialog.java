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
import org.eclipse.swt.widgets.MessageBox;
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
import com.ivyis.di.trans.steps.git.command.impl.CloneGitCommand;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;

/**
 * Clone command UI class.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class CloneGitCommandDialog extends Dialog implements
    BaseGitCommandDialog {
  private static final Class<?> PKG = CloneGitCommandDialog.class; // for i18n

  private Button wCloneSubModulesField, wCloneAllBranchesField;
  private TextVar wBranchField;

  private Shell shell;
  private boolean modal;
  private PropsUI props;
  private TransMeta transMeta;
  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;
  private CloneGitCommand cloneCommand;

  public Shell getShell() {
    return shell;
  }

  public CloneGitCommandDialog(Shell parent, TransMeta transMeta,
      GitCommand gitCommand) {
    super(parent, SWT.NONE);
    this.modal = true;
    this.transMeta = transMeta;
    props = PropsUI.getInstance();
    if (gitCommand != null && gitCommand instanceof CloneGitCommand) {
      this.cloneCommand = (CloneGitCommand) gitCommand;
    } else {
      this.cloneCommand = new CloneGitCommand();
      this.cloneCommand.setBranchName("master");
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
        "Git.CloneCommandDialog.Title"));
    shell.setImage(GUIResource.getInstance().getImageSpoon());

    // Git branch name (optional)
    final Label wlBranchField = new Label(shell, SWT.RIGHT);
    wlBranchField.setText(BaseMessages.getString(PKG,
        "Git.BranchField.Label"));
    props.setLook(wlBranchField);
    final FormData fdlBranchField = new FormData();
    fdlBranchField.left = new FormAttachment(0, 25);
    fdlBranchField.top = new FormAttachment(shell, margin);
    wlBranchField.setLayoutData(fdlBranchField);

    wBranchField = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wBranchField);
    final FormData fdBranchField = new FormData();
    fdBranchField.left = new FormAttachment(wlBranchField, margin);
    fdBranchField.top = new FormAttachment(shell, margin);
    fdBranchField.right = new FormAttachment(100, 0);
    wBranchField.setLayoutData(fdBranchField);

    // Clone sub modules
    final Label wlCloneSubModulesField = new Label(shell, SWT.RIGHT);
    wlCloneSubModulesField.setText(BaseMessages.getString(PKG,
        "Git.CloneSubModulesField.Label"));
    props.setLook(wlCloneSubModulesField);
    final FormData fdlCloneSubModulesField = new FormData();
    fdlCloneSubModulesField.left = new FormAttachment(0, 25);
    fdlCloneSubModulesField.top = new FormAttachment(wBranchField, margin);
    wlCloneSubModulesField.setLayoutData(fdlCloneSubModulesField);

    wCloneSubModulesField = new Button(shell, SWT.CHECK);
    props.setLook(wCloneSubModulesField);
    final FormData fdCloneSubModulesField = new FormData();
    fdCloneSubModulesField.left = new FormAttachment(
        wlCloneSubModulesField, margin);
    fdCloneSubModulesField.top = new FormAttachment(wBranchField, margin);
    fdCloneSubModulesField.right = new FormAttachment(100, 0);
    wCloneSubModulesField.setLayoutData(fdCloneSubModulesField);

    // Clone sub modules
    final Label wlCloneAllBranchesField = new Label(shell, SWT.RIGHT);
    wlCloneAllBranchesField.setText(BaseMessages.getString(PKG,
        "Git.CloneAllBranchesField.Label"));
    props.setLook(wlCloneAllBranchesField);
    final FormData fdlCloneAllBranchesField = new FormData();
    fdlCloneAllBranchesField.left = new FormAttachment(0, 25);
    fdlCloneAllBranchesField.top = new FormAttachment(
        wCloneSubModulesField, margin);
    wlCloneAllBranchesField.setLayoutData(fdlCloneAllBranchesField);

    wCloneAllBranchesField = new Button(shell, SWT.CHECK);
    props.setLook(wCloneAllBranchesField);
    final FormData fdCloneAllBranchesField = new FormData();
    fdCloneAllBranchesField.left = new FormAttachment(
        wlCloneAllBranchesField, margin);
    fdCloneAllBranchesField.top = new FormAttachment(wCloneSubModulesField,
        margin);
    fdCloneAllBranchesField.right = new FormAttachment(100, 0);
    wCloneAllBranchesField.setLayoutData(fdCloneAllBranchesField);

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

    return cloneCommand;

  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  public void getData() {
    if (!Const.isEmpty(this.cloneCommand.getBranchName())) {
      wBranchField.setText(this.cloneCommand.getBranchName());
    }
    wCloneAllBranchesField.setSelection(this.cloneCommand
        .isCloneAllBranches());
    wCloneSubModulesField.setSelection(this.cloneCommand
        .isCloneSubModules());
  }

  private void cancel() {
    this.cloneCommand = null;
    dispose();
  }

  private void ok() {
    if (Const.isEmpty(wBranchField.getText())) {
      final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(BaseMessages.getString(PKG,
          "Git.Branch.Mandatory.DialogMessage"));
      mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
      mb.open();
      return;
    }
    cloneCommand.setBranchName(wBranchField.getText());
    cloneCommand.setCloneAllBranches(wCloneAllBranchesField.getSelection());
    cloneCommand.setCloneSubModules(wCloneSubModulesField.getSelection());
    dispose();
  }
}
