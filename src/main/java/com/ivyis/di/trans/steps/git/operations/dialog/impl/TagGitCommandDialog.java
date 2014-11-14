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
import com.ivyis.di.trans.steps.git.command.impl.TagGitCommand;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;

/**
 * Tag command UI class.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class TagGitCommandDialog extends Dialog implements BaseGitCommandDialog {
  private static final Class<?> PKG = TagGitCommandDialog.class; // for i18n

  private Button wAnnotated, wForceUpdate, wSigned;
  private TextVar wName, wMessage;

  private Shell shell;
  private boolean modal;
  private PropsUI props;
  private TransMeta transMeta;
  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;
  private TagGitCommand tagCommand;

  public Shell getShell() {
    return shell;
  }

  public TagGitCommandDialog(Shell parent, TransMeta transMeta,
      GitCommand gitCommand) {
    super(parent, SWT.NONE);
    this.modal = true;
    this.transMeta = transMeta;
    props = PropsUI.getInstance();
    if (gitCommand != null && gitCommand instanceof TagGitCommand) {
      this.tagCommand = (TagGitCommand) gitCommand;
    } else {
      this.tagCommand = new TagGitCommand();
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
    shell.setText(BaseMessages.getString(PKG, "Git.TagCommandDialog.Title"));
    shell.setImage(GUIResource.getInstance().getImageSpoon());

    // Tag name
    final Label wlName = new Label(shell, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "Git.Name.Label"));
    props.setLook(wlName);
    final FormData fdlName = new FormData();
    fdlName.left = new FormAttachment(0, 25);
    fdlName.top = new FormAttachment(shell, margin);
    wlName.setLayoutData(fdlName);

    wName = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wName);
    final FormData fdName = new FormData();
    fdName.left = new FormAttachment(wlName, margin);
    fdName.top = new FormAttachment(shell, margin);
    fdName.right = new FormAttachment(100, 0);
    wName.setLayoutData(fdName);

    // Tag message
    final Label wlMessage = new Label(shell, SWT.RIGHT);
    wlMessage.setText(BaseMessages.getString(PKG, "Git.Message.Label"));
    props.setLook(wlMessage);
    final FormData fdlMessage = new FormData();
    fdlMessage.left = new FormAttachment(0, 25);
    fdlMessage.top = new FormAttachment(wName, margin);
    wlMessage.setLayoutData(fdlMessage);

    wMessage = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wMessage);
    final FormData fdMessage = new FormData();
    fdMessage.left = new FormAttachment(wlMessage, margin);
    fdMessage.top = new FormAttachment(wName, margin);
    fdMessage.right = new FormAttachment(100, 0);
    wMessage.setLayoutData(fdMessage);

    // Annotated
    final Label wlAnnotated = new Label(shell, SWT.RIGHT);
    wlAnnotated.setText(BaseMessages.getString(PKG, "Git.Annotated.Label"));
    props.setLook(wlAnnotated);
    final FormData fdlAnnotated = new FormData();
    fdlAnnotated.left = new FormAttachment(0, 25);
    fdlAnnotated.top = new FormAttachment(wMessage, margin);
    wlAnnotated.setLayoutData(fdlAnnotated);

    wAnnotated = new Button(shell, SWT.CHECK);
    props.setLook(wAnnotated);
    final FormData fdAnnotated = new FormData();
    fdAnnotated.left = new FormAttachment(wlAnnotated, margin);
    fdAnnotated.top = new FormAttachment(wMessage, margin);
    fdAnnotated.right = new FormAttachment(100, 0);
    wAnnotated.setLayoutData(fdAnnotated);

    // Force update
    final Label wlForceUpdate = new Label(shell, SWT.RIGHT);
    wlForceUpdate.setText(BaseMessages.getString(PKG,
        "Git.ForceUpdate.Label"));
    props.setLook(wlForceUpdate);
    final FormData fdlForceUpdate = new FormData();
    fdlForceUpdate.left = new FormAttachment(0, 25);
    fdlForceUpdate.top = new FormAttachment(wAnnotated, margin);
    wlForceUpdate.setLayoutData(fdlForceUpdate);

    wForceUpdate = new Button(shell, SWT.CHECK);
    props.setLook(wForceUpdate);
    final FormData fdForceUpdate = new FormData();
    fdForceUpdate.left = new FormAttachment(wlForceUpdate, margin);
    fdForceUpdate.top = new FormAttachment(wAnnotated, margin);
    fdForceUpdate.right = new FormAttachment(100, 0);
    wForceUpdate.setLayoutData(fdForceUpdate);

    // Signed
    final Label wlSigned = new Label(shell, SWT.RIGHT);
    wlSigned.setText(BaseMessages.getString(PKG, "Git.Signed.Label"));
    props.setLook(wlSigned);
    final FormData fdlSigned = new FormData();
    fdlSigned.left = new FormAttachment(0, 25);
    fdlSigned.top = new FormAttachment(wForceUpdate, margin);
    wlSigned.setLayoutData(fdlSigned);

    wSigned = new Button(shell, SWT.CHECK);
    props.setLook(wSigned);
    final FormData fdSigned = new FormData();
    fdSigned.left = new FormAttachment(wlSigned, margin);
    fdSigned.top = new FormAttachment(wForceUpdate, margin);
    fdSigned.right = new FormAttachment(100, 0);
    wSigned.setLayoutData(fdSigned);

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

    return this.tagCommand;

  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  public void getData() {
    wAnnotated.setSelection(this.tagCommand.isAnnotated());
    wForceUpdate.setSelection(this.tagCommand.isForceUpdate());
    wSigned.setSelection(this.tagCommand.isSigned());
    if (!Const.isEmpty(this.tagCommand.getMessage())) {
      wMessage.setText(this.tagCommand.getMessage());
    }

    if (!Const.isEmpty(this.tagCommand.getName())) {
      wName.setText(this.tagCommand.getName());
    }
  }

  private void cancel() {
    this.tagCommand = null;
    dispose();
  }

  private void ok() {
    if (Const.isEmpty(wName.getText())) {
      final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(BaseMessages.getString(PKG,
          "Git.TagName.Mandatory.DialogMessage"));
      mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
      mb.open();
      return;
    }
    this.tagCommand.setAnnotated(wAnnotated.getSelection());
    this.tagCommand.setForceUpdate(wForceUpdate.getSelection());
    this.tagCommand.setSigned(wSigned.getSelection());
    this.tagCommand.setMessage(wMessage.getText());
    this.tagCommand.setName(wName.getText());
    dispose();
  }
}
