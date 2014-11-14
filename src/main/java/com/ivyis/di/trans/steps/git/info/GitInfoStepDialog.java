package com.ivyis.di.trans.steps.git.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.pentaho.di.core.Props;
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
import com.ivyis.di.trans.steps.git.command.GitCommandFactory;
import com.ivyis.di.trans.steps.git.command.GitInfoCommand;
import com.ivyis.di.trans.steps.git.command.impl.CurrentBranchGitCommand;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;
import com.ivyis.di.trans.steps.git.dialog.GitCommandDialogFactory;

/**
 * This class is responsible for the UI in Spoon of Git information step.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class GitInfoStepDialog extends BaseStepDialog implements
    StepDialogInterface {

  /** for i18n purposes. **/
  private static final Class<?> PKG = GitInfoStepDialog.class;
  private static final Logger LOGGER = Logger.getLogger(PKG);

  private GitInfoStepMeta input;
  private Text wIdField, wNameField, wShortMessageField, wFullMessageField,
      wAuthorCreationDateField, wAuthorNameField, wAuthorEmailField,
      wCommitterCreationDateField, wCommitterNameField,
      wCommitterEmailField;
  private TextVar wUsernameField, wRepoUrlField, wRepoFolderPathField;
  private LabelTextVar wPasswordField;
  private CCombo wGitInfoCommandField;
  private Map<String, String> configFields = new HashMap<String, String>();
  private ColumnInfo[] ciFields;
  private TableView wFields;
  private Button wGetFields;
  private CTabFolder wTabFolder;
  private CTabItem wMainOptionsTab;
  private GitInfoCommand gitCommand;

  public GitInfoStepDialog(Shell parent, BaseStepMeta in,
      TransMeta transMeta, String sname) {
    super(parent, in, transMeta, sname);
    this.input = (GitInfoStepMeta) in;
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

    final ScrolledComposite sc = new ScrolledComposite(shell, SWT.H_SCROLL
        | SWT.V_SCROLL);

    wTabFolder = new CTabFolder(sc, SWT.BORDER);
    props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

    // /////////////////////////////////
    // START OF Main Options TAB //
    // /////////////////////////////////
    wMainOptionsTab = new CTabItem(wTabFolder, SWT.NONE);
    wMainOptionsTab.setText(BaseMessages.getString(PKG,
        "Git.MainOptionTab.CTabItem.Title"));

    final FormLayout mainOptionsLayout = new FormLayout();
    mainOptionsLayout.marginWidth = 3;
    mainOptionsLayout.marginHeight = 3;

    final Composite wMainOptionsComp = new Composite(wTabFolder, SWT.NONE);
    props.setLook(wMainOptionsComp);
    wMainOptionsComp.setLayout(mainOptionsLayout);

    // ////////////////////////////
    // START OF OPERATIONS GROUP //
    // ////////////////////////////
    final Group gBasicPropertiesFields = new Group(wMainOptionsComp,
        SWT.SHADOW_NONE);
    props.setLook(gBasicPropertiesFields);
    gBasicPropertiesFields.setText(BaseMessages.getString(PKG,
        "Git.gBasicPropertiesFields.Label"));
    final FormLayout flgFieldNamesLayout = new FormLayout();
    flgFieldNamesLayout.marginWidth = 10;
    flgFieldNamesLayout.marginHeight = 10;
    gBasicPropertiesFields.setLayout(flgFieldNamesLayout);

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

    // //////////////////////////
    // END OF OPERATIONS GROUP //
    // //////////////////////////

    // Retrying in case of unavailability
    final Label wlGitInfoCommandField = new Label(wMainOptionsComp,
        SWT.RIGHT);
    wlGitInfoCommandField.setText(BaseMessages.getString(PKG,
        "Git.GitInfoCommands.Label"));
    props.setLook(wlGitInfoCommandField);
    final FormData fdlGitInfoCommandField = new FormData();
    fdlGitInfoCommandField.left = new FormAttachment(0, -margin);
    fdlGitInfoCommandField.top = new FormAttachment(gBasicPropertiesFields,
        margin);
    fdlGitInfoCommandField.right = new FormAttachment(middle, -2 * margin);
    wlGitInfoCommandField.setLayoutData(fdlGitInfoCommandField);

    wGitInfoCommandField = new CCombo(wMainOptionsComp, SWT.BORDER
        | SWT.READ_ONLY);
    wGitInfoCommandField.setEditable(true);
    props.setLook(wGitInfoCommandField);
    final FormData fdGitInfoCommandField = new FormData();
    fdGitInfoCommandField.left = new FormAttachment(middle, -margin);
    fdGitInfoCommandField.top = new FormAttachment(gBasicPropertiesFields,
        margin);
    fdGitInfoCommandField.right = new FormAttachment(100, -margin);
    wGitInfoCommandField.setLayoutData(fdGitInfoCommandField);
    wGitInfoCommandField.setItems(GitCommandType.getInfoTypesLabel());
    wGitInfoCommandField.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        final String commandName = wGitInfoCommandField.getText();
        if (GitCommandType.CURRENT_BRANCH.equals(GitCommandType
            .fromLabel(commandName))) {
          try {
            gitCommand = GitCommandFactory.getGitCommand(
                GitCommandType.fromLabel(commandName),
                configFields);
          } catch (KettleException e1) {
            LOGGER.error(e1.getMessage(), e1);
          }
          enableOutputFields(false);
        } else {
          enableOutputFields(true);
        }
      }
    });

    // The fields table
    final Label wlFields = new Label(wMainOptionsComp, SWT.NONE);
    wlFields.setText(BaseMessages.getString(PKG, "Git.ConfigFields.Label"));
    props.setLook(wlFields);
    final FormData fdlUpIns = new FormData();
    fdlUpIns.left = new FormAttachment(0, 0);
    fdlUpIns.top = new FormAttachment(wGitInfoCommandField, margin);
    wlFields.setLayoutData(fdlUpIns);

    final int tableCols = 2;
    final int upInsRows = configFields != null ? configFields.size() : 1;

    ciFields = new ColumnInfo[tableCols];
    ciFields[0] = new ColumnInfo(BaseMessages.getString(PKG,
        "Git.ColumnInfo.FieldName"), ColumnInfo.COLUMN_TYPE_TEXT,
        new String[] {""}, true);
    ciFields[1] = new ColumnInfo(BaseMessages.getString(PKG,
        "Git.ColumnInfo.FieldValue"), ColumnInfo.COLUMN_TYPE_TEXT,
        new String[] {""}, true);
    wFields = new TableView(transMeta, wMainOptionsComp, SWT.BORDER
        | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
        ciFields, upInsRows, lsMod, props);

    wGetFields = new Button(wMainOptionsComp, SWT.PUSH);
    wGetFields.setText(BaseMessages.getString(PKG,
        "Git.DefineFields.Button"));
    final FormData fdGetFields = new FormData();
    fdGetFields.top = new FormAttachment(wlFields, margin);
    fdGetFields.right = new FormAttachment(100, 0);
    wGetFields.setLayoutData(fdGetFields);

    wGetFields.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        final String commandName = wGitInfoCommandField.getText();
        if ("".equals(commandName)) {
          wFields.clearAll();
          return;
        }

        try {
          gitCommand = GitCommandFactory.getGitCommand(
              GitCommandType.fromLabel(commandName), configFields);
        } catch (KettleException ex) {
          new ErrorDialog(shell, BaseMessages.getString(PKG,
              "Git.GitCommandNotFount.DialogTitle"), BaseMessages
              .getString(PKG,
                  "Git.GitCommandNotFount.DialogMessage"), ex);
        }
        if (gitCommand.isConfigurable() != BaseGitCommandDialog.NO_CONFIGURATION) {
          final BaseGitCommandDialog gcd = GitCommandDialogFactory
              .getGitCommandDialog(shell, transMeta, gitCommand,
                  GitCommandType.fromLabel(commandName));
          gitCommand = (GitInfoCommand) gcd.open();

          if (gitCommand != null) {
            wFields.clearAll();
            boolean first = true;
            for (Map.Entry<String, String> entry : gitCommand
                .getConfigFields(true).entrySet()) {
              configFields.put(entry.getKey(), entry.getValue());
              if (entry.getKey() == null
                  || entry.getValue() == null) {
                continue;
              }
              if (first) {
                wFields.setText(entry.getKey(), 1, 0);
                wFields.setText(entry.getValue(), 2, 0);
                first = false;
              } else {
                final TableItem tableItem = new TableItem(
                    wFields.table, SWT.NONE);
                tableItem.setText(1, entry.getKey());
                tableItem.setText(2, entry.getValue());
              }
            }
            input.setChanged();
          } else {
            wFields.clearAll();
          }
        } else {
          final MessageBox mb = new MessageBox(shell, SWT.OK
              | SWT.ICON_INFORMATION);
          mb.setMessage(BaseMessages.getString(PKG,
              "Git.Info.Nothing.Conf.DialogMessage"));
          mb.setText(BaseMessages.getString(PKG,
              "System.Dialog.Info.Title"));
          mb.open();
          wFields.clearAll();
        }
      }
    });

    final FormData fdFields = new FormData();
    fdFields.left = new FormAttachment(0, 0);
    fdFields.top = new FormAttachment(wlFields, margin);
    fdFields.right = new FormAttachment(wGetFields, -margin);
    fdFields.bottom = new FormAttachment(wlFields, 100);
    wFields.setLayoutData(fdFields);

    // ////////////////////////
    // START OF FIELDS GROUP //
    // ////////////////////////
    final Group gFieldNames = new Group(wMainOptionsComp, SWT.SHADOW_NONE);
    props.setLook(gFieldNames);
    gFieldNames.setText(BaseMessages.getString(PKG,
        "Git.OutputFieldNames.Label"));
    final FormLayout flgOperationsFieldsLayout = new FormLayout();
    flgOperationsFieldsLayout.marginWidth = 10;
    flgOperationsFieldsLayout.marginHeight = 10;
    gFieldNames.setLayout(flgOperationsFieldsLayout);

    // Id field
    final Label wlIdField = new Label(gFieldNames, SWT.RIGHT);
    wlIdField.setText(BaseMessages.getString(PKG, "Git.IdField.Label"));
    props.setLook(wlIdField);
    final FormData fdlIdField = new FormData();
    fdlIdField.left = new FormAttachment(0, -margin);
    fdlIdField.top = new FormAttachment(wFields, margin);
    fdlIdField.right = new FormAttachment(middle, -2 * margin);
    wlIdField.setLayoutData(fdlIdField);

    wIdField = new Text(gFieldNames, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wIdField.setEditable(true);
    wIdField.setText("Id");
    props.setLook(wIdField);
    final FormData fdIdField = new FormData();
    fdIdField.left = new FormAttachment(middle, -margin);
    fdIdField.top = new FormAttachment(wFields, margin);
    fdIdField.right = new FormAttachment(100, -margin);
    wIdField.setLayoutData(fdIdField);

    // Name field
    final Label wlNameField = new Label(gFieldNames, SWT.RIGHT);
    wlNameField.setText(BaseMessages.getString(PKG, "Git.NameField.Label"));
    props.setLook(wlNameField);
    final FormData fdlNameField = new FormData();
    fdlNameField.left = new FormAttachment(0, -margin);
    fdlNameField.top = new FormAttachment(wIdField, margin);
    fdlNameField.right = new FormAttachment(middle, -2 * margin);
    wlNameField.setLayoutData(fdlNameField);

    wNameField = new Text(gFieldNames, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wNameField.setEditable(true);
    wNameField.setText("Name");
    props.setLook(wNameField);
    final FormData fdNameField = new FormData();
    fdNameField.left = new FormAttachment(middle, -margin);
    fdNameField.top = new FormAttachment(wIdField, margin);
    fdNameField.right = new FormAttachment(100, -margin);
    wNameField.setLayoutData(fdNameField);

    // Full message field
    final Label wlFullMessageField = new Label(gFieldNames, SWT.RIGHT);
    wlFullMessageField.setText(BaseMessages.getString(PKG,
        "Git.FullMessageField.Label"));
    props.setLook(wlFullMessageField);
    final FormData fdlFullMessageField = new FormData();
    fdlFullMessageField.left = new FormAttachment(0, -margin);
    fdlFullMessageField.top = new FormAttachment(wNameField, margin);
    fdlFullMessageField.right = new FormAttachment(middle, -2 * margin);
    wlFullMessageField.setLayoutData(fdlFullMessageField);

    wFullMessageField = new Text(gFieldNames, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    wFullMessageField.setEditable(true);
    wFullMessageField.setText("Full message");
    props.setLook(wFullMessageField);
    final FormData fdFullMessageField = new FormData();
    fdFullMessageField.left = new FormAttachment(middle, -margin);
    fdFullMessageField.top = new FormAttachment(wNameField, margin);
    fdFullMessageField.right = new FormAttachment(100, -margin);
    wFullMessageField.setLayoutData(fdFullMessageField);

    // Short message field
    final Label wlShortMessageField = new Label(gFieldNames, SWT.RIGHT);
    wlShortMessageField.setText(BaseMessages.getString(PKG,
        "Git.ShortMessageField.Label"));
    props.setLook(wlShortMessageField);
    final FormData fdlShortMessageField = new FormData();
    fdlShortMessageField.left = new FormAttachment(0, -margin);
    fdlShortMessageField.top = new FormAttachment(wFullMessageField, margin);
    fdlShortMessageField.right = new FormAttachment(middle, -2 * margin);
    wlShortMessageField.setLayoutData(fdlShortMessageField);

    wShortMessageField = new Text(gFieldNames, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    wShortMessageField.setEditable(true);
    wShortMessageField.setText("Short message");
    props.setLook(wShortMessageField);
    final FormData fdShortMessageField = new FormData();
    fdShortMessageField.left = new FormAttachment(middle, -margin);
    fdShortMessageField.top = new FormAttachment(wFullMessageField, margin);
    fdShortMessageField.right = new FormAttachment(100, -margin);
    wShortMessageField.setLayoutData(fdShortMessageField);

    // Author creation date field
    final Label wlAuthorCreationDateField = new Label(gFieldNames,
        SWT.RIGHT);
    wlAuthorCreationDateField.setText(BaseMessages.getString(PKG,
        "Git.AuthorCreationDateField.Label"));
    props.setLook(wlAuthorCreationDateField);
    final FormData fdlAuthorCreationDateField = new FormData();
    fdlAuthorCreationDateField.left = new FormAttachment(0, -margin);
    fdlAuthorCreationDateField.top = new FormAttachment(wShortMessageField,
        margin);
    fdlAuthorCreationDateField.right = new FormAttachment(middle, -2
        * margin);
    wlAuthorCreationDateField.setLayoutData(fdlAuthorCreationDateField);

    wAuthorCreationDateField = new Text(gFieldNames, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    wAuthorCreationDateField.setEditable(true);
    wAuthorCreationDateField.setText("Author creation date");
    props.setLook(wAuthorCreationDateField);
    final FormData fdAuthorCreationDateField = new FormData();
    fdAuthorCreationDateField.left = new FormAttachment(middle, -margin);
    fdAuthorCreationDateField.top = new FormAttachment(wShortMessageField,
        margin);
    fdAuthorCreationDateField.right = new FormAttachment(100, -margin);
    wAuthorCreationDateField.setLayoutData(fdAuthorCreationDateField);

    // Author name field
    final Label wlAuthorNameField = new Label(gFieldNames, SWT.RIGHT);
    wlAuthorNameField.setText(BaseMessages.getString(PKG,
        "Git.AuthorNameField.Label"));
    props.setLook(wlAuthorNameField);
    final FormData fdlAuthorNameField = new FormData();
    fdlAuthorNameField.left = new FormAttachment(0, -margin);
    fdlAuthorNameField.top = new FormAttachment(wAuthorCreationDateField,
        margin);
    fdlAuthorNameField.right = new FormAttachment(middle, -2 * margin);
    wlAuthorNameField.setLayoutData(fdlAuthorNameField);

    wAuthorNameField = new Text(gFieldNames, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    wAuthorNameField.setEditable(true);
    wAuthorNameField.setText("Author name");
    props.setLook(wAuthorNameField);
    final FormData fdAuthorNameField = new FormData();
    fdAuthorNameField.left = new FormAttachment(middle, -margin);
    fdAuthorNameField.top = new FormAttachment(wAuthorCreationDateField,
        margin);
    fdAuthorNameField.right = new FormAttachment(100, -margin);
    wAuthorNameField.setLayoutData(fdAuthorNameField);

    // Author email field
    final Label wlAuthorEmailField = new Label(gFieldNames, SWT.RIGHT);
    wlAuthorEmailField.setText(BaseMessages.getString(PKG,
        "Git.AuthorEmailField.Label"));
    props.setLook(wlAuthorEmailField);
    final FormData fdlAuthorEmailField = new FormData();
    fdlAuthorEmailField.left = new FormAttachment(0, -margin);
    fdlAuthorEmailField.top = new FormAttachment(wAuthorNameField, margin);
    fdlAuthorEmailField.right = new FormAttachment(middle, -2 * margin);
    wlAuthorEmailField.setLayoutData(fdlAuthorEmailField);

    wAuthorEmailField = new Text(gFieldNames, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    wAuthorEmailField.setEditable(true);
    wAuthorEmailField.setText("Author email");
    props.setLook(wAuthorEmailField);
    final FormData fdAuthorEmailField = new FormData();
    fdAuthorEmailField.left = new FormAttachment(middle, -margin);
    fdAuthorEmailField.top = new FormAttachment(wAuthorNameField, margin);
    fdAuthorEmailField.right = new FormAttachment(100, -margin);
    wAuthorEmailField.setLayoutData(fdAuthorEmailField);

    // Committer creation date field
    final Label wlCommitterCreationDateField = new Label(gFieldNames,
        SWT.RIGHT);
    wlCommitterCreationDateField.setText(BaseMessages.getString(PKG,
        "Git.CommitterCreationDateField.Label"));
    props.setLook(wlCommitterCreationDateField);
    final FormData fdlCommitterCreationDateField = new FormData();
    fdlCommitterCreationDateField.left = new FormAttachment(0, -margin);
    fdlCommitterCreationDateField.top = new FormAttachment(
        wAuthorEmailField, margin);
    fdlCommitterCreationDateField.right = new FormAttachment(middle, -2
        * margin);
    wlCommitterCreationDateField
        .setLayoutData(fdlCommitterCreationDateField);

    wCommitterCreationDateField = new Text(gFieldNames, SWT.SINGLE
        | SWT.LEFT | SWT.BORDER);
    wCommitterCreationDateField.setEditable(true);
    wCommitterCreationDateField.setText("Committer creation date");
    props.setLook(wCommitterCreationDateField);
    final FormData fdCommitterCreationDateField = new FormData();
    fdCommitterCreationDateField.left = new FormAttachment(middle, -margin);
    fdCommitterCreationDateField.top = new FormAttachment(
        wAuthorEmailField, margin);
    fdCommitterCreationDateField.right = new FormAttachment(100, -margin);
    wCommitterCreationDateField.setLayoutData(fdCommitterCreationDateField);

    // Committer name field
    final Label wlCommitterNameField = new Label(gFieldNames, SWT.RIGHT);
    wlCommitterNameField.setText(BaseMessages.getString(PKG,
        "Git.CommitterNameField.Label"));
    props.setLook(wlCommitterNameField);
    final FormData fdlCommitterNameField = new FormData();
    fdlCommitterNameField.left = new FormAttachment(0, -margin);
    fdlCommitterNameField.top = new FormAttachment(
        wCommitterCreationDateField, margin);
    fdlCommitterNameField.right = new FormAttachment(middle, -2 * margin);
    wlCommitterNameField.setLayoutData(fdlCommitterNameField);

    wCommitterNameField = new Text(gFieldNames, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    wCommitterNameField.setEditable(true);
    wCommitterNameField.setText("Committer name");
    props.setLook(wCommitterNameField);
    final FormData fdCommitterNameField = new FormData();
    fdCommitterNameField.left = new FormAttachment(middle, -margin);
    fdCommitterNameField.top = new FormAttachment(
        wCommitterCreationDateField, margin);
    fdCommitterNameField.right = new FormAttachment(100, -margin);
    wCommitterNameField.setLayoutData(fdCommitterNameField);

    // Committer email field
    final Label wlCommitterEmailField = new Label(gFieldNames, SWT.RIGHT);
    wlCommitterEmailField.setText(BaseMessages.getString(PKG,
        "Git.CommitterEmailField.Label"));
    props.setLook(wlCommitterEmailField);
    final FormData fdlCommitterEmailField = new FormData();
    fdlCommitterEmailField.left = new FormAttachment(0, -margin);
    fdlCommitterEmailField.top = new FormAttachment(wCommitterNameField,
        margin);
    fdlCommitterEmailField.right = new FormAttachment(middle, -2 * margin);
    wlCommitterEmailField.setLayoutData(fdlCommitterEmailField);

    wCommitterEmailField = new Text(gFieldNames, SWT.SINGLE | SWT.LEFT
        | SWT.BORDER);
    wCommitterEmailField.setEditable(true);
    wCommitterEmailField.setText("Committer email");
    props.setLook(wCommitterEmailField);
    final FormData fdCommitterEmailField = new FormData();
    fdCommitterEmailField.left = new FormAttachment(middle, -margin);
    fdCommitterEmailField.top = new FormAttachment(wCommitterNameField,
        margin);
    fdCommitterEmailField.right = new FormAttachment(100, -margin);
    wCommitterEmailField.setLayoutData(fdCommitterEmailField);

    final FormData fdFieldNames = new FormData();
    fdFieldNames.left = new FormAttachment(0, margin);
    fdFieldNames.top = new FormAttachment(wFields, 2 * margin);
    fdFieldNames.right = new FormAttachment(100, -margin);
    gFieldNames.setLayoutData(fdFieldNames);

    // //////////////////////
    // END OF FIELDS GROUP //
    // //////////////////////
    final FormData fdMainOptions = new FormData();
    fdMainOptions.left = new FormAttachment(0, 0);
    fdMainOptions.top = new FormAttachment(0, 0);
    fdMainOptions.right = new FormAttachment(100, 0);
    fdMainOptions.bottom = new FormAttachment(100, 0);
    wMainOptionsComp.setLayoutData(fdMainOptions);

    wMainOptionsComp.layout();
    wMainOptionsTab.setControl(wMainOptionsComp);
    // /////////////////////////////////
    // END OF Main Options TAB //
    // /////////////////////////////////

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

    final FormData fdMainOptionsTab = new FormData();
    fdMainOptionsTab.left = new FormAttachment(0, 0);
    fdMainOptionsTab.top = new FormAttachment(wStepname, margin);
    fdMainOptionsTab.right = new FormAttachment(100, 0);
    fdMainOptionsTab.bottom = new FormAttachment(wOK, -margin);
    wTabFolder.setLayoutData(fdMainOptionsTab);

    wTabFolder.setSelection(0);

    final FormData fdSc = new FormData();
    fdSc.left = new FormAttachment(0, 0);
    fdSc.top = new FormAttachment(wStepname, margin);
    fdSc.right = new FormAttachment(100, 0);
    fdSc.bottom = new FormAttachment(100, -50);
    sc.setLayoutData(fdSc);

    sc.setContent(wTabFolder);

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

    // determine scrollable area
    sc.setMinSize(wTabFolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    sc.setExpandHorizontal(true);
    sc.setExpandVertical(true);

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

  private void enableOutputFields(boolean enable) {
    wIdField.setEnabled(enable);
    wFullMessageField.setEnabled(enable);
    wShortMessageField.setEnabled(enable);
    wAuthorCreationDateField.setEnabled(enable);
    wAuthorNameField.setEnabled(enable);
    wAuthorEmailField.setEnabled(enable);
    wCommitterCreationDateField.setEnabled(enable);
    wCommitterNameField.setEnabled(enable);
    wCommitterEmailField.setEnabled(enable);
    input.setChanged(backupChanged);
  }

  /**
   * Read data and place it in the dialog.
   */
  public void getData() {
    wStepname.selectAll();
    if (input.getUsername() != null) {
      wUsernameField.setText(Const.NVL(input.getUsername(), ""));
    }
    if (input.getPassword() != null) {
      wPasswordField.setText(Const.NVL(input.getPassword(), ""));
    }
    if (input.getGitRepoFolderPath() != null) {
      wRepoFolderPathField.setText(input.getGitRepoFolderPath());
    }
    if (input.getGitRepoUrl() != null) {
      wRepoUrlField.setText(input.getGitRepoUrl());
    }
    if (input.getGitCommand() != null) {
      gitCommand = (GitInfoCommand) input.getGitCommand();

      wGitInfoCommandField.setText(BaseMessages.getString(
          GitCommandType.class, "Git."
              + gitCommand.getCommandType().getOperationName()
              + ".Label"));
      if (GitCommandType.CURRENT_BRANCH.equals(gitCommand
          .getCommandType())) {
        enableOutputFields(false);
      } else {
        enableOutputFields(true);
      }

      wFields.clearAll();
      boolean first = true;
      for (Map.Entry<String, String> entry : gitCommand.getConfigFields(
          true).entrySet()) {
        configFields.put(entry.getKey(), entry.getValue());
        if (entry.getKey() == null || entry.getValue() == null) {
          continue;
        }
        if (first) {
          wFields.setText(entry.getKey(), 1, 0);
          wFields.setText(entry.getValue(), 2, 0);
          first = false;
        } else {
          final TableItem tableItem = new TableItem(wFields.table,
              SWT.NONE);
          tableItem.setText(1, entry.getKey());
          tableItem.setText(2, entry.getValue());
        }
      }

      wIdField.setText(Const.NVL(
          gitCommand.getConfigFields(false).get(
              BaseMessages.getString(PKG, "Git.IdField.Label")),
          ""));
      wNameField
          .setText(Const.NVL(
              gitCommand.getConfigFields(false).get(
                  BaseMessages.getString(PKG,
                      "Git.NameField.Label")), ""));
      wFullMessageField.setText(Const.NVL(
          gitCommand.getConfigFields(false).get(
              BaseMessages.getString(PKG,
                  "Git.FullMessageField.Label")), ""));
      wShortMessageField.setText(Const.NVL(
          gitCommand.getConfigFields(false).get(
              BaseMessages.getString(PKG,
                  "Git.ShortMessageField.Label")), ""));
      wAuthorCreationDateField.setText(Const.NVL(
          gitCommand.getConfigFields(false).get(
              BaseMessages.getString(PKG,
                  "Git.AuthorCreationDateField.Label")), ""));
      wAuthorNameField.setText(Const.NVL(
          gitCommand.getConfigFields(false).get(
              BaseMessages.getString(PKG,
                  "Git.AuthorNameField.Label")), ""));
      wAuthorEmailField.setText(Const.NVL(
          gitCommand.getConfigFields(false).get(
              BaseMessages.getString(PKG,
                  "Git.AuthorEmailField.Label")), ""));
      wCommitterCreationDateField.setText(Const.NVL(
          gitCommand.getConfigFields(false).get(
              BaseMessages.getString(PKG,
                  "Git.CommitterCreationDateField.Label")),
          ""));
      wCommitterNameField.setText(Const.NVL(
          gitCommand.getConfigFields(false).get(
              BaseMessages.getString(PKG,
                  "Git.CommitterNameField.Label")), ""));
      wCommitterEmailField.setText(Const.NVL(
          gitCommand.getConfigFields(false).get(
              BaseMessages.getString(PKG,
                  "Git.CommitterEmailField.Label")), ""));
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
  public void getInfo(GitInfoStepMeta info) {
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

    final Map<String, String> config = gitCommand.getConfigFields(true);
    if (gitCommand instanceof CurrentBranchGitCommand) {
      if (Const.isEmpty(wNameField.getText())) {
        final MessageBox mb = new MessageBox(shell, SWT.OK
            | SWT.ICON_ERROR);
        mb.setMessage(BaseMessages.getString(PKG,
            "Git.ConfigFields.Mandatory.DialogMessage"));
        mb.setText(BaseMessages.getString(PKG,
            "System.Dialog.Error.Title"));
        mb.open();
        return;
      }
      config.put(BaseMessages.getString(PKG, "Git.NameField.Label"),
          wNameField.getText());

    } else if (Const.isEmpty(wIdField.getText())
        || Const.isEmpty(wNameField.getText())
        || Const.isEmpty(wFullMessageField.getText())
        || Const.isEmpty(wShortMessageField.getText())
        || Const.isEmpty(wAuthorCreationDateField.getText())
        || Const.isEmpty(wAuthorNameField.getText())
        || Const.isEmpty(wAuthorEmailField.getText())
        || Const.isEmpty(wCommitterCreationDateField.getText())
        || Const.isEmpty(wCommitterNameField.getText())
        || Const.isEmpty(wCommitterEmailField.getText())) {
      final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(BaseMessages.getString(PKG,
          "Git.ConfigFields.Mandatory.DialogMessage"));
      mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
      mb.open();
      return;
    } else {
      config.put(BaseMessages.getString(PKG, "Git.IdField.Label"),
          wIdField.getText());
      config.put(BaseMessages.getString(PKG, "Git.NameField.Label"),
          wNameField.getText());
      config.put(
          BaseMessages.getString(PKG, "Git.FullMessageField.Label"),
          wFullMessageField.getText());
      config.put(
          BaseMessages.getString(PKG, "Git.ShortMessageField.Label"),
          wShortMessageField.getText());
      config.put(BaseMessages.getString(PKG,
          "Git.AuthorCreationDateField.Label"),
          wAuthorCreationDateField.getText());
      config.put(
          BaseMessages.getString(PKG, "Git.AuthorNameField.Label"),
          wAuthorNameField.getText());
      config.put(
          BaseMessages.getString(PKG, "Git.AuthorEmailField.Label"),
          wAuthorEmailField.getText());
      config.put(BaseMessages.getString(PKG,
          "Git.CommitterCreationDateField.Label"),
          wCommitterCreationDateField.getText());
      config.put(
          BaseMessages.getString(PKG, "Git.CommitterNameField.Label"),
          wCommitterNameField.getText());
      config.put(BaseMessages.getString(PKG,
          "Git.CommitterEmailField.Label"), wCommitterEmailField
          .getText());
    }
    try {
      gitCommand = GitCommandFactory.getGitCommand(
          gitCommand.getCommandType(), config);
    } catch (KettleException e) {
      final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(BaseMessages.getString(PKG,
          "Git.Command.Not.Found.DialogMessage"));
      mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
      mb.open();
      return;
    }
    input.setGitCommand(gitCommand);

    dispose();
  }
}
