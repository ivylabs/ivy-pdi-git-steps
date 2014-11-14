package com.ivyis.di.trans.steps.git.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.ShowBrowserDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.ImageUtil;

import com.ivyis.di.trans.steps.git.GitCommandType;
import com.ivyis.di.trans.steps.git.command.GitCommand;
import com.ivyis.di.trans.steps.git.command.GitCommandFactory;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;
import com.ivyis.di.trans.steps.git.dialog.GitCommandDialogFactory;

/**
 * This class is responsible for Push notification UI on Spoon.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class GitOperationsStepDialog extends BaseStepDialog implements
    StepDialogInterface {

  /** for i18n purposes. **/
  private static final Class<?> PKG = GitOperationsStepDialog.class;

  private GitOperationsStepMeta input;
  private TextVar wUsernameField, wRepoUrlField, wRepoFolderPathField;
  private LabelTextVar wPasswordField;
  private TableView wGitCommands;
  private ColumnInfo[] ciOperations;

  public GitOperationsStepDialog(Shell parent, BaseStepMeta in,
      TransMeta transMeta, String sname) {
    super(parent, in, transMeta, sname);
    this.input = (GitOperationsStepMeta) in;
  }

  /**
   * Opens a step dialog window.
   * 
   * @return the (potentially new) name of the step
   */
  public String open() {
    final Shell parent = getParent();
    final Display display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN
        | SWT.MAX);
    props.setLook(shell);
    setShellImage(shell, this.input);

    final ModifyListener lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent arg0) {
        input.setChanged();
      }
    };
    backupChanged = input.hasChanged();

    final FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;
    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "Git.Shell.Title"));

    final int middle = props.getMiddlePct();
    final int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label(shell, SWT.RIGHT);
    wlStepname
        .setText(BaseMessages.getString(PKG, "System.Label.StepName"));
    props.setLook(wlStepname);
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment(0, 0);
    fdlStepname.right = new FormAttachment(middle, -margin);
    fdlStepname.top = new FormAttachment(0, margin);
    wlStepname.setLayoutData(fdlStepname);

    wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wStepname.setText(stepname);
    props.setLook(wStepname);
    wStepname.addModifyListener(lsMod);
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment(middle, 0);
    fdStepname.top = new FormAttachment(0, margin);
    fdStepname.right = new FormAttachment(100, 0);
    wStepname.setLayoutData(fdStepname);

    // ////////////////////////////
    // START OF OPERATIONS GROUP //
    // ////////////////////////////
    final Group gBasicPropertiesFields = new Group(shell, SWT.SHADOW_NONE);
    props.setLook(gBasicPropertiesFields);
    gBasicPropertiesFields.setText(BaseMessages.getString(PKG,
        "Git.gBasicPropertiesFields.Label"));
    final FormLayout flgOperationsFieldsLayout = new FormLayout();
    flgOperationsFieldsLayout.marginWidth = 10;
    flgOperationsFieldsLayout.marginHeight = 10;
    gBasicPropertiesFields.setLayout(flgOperationsFieldsLayout);

    // UserName field
    final Label wlUsernameField = new Label(gBasicPropertiesFields,
        SWT.RIGHT);
    wlUsernameField.setText(BaseMessages.getString(PKG,
        "Git.UserName.Label"));
    props.setLook(wlUsernameField);
    final FormData fdlUsernameField = new FormData();
    fdlUsernameField.left = new FormAttachment(0, -margin);
    fdlUsernameField.top = new FormAttachment(wStepname, margin);
    fdlUsernameField.right = new FormAttachment(middle, -2 * margin);
    wlUsernameField.setLayoutData(fdlUsernameField);

    wUsernameField = new TextVar(this.transMeta, gBasicPropertiesFields,
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wUsernameField.setEditable(true);
    props.setLook(wUsernameField);
    final FormData fdUsernameField = new FormData();
    fdUsernameField.left = new FormAttachment(middle, -margin);
    fdUsernameField.top = new FormAttachment(wStepname, margin);
    fdUsernameField.right = new FormAttachment(100, -margin);
    wUsernameField.setLayoutData(fdUsernameField);

    // Git repository password value
    wPasswordField = new LabelTextVar(transMeta, gBasicPropertiesFields,
        BaseMessages.getString(PKG, "Git.PasswordField.Label"),
        BaseMessages.getString(PKG, "Git.PasswordField.Tooltip"));
    props.setLook(wPasswordField);
    wPasswordField.setEchoChar('*');
    wPasswordField.addModifyListener(lsMod);
    final FormData fdCertificatePassword = new FormData();
    fdCertificatePassword.left = new FormAttachment(0, -margin);
    fdCertificatePassword.top = new FormAttachment(wUsernameField, margin);
    fdCertificatePassword.right = new FormAttachment(100, -margin);
    wPasswordField.setLayoutData(fdCertificatePassword);

    // OK, if the password contains a variable, we don't want to have the
    // password hidden...
    wPasswordField.getTextWidget().addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        checkPasswordVisible();
      }
    });

    // Git Repository URL
    final Label wlRepoUrlField = new Label(gBasicPropertiesFields,
        SWT.RIGHT);
    wlRepoUrlField.setText(BaseMessages.getString(PKG,
        "Git.RepoUrlField.Label"));
    props.setLook(wlRepoUrlField);
    final FormData fdlRepoUrlField = new FormData();
    fdlRepoUrlField.left = new FormAttachment(0, -margin);
    fdlRepoUrlField.top = new FormAttachment(wPasswordField, margin);
    fdlRepoUrlField.right = new FormAttachment(middle, -2 * margin);
    wlRepoUrlField.setLayoutData(fdlRepoUrlField);

    wRepoUrlField = new TextVar(transMeta, gBasicPropertiesFields,
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wRepoUrlField);
    wRepoUrlField.addModifyListener(lsMod);
    final FormData fdRepoUrlField = new FormData();
    fdRepoUrlField.left = new FormAttachment(middle, -margin);
    fdRepoUrlField.top = new FormAttachment(wPasswordField, margin);
    fdRepoUrlField.right = new FormAttachment(100, -margin);
    wRepoUrlField.setLayoutData(fdRepoUrlField);

    // Git clone repository folder
    final Label wlRepoFolderPathField = new Label(gBasicPropertiesFields,
        SWT.RIGHT);
    wlRepoFolderPathField.setText(BaseMessages.getString(PKG,
        "Git.RepoFolderPathField.Label"));
    props.setLook(wlRepoFolderPathField);
    final FormData fdlRepoFolderPathField = new FormData();
    fdlRepoFolderPathField.left = new FormAttachment(0, -margin);
    fdlRepoFolderPathField.top = new FormAttachment(wRepoUrlField, margin);
    fdlRepoFolderPathField.right = new FormAttachment(middle, -2 * margin);
    wlRepoFolderPathField.setLayoutData(fdlRepoFolderPathField);

    wRepoFolderPathField = new TextVar(transMeta, gBasicPropertiesFields,
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wRepoUrlField);
    wRepoFolderPathField.addModifyListener(lsMod);
    final FormData fdRepoFolderPathField = new FormData();
    fdRepoFolderPathField.left = new FormAttachment(middle, -margin);
    fdRepoFolderPathField.top = new FormAttachment(wRepoUrlField, margin);
    fdRepoFolderPathField.right = new FormAttachment(100, -margin);
    wRepoFolderPathField.setLayoutData(fdRepoFolderPathField);

    final FormData fdOperationsFields = new FormData();
    fdOperationsFields.left = new FormAttachment(0, margin);
    fdOperationsFields.top = new FormAttachment(wStepname, 2 * margin);
    fdOperationsFields.right = new FormAttachment(100, -margin);
    gBasicPropertiesFields.setLayoutData(fdOperationsFields);

    // ///////////////////////////
    // END OF OPERATIONS GROUP //
    // ///////////////////////////

    // OK and cancel buttons
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    BaseStepDialog.positionBottomButtons(shell,
        new Button[] {wOK, wCancel}, margin, null);

    final Button button = new Button(shell, SWT.PUSH);
    button.setImage(ImageUtil.getImage(display, getClass(),
        "ivyis_logo.png"));
    button.setToolTipText("Ivy Information Systems");
    button.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        final ShowBrowserDialog sbd =
            new ShowBrowserDialog(shell, BaseMessages.getString(PKG,
                "ExportCmdLine.CommandLine.Title"),
                "<html><script>window.location=\"http://www.ivy-is.co.uk\""
                    + "</script></html>");
        sbd.open();
      }
    });

    // Determine the largest button in the array
    Rectangle largest = null;
    button.pack(true);
    final Rectangle r = button.getBounds();
    if (largest == null || r.width > largest.width) {
      largest = r;
    }

    // Also, set the tooltip the same as the name if we don't have one...
    if (button.getToolTipText() == null) {
      button.setToolTipText(Const.replace(button.getText(), "&", ""));
    }

    // Make buttons a bit larger... (nicer)
    largest.width += 10;
    if ((largest.width % 2) == 1) {
      largest.width++;
    }

    BaseStepDialog.rightAlignButtons(new Button[] {button},
        largest.width, margin, null);
    if (Const.isOSX()) {
      final List<TableView> tableViews = new ArrayList<TableView>();
      getTableViews(shell, tableViews);
      button.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          for (TableView view : tableViews) {
            view.applyOSXChanges();
          }
        }
      });
    }

    // The git commands table
    final Label wlOperations = new Label(shell, SWT.NONE);
    wlOperations.setText(BaseMessages.getString(PKG,
        "Git.DefineCommands.Label"));
    props.setLook(wlOperations);
    final FormData fdlOperations = new FormData();
    fdlOperations.left = new FormAttachment(0, 0);
    fdlOperations.top = new FormAttachment(gBasicPropertiesFields, margin);
    wlOperations.setLayoutData(fdlOperations);

    final int tableCols = 2;
    final int upInsRows = input.getGitCommands() != null ? input
        .getGitCommands().size() : 1;

    ciOperations = new ColumnInfo[tableCols];
    ciOperations[0] = new ColumnInfo(BaseMessages.getString(PKG,
        "Git.ColumnInfo.CommandType.Label"),
        ColumnInfo.COLUMN_TYPE_CCOMBO,
        GitCommandType.getOperationTypesLabel(), false);
    ciOperations[1] = new ColumnInfo(BaseMessages.getString(PKG,
        "Git.ColumnInfo.Description.Label"),
        ColumnInfo.COLUMN_TYPE_TEXT, new String[] {""}, false);

    ciOperations[1].setSelectionAdapter(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {

        final String commandName = ((TableView) e.widget).getItem(e.y,
            e.x - 1);
        if ("".equals(commandName)) {
          return;
        }

        final TableView tv = (TableView) e.widget;
        GitCommand gitCommand = null;
        if (!"".equals(tv.getItem(e.y)[1])) {
          try {
            gitCommand = GitCommandFactory.getGitCommand(
                GitCommandType.fromLabel(commandName),
                tv.getItem(e.y)[1]);
          } catch (KettleException ex) {
            new ErrorDialog(
                shell,
                BaseMessages.getString(PKG,
                    "Git.GitCommandNotFount.DialogTitle"),
                BaseMessages.getString(PKG,
                    "Git.GitCommandNotFount.DialogMessage"),
                ex);
          }
        }
        final BaseGitCommandDialog gcd = GitCommandDialogFactory
            .getGitCommandDialog(shell, transMeta, gitCommand,
                GitCommandType.fromLabel(commandName));
        final GitCommand gc = gcd.open();

        if (gc != null) {
          tv.setText(gc.getDescription(), e.x, e.y);
          input.setChanged();
        }
      }
    });

    wGitCommands = new TableView(transMeta, shell, SWT.BORDER
        | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
        ciOperations, upInsRows, lsMod, props);

    final FormData fdGitCommandFields = new FormData();
    fdGitCommandFields.top = new FormAttachment(wlOperations, margin * 2);
    fdGitCommandFields.bottom = new FormAttachment(wOK, -margin * 2);
    fdGitCommandFields.left = new FormAttachment(0, 0);
    fdGitCommandFields.right = new FormAttachment(100, 0);
    wGitCommands.setLayoutData(fdGitCommandFields);

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent(Event e) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent(Event e) {
        ok();
      }
    };

    wCancel.addListener(SWT.Selection, lsCancel);
    wOK.addListener(SWT.Selection, lsOK);

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected(SelectionEvent e) {
        ok();
      }
    };
    wStepname.addSelectionListener(lsDef);

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    input.setChanged(backupChanged);

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    return stepname;
  }

  /**
   * Gets the table views.
   * 
   * @param parentControl the parent control
   * @param tableViews the table views
   * @return the table views
   */
  private static final void getTableViews(Control parentControl,
      List<TableView> tableViews) {
    if (parentControl instanceof TableView) {
      tableViews.add((TableView) parentControl);
    } else {
      if (parentControl instanceof Composite) {
        final Control[] children = ((Composite) parentControl)
            .getChildren();
        for (Control child : children) {
          getTableViews(child, tableViews);
        }
      } else {
        if (parentControl instanceof Shell) {
          final Control[] children = ((Shell) parentControl)
              .getChildren();
          for (Control child : children) {
            getTableViews(child, tableViews);
          }

        }
      }
    }
  }

  /**
   * Read data and place it in the dialog.
   */
  public void getData() {
    wStepname.selectAll();
    if (input.getUsername() != null) {
      wUsernameField.setText(input.getUsername());
    }
    if (input.getPassword() != null) {
      wPasswordField.setText(input.getPassword());
    }
    if (input.getGitRepoFolderPath() != null) {
      wRepoFolderPathField.setText(input.getGitRepoFolderPath());
    }
    if (input.getGitRepoUrl() != null) {
      wRepoUrlField.setText(input.getGitRepoUrl());
    }
    if (input.getGitCommands().size() > 0) {
      for (int i = 0; i < input.getGitCommands().size(); i++) {
        final TableItem item = wGitCommands.table.getItem(i);
        if (input.getGitCommands().get(i) != null) {
          item.setText(1, input.getGitCommands().get(i)
              .getCommandType().getOperationLabel());
          item.setText(2, input.getGitCommands().get(i)
              .getDescription());
        }
      }
    }
  }

  /**
   * Checks the password visible.
   */
  private void checkPasswordVisible() {
    final String password = wPasswordField.getText();
    final List<String> list = new ArrayList<String>();
    StringUtil.getUsedVariables(password, list, true);
    if (list.size() == 0) {
      wPasswordField.setEchoChar('*');
    } else {
      String variableName = null;
      if (password.startsWith(StringUtil.UNIX_OPEN)
          && password.endsWith(StringUtil.UNIX_CLOSE)) {
        variableName = password.substring(
            StringUtil.UNIX_OPEN.length(), password.length()
                - StringUtil.UNIX_CLOSE.length());
      }
      if (password.startsWith(StringUtil.WINDOWS_OPEN)
          && password.endsWith(StringUtil.WINDOWS_CLOSE)) {
        variableName = password.substring(
            StringUtil.WINDOWS_OPEN.length(), password.length()
                - StringUtil.WINDOWS_CLOSE.length());
      }
      if (variableName != null
          && System.getProperty(variableName) != null) {
        wPasswordField.setEchoChar('\0');
      } else {
        wPasswordField.setEchoChar('*');
      }
    }
  }

  /**
   * Cancel.
   */
  private void cancel() {
    stepname = null;
    input.setChanged(backupChanged);
    dispose();
  }

  /**
   * Let the plugin know about the entered data.
   */
  private void ok() {
    if (!Const.isEmpty(wStepname.getText())) {
      stepname = wStepname.getText();
      getInfo(input);
    }
  }

  /**
   * Get the information.
   * 
   * @param info the push notification step meta data.
   */
  public void getInfo(GitOperationsStepMeta info) {
    if (Const.isEmpty(wUsernameField.getText())
        || Const.isEmpty(wPasswordField.getText())) {
      final MessageBox mb = new MessageBox(shell, SWT.OK
          | SWT.ICON_WARNING);
      mb.setMessage(BaseMessages.getString(PKG,
          "Git.Username.Password.Mandatory.DialogMessage"));
      mb.setText(BaseMessages.getString(PKG,
          "System.Dialog.Warning.Title"));
      mb.open();
    }
    input.setUsername(wUsernameField.getText());
    input.setPassword(wPasswordField.getText());

    if (Const.isEmpty(wRepoFolderPathField.getText())) {
      final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(BaseMessages.getString(PKG,
          "Git.RepoFolder.Mandatory.DialogMessage"));
      mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
      mb.open();
      return;
    }
    input.setGitRepoFolderPath(wRepoFolderPathField.getText());

    if (Const.isEmpty(wRepoUrlField.getText())) {
      final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(BaseMessages.getString(PKG,
          "Git.RepoUrl.Mandatory.DialogMessage"));
      mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
      mb.open();
      return;
    }
    input.setGitRepoUrl(wRepoUrlField.getText());
    final ArrayList<GitCommand> commands = new ArrayList<GitCommand>();
    if (wGitCommands.getItemCount() > 0) {
      try {
        for (int i = 0; i < wGitCommands.getItemCount(); i++) {
          if (wGitCommands.getItem(i, 1) == null
              || GitCommandType.fromLabel(wGitCommands.getItem(i,
                  1)) == null) {
            continue;
          }
          final GitCommand gitCommand = GitCommandFactory
              .getGitCommand(GitCommandType
                  .fromLabel(wGitCommands.getItem(i, 1)),
                  wGitCommands.getItem(i, 2));

          if (gitCommand != null) {
            commands.add(gitCommand);
          }
        }
      } catch (KettleException e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG,
            "Git.GitCommandNotFount.DialogTitle"),
            BaseMessages.getString(PKG,
                "Git.GitCommandNotFount.DialogMessage"), e);
      }
    }
    if (commands.size() == 0) {
      final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(BaseMessages.getString(PKG,
          "Git.Min.Git.Commands.DialogMessage"));
      mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
      mb.open();
      return;
    }
    input.setGitCommands(commands);

    dispose();
  }
}
