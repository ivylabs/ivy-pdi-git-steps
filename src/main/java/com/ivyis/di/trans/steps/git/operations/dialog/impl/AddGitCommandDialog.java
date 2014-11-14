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
import com.ivyis.di.trans.steps.git.command.impl.AddGitCommand;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;

/**
 * Add command UI class.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class AddGitCommandDialog extends Dialog implements BaseGitCommandDialog {
  private static final Class<?> PKG = AddGitCommandDialog.class; // for i18n

  private Button wUpdate;
  private TextVar wFilepattern;

  private Shell shell;
  private boolean modal;
  private PropsUI props;
  private TransMeta transMeta;
  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;
  private AddGitCommand addCommand;

  public Shell getShell() {
    return shell;
  }

  public AddGitCommandDialog(Shell parent, TransMeta transMeta,
      GitCommand gitCommand) {
    super(parent, SWT.NONE);
    this.modal = true;
    this.transMeta = transMeta;
    props = PropsUI.getInstance();
    if (gitCommand != null && gitCommand instanceof AddGitCommand) {
      this.addCommand = (AddGitCommand) gitCommand;
    } else {
      this.addCommand = new AddGitCommand();
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
    shell.setText(BaseMessages.getString(PKG, "Git.AddCommandDialog.Title"));
    shell.setImage(GUIResource.getInstance().getImageSpoon());

    // File pattern
    final Label wlFilepattern = new Label(shell, SWT.RIGHT);
    wlFilepattern.setText(BaseMessages.getString(PKG,
        "Git.FilePattern.Label"));
    props.setLook(wlFilepattern);
    final FormData fdlFilepattern = new FormData();
    fdlFilepattern.left = new FormAttachment(0, 25);
    fdlFilepattern.top = new FormAttachment(shell, margin);
    wlFilepattern.setLayoutData(fdlFilepattern);

    wFilepattern = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wFilepattern);
    final FormData fdFilepattern = new FormData();
    fdFilepattern.left = new FormAttachment(wlFilepattern, margin);
    fdFilepattern.top = new FormAttachment(shell, margin);
    fdFilepattern.right = new FormAttachment(100, 0);
    wFilepattern.setLayoutData(fdFilepattern);

    // Update
    final Label wlUpdate = new Label(shell, SWT.RIGHT);
    wlUpdate.setText(BaseMessages.getString(PKG, "Git.Update.Label"));
    props.setLook(wlUpdate);
    final FormData fdlUpdate = new FormData();
    fdlUpdate.left = new FormAttachment(0, 25);
    fdlUpdate.top = new FormAttachment(wFilepattern, margin);
    wlUpdate.setLayoutData(fdlUpdate);

    wUpdate = new Button(shell, SWT.CHECK);
    props.setLook(wUpdate);
    final FormData fdUpdate = new FormData();
    fdUpdate.left = new FormAttachment(wlUpdate, margin);
    fdUpdate.top = new FormAttachment(wFilepattern, margin);
    fdUpdate.right = new FormAttachment(100, 0);
    wUpdate.setLayoutData(fdUpdate);

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

    return this.addCommand;

  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  public void getData() {
    if (!Const.isEmpty(this.addCommand.getFilepattern())) {
      wFilepattern.setText(this.addCommand.getFilepattern());
    }
    wUpdate.setSelection(this.addCommand.isUpdate());
  }

  private void cancel() {
    this.addCommand = null;
    dispose();
  }

  private void ok() {
    if (Const.isEmpty(wFilepattern.getText())) {
      final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(BaseMessages.getString(PKG,
          "Git.FilePattern.Mandatory.DialogMessage"));
      mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
      mb.open();
      return;
    }
    this.addCommand.setFilepattern(wFilepattern.getText());
    this.addCommand.setUpdate(wUpdate.getSelection());
    dispose();
  }
}
