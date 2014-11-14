package com.ivyis.di.trans.steps.git.info.dialog.impl;

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
import com.ivyis.di.trans.steps.git.command.impl.LogGitCommand;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;

/**
 * Log command UI class.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class LogGitCommandDialog extends Dialog implements BaseGitCommandDialog {
  private static final Class<?> PKG = LogGitCommandDialog.class; // for i18n

  private TextVar wPath, wMaxCount, wSkip;

  private Shell shell;
  private boolean modal;
  private PropsUI props;
  private TransMeta transMeta;
  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;
  private LogGitCommand logGitCommand;

  public Shell getShell() {
    return shell;
  }

  public LogGitCommandDialog(Shell parent, TransMeta transMeta,
      GitCommand gitCommand) {
    super(parent, SWT.NONE);
    this.modal = true;
    this.transMeta = transMeta;
    props = PropsUI.getInstance();
    if (gitCommand != null && gitCommand instanceof LogGitCommand) {
      this.logGitCommand = (LogGitCommand) gitCommand;
    } else {
      this.logGitCommand = new LogGitCommand();
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
    shell.setText(BaseMessages.getString(PKG, "Git.LogCommandDialog.Title"));
    shell.setImage(GUIResource.getInstance().getImageSpoon());

    // Path field
    final Label wlPath = new Label(shell, SWT.RIGHT);
    wlPath.setText(BaseMessages.getString(PKG, "Git.Path.Label"));
    props.setLook(wlPath);
    final FormData fdlPath = new FormData();
    fdlPath.left = new FormAttachment(0, 25);
    fdlPath.top = new FormAttachment(shell, margin);
    wlPath.setLayoutData(fdlPath);

    wPath = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wPath);
    final FormData fdPath = new FormData();
    fdPath.left = new FormAttachment(wlPath, margin);
    fdPath.top = new FormAttachment(shell, margin);
    fdPath.right = new FormAttachment(100, 0);
    wPath.setLayoutData(fdPath);

    // Max count field
    final Label wlMaxCount = new Label(shell, SWT.RIGHT);
    wlMaxCount.setText(BaseMessages.getString(PKG, "Git.MaxCount.Label"));
    props.setLook(wlMaxCount);
    final FormData fdlMaxCount = new FormData();
    fdlMaxCount.left = new FormAttachment(0, 25);
    fdlMaxCount.top = new FormAttachment(wPath, margin);
    wlMaxCount.setLayoutData(fdlMaxCount);

    wMaxCount = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wMaxCount);
    final FormData fdMaxCount = new FormData();
    fdMaxCount.left = new FormAttachment(wlMaxCount, margin);
    fdMaxCount.top = new FormAttachment(wPath, margin);
    fdMaxCount.right = new FormAttachment(100, 0);
    wMaxCount.setLayoutData(fdMaxCount);

    // Skip field
    final Label wlSkip = new Label(shell, SWT.RIGHT);
    wlSkip.setText(BaseMessages.getString(PKG, "Git.Skip.Label"));
    props.setLook(wlSkip);
    final FormData fdlSkip = new FormData();
    fdlSkip.left = new FormAttachment(0, 25);
    fdlSkip.top = new FormAttachment(wMaxCount, margin);
    wlSkip.setLayoutData(fdlSkip);

    wSkip = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wSkip);
    final FormData fdSkip = new FormData();
    fdSkip.left = new FormAttachment(wlSkip, margin);
    fdSkip.top = new FormAttachment(wMaxCount, margin);
    fdSkip.right = new FormAttachment(100, 0);
    wSkip.setLayoutData(fdSkip);

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

    return this.logGitCommand;

  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  public void getData() {
    if (!Const.isEmpty(this.logGitCommand.getPath())) {
      wPath.setText(this.logGitCommand.getPath());
    }
    if (this.logGitCommand.getMaxCount() != null) {
      wMaxCount.setText(String.valueOf(this.logGitCommand.getMaxCount()));
    }
    if (this.logGitCommand.getMaxCount() != null) {
      wSkip.setText(String.valueOf(this.logGitCommand.getSkip()));
    }
  }

  private void cancel() {
    dispose();
  }

  private void ok() {
    if (!Const.isEmpty(wPath.getText())) {
      this.logGitCommand.setPath(wPath.getText());
    } else {
      this.logGitCommand.setPath(null);
    }

    if (!Const.isEmpty(wMaxCount.getText())) {
      try {
        Integer.parseInt(transMeta.environmentSubstitute(wMaxCount
            .getText()));
        this.logGitCommand.setMaxCount(wMaxCount.getText());
      } catch (NumberFormatException e) {
        final MessageBox mb = new MessageBox(shell, SWT.OK
            | SWT.ICON_ERROR);
        mb.setMessage(BaseMessages.getString(PKG,
            "Git.MaxCount.NumberParseError.DialogMessage"));
        mb.setText(BaseMessages.getString(PKG,
            "System.Dialog.Error.Title"));
        mb.open();
        return;
      }
    } else {
      this.logGitCommand.setMaxCount(null);
    }

    if (!Const.isEmpty(wSkip.getText())) {
      try {
        Integer.parseInt(transMeta.environmentSubstitute(wSkip
            .getText()));
        this.logGitCommand.setSkip(wSkip.getText());
      } catch (NumberFormatException e) {
        final MessageBox mb = new MessageBox(shell, SWT.OK
            | SWT.ICON_ERROR);
        mb.setMessage(BaseMessages.getString(PKG,
            "Git.Skip.NumberParseError.DialogMessage"));
        mb.setText(BaseMessages.getString(PKG,
            "System.Dialog.Error.Title"));
        mb.open();
        return;
      }
    } else {
      this.logGitCommand.setSkip(null);
    }

    dispose();
  }
}
