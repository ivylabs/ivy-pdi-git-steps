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
import com.ivyis.di.trans.steps.git.command.impl.CheckoutGitCommand;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;

/**
 * Checkout command UI class.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class CheckoutGitCommandDialog extends Dialog implements
    BaseGitCommandDialog {
  private static final Class<?> PKG = CheckoutGitCommandDialog.class; // for i18n

  private Button wAllPaths, wForce;
  private TextVar wPath, wName, wStartPoint;

  private Shell shell;
  private boolean modal;
  private PropsUI props;
  private TransMeta transMeta;
  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;
  private CheckoutGitCommand checkoutGitCommand;

  public Shell getShell() {
    return shell;
  }

  public CheckoutGitCommandDialog(Shell parent, TransMeta transMeta,
      GitCommand gitCommand) {
    super(parent, SWT.NONE);
    this.modal = true;
    this.transMeta = transMeta;
    props = PropsUI.getInstance();
    if (gitCommand != null && gitCommand instanceof CheckoutGitCommand) {
      this.checkoutGitCommand = (CheckoutGitCommand) gitCommand;
    } else {
      this.checkoutGitCommand = new CheckoutGitCommand();
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
        "Git.CheckoutCommandDialog.Title"));
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

    // Name field
    final Label wlName = new Label(shell, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "Git.Name.Label"));
    props.setLook(wlName);
    final FormData fdlName = new FormData();
    fdlName.left = new FormAttachment(0, 25);
    fdlName.top = new FormAttachment(wPath, margin);
    wlName.setLayoutData(fdlName);

    wName = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wName);
    final FormData fdName = new FormData();
    fdName.left = new FormAttachment(wlName, margin);
    fdName.top = new FormAttachment(wPath, margin);
    fdName.right = new FormAttachment(100, 0);
    wName.setLayoutData(fdName);

    // Start point field
    final Label wlStartPoint = new Label(shell, SWT.RIGHT);
    wlStartPoint.setText(BaseMessages
        .getString(PKG, "Git.StartPoint.Label"));
    props.setLook(wlStartPoint);
    final FormData fdlStartPoint = new FormData();
    fdlStartPoint.left = new FormAttachment(0, 25);
    fdlStartPoint.top = new FormAttachment(wName, margin);
    wlStartPoint.setLayoutData(fdlStartPoint);

    wStartPoint = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    props.setLook(wStartPoint);
    final FormData fdStartPoint = new FormData();
    fdStartPoint.left = new FormAttachment(wlStartPoint, margin);
    fdStartPoint.top = new FormAttachment(wName, margin);
    fdStartPoint.right = new FormAttachment(100, 0);
    wStartPoint.setLayoutData(fdStartPoint);

    // All Paths field
    final Label wlAllPaths = new Label(shell, SWT.RIGHT);
    wlAllPaths.setText(BaseMessages.getString(PKG, "Git.AllPaths.Label"));
    props.setLook(wlAllPaths);
    final FormData fdlAllPaths = new FormData();
    fdlAllPaths.left = new FormAttachment(0, 25);
    fdlAllPaths.top = new FormAttachment(wStartPoint, margin);
    wlAllPaths.setLayoutData(fdlAllPaths);

    wAllPaths = new Button(shell, SWT.CHECK);
    props.setLook(wAllPaths);
    final FormData fdAllPaths = new FormData();
    fdAllPaths.left = new FormAttachment(wlAllPaths, margin);
    fdAllPaths.top = new FormAttachment(wStartPoint, margin);
    fdAllPaths.right = new FormAttachment(100, 0);
    wAllPaths.setLayoutData(fdAllPaths);

    // force field
    final Label wlForce = new Label(shell, SWT.RIGHT);
    wlForce.setText(BaseMessages.getString(PKG, "Git.Force.Label"));
    props.setLook(wlForce);
    final FormData fdlForce = new FormData();
    fdlForce.left = new FormAttachment(0, 25);
    fdlForce.top = new FormAttachment(wAllPaths, margin);
    wlForce.setLayoutData(fdlForce);

    wForce = new Button(shell, SWT.CHECK);
    props.setLook(wForce);
    final FormData fdForce = new FormData();
    fdForce.left = new FormAttachment(wlForce, margin);
    fdForce.top = new FormAttachment(wAllPaths, margin);
    fdForce.right = new FormAttachment(100, 0);
    wForce.setLayoutData(fdForce);

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

    return this.checkoutGitCommand;

  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  public void getData() {
    if (!Const.isEmpty(this.checkoutGitCommand.getName())) {
      wName.setText(this.checkoutGitCommand.getName());
    }
    if (!Const.isEmpty(this.checkoutGitCommand.getPath())) {
      wPath.setText(this.checkoutGitCommand.getPath());
    }
    if (!Const.isEmpty(this.checkoutGitCommand.getStartPoint())) {
      wStartPoint.setText(this.checkoutGitCommand.getStartPoint());
    }
    wAllPaths.setSelection(this.checkoutGitCommand.isAllPaths());
    wForce.setSelection(this.checkoutGitCommand.isForce());
  }

  private void cancel() {
    this.checkoutGitCommand = null;
    dispose();
  }

  private void ok() {
    this.checkoutGitCommand.setName(wName.getText());
    this.checkoutGitCommand.setPath(Const.isEmpty(wPath.getText()) ? ""
        : wPath.getText());
    this.checkoutGitCommand.setStartPoint(Const.isEmpty(wStartPoint
        .getText()) ? "" : wStartPoint.getText());
    this.checkoutGitCommand.setAllPaths(wAllPaths.getSelection());
    this.checkoutGitCommand.setForce(wForce.getSelection());
    dispose();
  }
}
