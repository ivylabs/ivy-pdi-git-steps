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
import com.ivyis.di.trans.steps.git.command.impl.InitGitCommand;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;

/**
 * Init command UI class.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class InitGitCommandDialog extends Dialog implements
    BaseGitCommandDialog {
  private static final Class<?> PKG = InitGitCommandDialog.class; // for i18n

  private Button wBare;

  private Shell shell;
  private boolean modal;
  private PropsUI props;
  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;
  private InitGitCommand initCommand;

  public Shell getShell() {
    return shell;
  }

  public InitGitCommandDialog(Shell parent, TransMeta transMeta,
      GitCommand gitCommand) {
    super(parent, SWT.NONE);
    this.modal = true;
    props = PropsUI.getInstance();
    if (gitCommand != null && gitCommand instanceof InitGitCommand) {
      this.initCommand = (InitGitCommand) gitCommand;
    } else {
      this.initCommand = new InitGitCommand();
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
        .getString(PKG, "Git.InitCommandDialog.Title"));
    shell.setImage(GUIResource.getInstance().getImageSpoon());

    // Bare
    final Label wlBare = new Label(shell, SWT.RIGHT);
    wlBare.setText(BaseMessages.getString(PKG, "Git.Bare.Label"));
    props.setLook(wlBare);
    final FormData fdlBare = new FormData();
    fdlBare.left = new FormAttachment(0, 25);
    fdlBare.top = new FormAttachment(shell, margin);
    wlBare.setLayoutData(fdlBare);

    wBare = new Button(shell, SWT.CHECK);
    props.setLook(wBare);
    final FormData fdBare = new FormData();
    fdBare.left = new FormAttachment(wlBare, margin);
    fdBare.top = new FormAttachment(shell, margin);
    fdBare.right = new FormAttachment(100, 0);
    wBare.setLayoutData(fdBare);

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

    return this.initCommand;

  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  public void getData() {
    wBare.setSelection(this.initCommand.isBare());
  }

  private void cancel() {
    this.initCommand = null;
    dispose();
  }

  private void ok() {
    this.initCommand.setBare(wBare.getSelection());
    dispose();
  }
}
