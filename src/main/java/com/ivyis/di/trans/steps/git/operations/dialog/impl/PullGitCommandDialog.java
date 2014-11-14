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
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.ivyis.di.trans.steps.git.command.GitCommand;
import com.ivyis.di.trans.steps.git.command.impl.PullGitCommand;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;

/**
 * Pull command UI class.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class PullGitCommandDialog extends Dialog implements
    BaseGitCommandDialog {
  private static final Class<?> PKG = PullGitCommandDialog.class; // for i18n

  private Button wRebase;

  private Shell shell;
  private boolean modal;
  private PropsUI props;
  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;
  private PullGitCommand pullCommand;

  public Shell getShell() {
    return shell;
  }

  public PullGitCommandDialog(Shell parent, TransMeta transMeta,
      GitCommand gitCommand) {
    super(parent, SWT.NONE);
    this.modal = true;
    props = PropsUI.getInstance();
    if (gitCommand != null && gitCommand instanceof PullGitCommand) {
      this.pullCommand = (PullGitCommand) gitCommand;
    } else {
      this.pullCommand = new PullGitCommand();
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
        .getString(PKG, "Git.PullCommandDialog.Title"));
    shell.setImage(GUIResource.getInstance().getImageSpoon());

    // File pattern
    final Label wlRebase = new Label(shell, SWT.RIGHT);
    wlRebase.setText(BaseMessages.getString(PKG, "Git.Rebase.Label"));
    props.setLook(wlRebase);
    final FormData fdlRebase = new FormData();
    fdlRebase.left = new FormAttachment(0, 25);
    fdlRebase.top = new FormAttachment(shell, margin);
    wlRebase.setLayoutData(fdlRebase);

    wRebase = new Button(shell, SWT.CHECK);
    props.setLook(wRebase);
    final FormData fdRebase = new FormData();
    fdRebase.left = new FormAttachment(wlRebase, margin);
    fdRebase.top = new FormAttachment(shell, margin);
    fdRebase.right = new FormAttachment(100, 0);
    wRebase.setLayoutData(fdRebase);

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

    return this.pullCommand;

  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  public void getData() {
    wRebase.setSelection(this.pullCommand.isRebase());
  }

  private void cancel() {
    this.pullCommand = null;
    dispose();
  }

  private void ok() {
    this.pullCommand.setRebase(wRebase.getSelection());
    dispose();
  }
}
