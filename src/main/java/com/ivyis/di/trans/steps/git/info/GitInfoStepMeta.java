package com.ivyis.di.trans.steps.git.info;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

import com.ivyis.di.trans.steps.git.GitCommandType;
import com.ivyis.di.trans.steps.git.command.GitCommand;
import com.ivyis.di.trans.steps.git.command.GitCommandFactory;
import com.ivyis.di.trans.steps.git.command.GitInfoCommand;

/**
 * This class is responsible for implementing functionality regarding step meta. All Kettle steps
 * have an extension of this where private fields have been added with public accessors.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
@Step(id = "GitInfoStep", name = "GitInfo.Step.Name", description = "GitInfo.Step.Description",
    categoryDescription = "GitInfo.Step.Category",
    image = "com/ivyis/di/trans/steps/git/info/GitInfoStep.png",
    i18nPackageName = "com.ivyis.di.trans.steps.git.info", casesUrl = "https://github.com/ivylabs",
    documentationUrl = "https://github.com/ivylabs", forumUrl = "https://github.com/ivylabs")
public class GitInfoStepMeta extends BaseStepMeta implements StepMetaInterface {

  /** for i18n purposes. **/
  private static final Class<?> PKG = GitInfoStepMeta.class;

  private String username, password, gitRepoUrl, gitRepoFolderPath;
  private GitCommand gitCommand;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getGitRepoUrl() {
    return gitRepoUrl;
  }

  public void setGitRepoUrl(String gitRepoUrl) {
    this.gitRepoUrl = gitRepoUrl;
  }

  public String getGitRepoFolderPath() {
    return gitRepoFolderPath;
  }

  public void setGitRepoFolderPath(String gitRepoFolderPath) {
    this.gitRepoFolderPath = gitRepoFolderPath;
  }

  public GitCommand getGitCommand() {
    return gitCommand;
  }

  public void setGitCommand(GitCommand gitCommand) {
    this.gitCommand = gitCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getXML() {
    final StringBuilder retval = new StringBuilder();
    retval.append("    " + XMLHandler.addTagValue("username", username));
    retval.append("    " + XMLHandler.addTagValue("password", password));
    retval.append("    " + XMLHandler.addTagValue("gitRepoUrl", gitRepoUrl));
    retval.append("    "
        + XMLHandler
            .addTagValue("gitRepoFolderPath", gitRepoFolderPath));

    retval.append("    <gitCommands>").append(Const.CR);
    retval.append("        <gitCommand>").append(Const.CR);
    retval.append("          ").append(gitCommand.getXML());
    retval.append("        </gitCommand>").append(Const.CR);
    retval.append("    </gitCommands>").append(Const.CR);

    return retval.toString();
  }

  /**
   * Reads data from XML transformation file.
   * 
   * @param stepnode the step XML node.
   * @throws KettleXMLException the kettle XML exception.
   */
  public void readData(Node stepnode) throws KettleXMLException {
    try {
      username = XMLHandler.getTagValue(stepnode, "username");
      password = XMLHandler.getTagValue(stepnode, "password");
      gitRepoUrl = XMLHandler.getTagValue(stepnode, "gitRepoUrl");
      gitRepoFolderPath = XMLHandler.getTagValue(stepnode,
          "gitRepoFolderPath");
      final String stepName = XMLHandler.getTagValue(stepnode, "name");

      // Git commands
      final Node gitCommands = XMLHandler.getSubNode(stepnode,
          "gitCommands");
      final int nrCustomFieldsRows = XMLHandler.countNodes(gitCommands,
          "gitCommand");
      for (int i = 0; i < nrCustomFieldsRows; i++) {
        final Node knode = XMLHandler.getSubNodeByNr(gitCommands,
            "gitCommand", i);
        this.gitCommand = GitCommandFactory.getGitCommand(knode,
            stepName);
      }

    } catch (Exception e) {
      throw new KettleXMLException(BaseMessages.getString(PKG,
          "ImageResize.Exception.UnexpectedErrorInReadingStepInfo"),
          e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @throws KettleException
   */
  @Override
  public void readRep(Repository rep, ObjectId idStep,
      List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleException {
    try {
      username = rep.getStepAttributeString(idStep, "username");
      password = rep.getStepAttributeString(idStep, "password");
      gitRepoUrl = rep.getStepAttributeString(idStep, "gitRepoUrl");
      gitRepoFolderPath = rep.getStepAttributeString(idStep,
          "gitRepoFolderPath");
      final String stepName = rep.getStepAttributeString(idStep, "name");

      final int nrRows = rep.countNrStepAttributes(idStep, "gitCommands");
      for (int i = 0; i < nrRows; i++) {
        gitCommand = GitCommandFactory.getGitCommand(
            rep.getStepAttributeString(idStep, i, "gitCommand"),
            stepName);
      }

    } catch (Exception e) {
      throw new KettleException(BaseMessages.getString(PKG,
          "Git.Exception.UnexpectedErrorInReadingStepInfo"), e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @throws KettleException
   */
  @Override
  public void saveRep(Repository rep, ObjectId idTransformation,
      ObjectId idStep) throws KettleException {
    try {
      rep.saveStepAttribute(idTransformation, idStep, "username",
          username);
      rep.saveStepAttribute(idTransformation, idStep, "password",
          password);
      rep.saveStepAttribute(idTransformation, idStep, "gitRepoUrl",
          gitRepoUrl);
      rep.saveStepAttribute(idTransformation, idStep,
          "gitRepoFolderPath", gitRepoFolderPath);
      rep.saveStepAttribute(idTransformation, idStep, 0, "gitCommand",
          gitCommand.getXML());
    } catch (Exception e) {
      throw new KettleException(BaseMessages.getString(PKG,
          "Git.Exception.UnableToSaveStepInfoToRepository") + idStep,
          e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void getFields(RowMetaInterface r, String origin,
      RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {
    // Just add the response field...
    if (gitCommand != null) {
      final Map<String, String> configFields = ((GitInfoCommand) gitCommand)
          .getConfigFields(false);
      if (GitCommandType.CURRENT_BRANCH.equals(gitCommand
          .getCommandType())) {
        final ValueMetaInterface nameField = new ValueMeta(
            space.environmentSubstitute(configFields
                .get(BaseMessages.getString(PKG,
                    "Git.NameField.Label"))),
            ValueMetaInterface.TYPE_STRING);
        nameField.setOrigin(origin);
        r.addValueMeta(nameField);
        return;
      }

      final ValueMetaInterface idField = new ValueMeta(
          space.environmentSubstitute(configFields.get(BaseMessages
              .getString(PKG, "Git.IdField.Label"))),
          ValueMetaInterface.TYPE_STRING);
      idField.setOrigin(origin);
      r.addValueMeta(idField);

      final ValueMetaInterface nameField = new ValueMeta(
          space.environmentSubstitute(configFields.get(BaseMessages
              .getString(PKG, "Git.NameField.Label"))),
          ValueMetaInterface.TYPE_STRING);
      nameField.setOrigin(origin);
      r.addValueMeta(nameField);

      final ValueMetaInterface fullMessageField = new ValueMeta(
          space.environmentSubstitute(configFields.get(BaseMessages
              .getString(PKG, "Git.FullMessageField.Label"))),
          ValueMetaInterface.TYPE_STRING);
      fullMessageField.setOrigin(origin);
      r.addValueMeta(fullMessageField);

      final ValueMetaInterface shortMessageField = new ValueMeta(
          space.environmentSubstitute(configFields.get(BaseMessages
              .getString(PKG, "Git.ShortMessageField.Label"))),
          ValueMetaInterface.TYPE_STRING);
      shortMessageField.setOrigin(origin);
      r.addValueMeta(shortMessageField);

      final ValueMetaInterface authorCreationDateField = new ValueMeta(
          space.environmentSubstitute(configFields.get(BaseMessages
              .getString(PKG, "Git.AuthorCreationDateField.Label"))),
          ValueMetaInterface.TYPE_STRING);
      authorCreationDateField.setOrigin(origin);
      r.addValueMeta(authorCreationDateField);

      final ValueMetaInterface authorNameField = new ValueMeta(
          space.environmentSubstitute(configFields.get(BaseMessages
              .getString(PKG, "Git.AuthorNameField.Label"))),
          ValueMetaInterface.TYPE_STRING);
      authorNameField.setOrigin(origin);
      r.addValueMeta(authorNameField);

      final ValueMetaInterface authorEmailField = new ValueMeta(
          space.environmentSubstitute(configFields.get(BaseMessages
              .getString(PKG, "Git.AuthorEmailField.Label"))),
          ValueMetaInterface.TYPE_STRING);
      authorEmailField.setOrigin(origin);
      r.addValueMeta(authorEmailField);

      final ValueMetaInterface committerCreationDateField = new ValueMeta(
          space.environmentSubstitute(configFields.get(BaseMessages
              .getString(PKG,
                  "Git.CommitterCreationDateField.Label"))),
          ValueMetaInterface.TYPE_STRING);
      committerCreationDateField.setOrigin(origin);
      r.addValueMeta(committerCreationDateField);

      final ValueMetaInterface committerNameField = new ValueMeta(
          space.environmentSubstitute(configFields.get(BaseMessages
              .getString(PKG, "Git.CommitterNameField.Label"))),
          ValueMetaInterface.TYPE_STRING);
      committerNameField.setOrigin(origin);
      r.addValueMeta(committerNameField);

      final ValueMetaInterface committerEmailField = new ValueMeta(
          space.environmentSubstitute(configFields.get(BaseMessages
              .getString(PKG, "Git.CommitterEmailField.Label"))),
          ValueMetaInterface.TYPE_STRING);
      committerEmailField.setOrigin(origin);
      r.addValueMeta(committerEmailField);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object clone() {
    return super.clone();
  }

  /**
   * {@inheritDoc}
   * 
   * @throws KettleXMLException
   */
  @Override
  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleXMLException {
    readData(stepnode);
  }

  /**
   * Sets the default values.
   */
  public void setDefault() {}

  /**
   * {@inheritDoc}
   */
  @Override
  public void check(List<CheckResultInterface> remarks, TransMeta transmeta,
      StepMeta stepMeta, RowMetaInterface prev, String[] input,
      String[] output, RowMetaInterface info) {}

  /**
   * Get the Step dialog, needs for configure the step.
   * 
   * @param shell the shell.
   * @param meta the associated base step metadata.
   * @param transMeta the associated transformation metadata.
   * @param name the step name
   * @return The appropriate StepDialogInterface class.
   */
  public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta,
      TransMeta transMeta, String name) {
    return new GitInfoStepDialog(shell, (BaseStepMeta) meta, transMeta,
        name);
  }

  /**
   * Get the executing step, needed by Trans to launch a step.
   * 
   * @param stepMeta The step info.
   * @param stepDataInterface the step data interface linked to this step. Here the step can store
   *        temporary data, database connections, etc.
   * @param cnr The copy nr to get.
   * @param transMeta The transformation info.
   * @param disp The launching transformation.
   * @return The appropriate StepInterface class.
   */
  public StepInterface getStep(StepMeta stepMeta,
      StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans disp) {
    return new GitInfoStep(stepMeta, stepDataInterface, cnr, transMeta,
        disp);
  }

  /**
   * Get a new instance of the appropriate data class. This data class implements the
   * StepDataInterface. It basically contains the persisting data that needs to live on, even if a
   * worker thread is terminated.
   * 
   * @return The appropriate StepDataInterface class.
   */
  public StepDataInterface getStepData() {
    return new GitInfoStepData();
  }

}
