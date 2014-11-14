package com.ivyis.di.trans.steps.git.command.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ivyis.di.trans.steps.git.GitCommandType;
import com.ivyis.di.trans.steps.git.command.GitCommand;
import com.ivyis.di.trans.steps.git.command.GitInfoCommand;
import com.ivyis.di.trans.steps.git.info.GitInfoStep;

/**
 * List branchs Git command implementation.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class ListBranchsGitCommand implements GitInfoCommand {
  private static final Class<?> PKG = ListBranchsGitCommand.class; // for i18n
  private static final Logger LOGGER = Logger.getLogger(PKG);
  private static final SimpleDateFormat dt = new SimpleDateFormat(
      GitCommand.DATE_FORMAT);

  private String listMode;

  // Fields name
  private String commitIdField;
  private String commitNameField;
  private String commitFullMessageField;
  private String commitShortMessageField;
  private String commitAuthorDateField;
  private String commitAuthorNameField;
  private String commitAuthorEmailField;
  private String commitCommiterDateField;
  private String commitCommiterNameField;
  private String commitCommiterEmailField;

  public ListBranchsGitCommand() {}

  public ListBranchsGitCommand(Map<String, String> configFields) {
    if (configFields == null) {
      throw new IllegalArgumentException("Unkomnown config fields.");
    }
    this.listMode = configFields.get(BaseMessages.getString(PKG,
        "Git.ListMode.Label"));
    this.commitIdField = configFields.get(BaseMessages.getString(PKG,
        "Git.IdField.Label"));
    this.commitNameField = configFields.get(BaseMessages.getString(PKG,
        "Git.NameField.Label"));
    this.commitFullMessageField = configFields.get(BaseMessages.getString(
        PKG, "Git.FullMessageField.Label"));
    this.commitShortMessageField = configFields.get(BaseMessages.getString(
        PKG, "Git.ShortMessageField.Label"));
    this.commitAuthorDateField = configFields.get(BaseMessages.getString(
        PKG, "Git.AuthorCreationDateField.Label"));
    this.commitAuthorNameField = configFields.get(BaseMessages.getString(
        PKG, "Git.AuthorNameField.Label"));
    this.commitAuthorEmailField = configFields.get(BaseMessages.getString(
        PKG, "Git.AuthorEmailField.Label"));
    this.commitCommiterDateField = configFields.get(BaseMessages.getString(
        PKG, "Git.CommitterCreationDateField.Label"));
    this.commitCommiterNameField = configFields.get(BaseMessages.getString(
        PKG, "Git.CommitterNameField.Label"));
    this.commitCommiterEmailField = configFields.get(BaseMessages
        .getString(PKG, "Git.CommitterEmailField.Label"));
  }

  public ListBranchsGitCommand(Node xml, String stepName)
      throws KettleException {
    try {
      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      XPathExpression xPathExpr = xpath.compile("//step[name='"
          + stepName + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/listMode/text()");
      this.listMode = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/commitIdField/text()");
      this.commitIdField = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/commitNameField/text()");
      this.commitNameField = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/commitFullMessageField/text()");
      this.commitFullMessageField = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/commitShortMessageField/text()");
      this.commitShortMessageField = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/commitAuthorDateField/text()");
      this.commitAuthorDateField = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/commitAuthorNameField/text()");
      this.commitAuthorNameField = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/commitAuthorEmailField/text()");
      this.commitAuthorEmailField = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/commitCommiterDateField/text()");
      this.commitCommiterDateField = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/commitCommiterNameField/text()");
      this.commitCommiterNameField = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/commitCommiterEmailField/text()");
      this.commitCommiterEmailField = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

    } catch (XPathExpressionException e) {
      throw new KettleException(e);
    }
  }

  public String getDescription() {
    return BaseMessages.getString(PKG, "Git.ListMode.Label")
        + ": "
        + listMode
        + BaseMessages.getString(PKG, "Git.IdField.Label")
        + ": "
        + commitIdField
        + " / "
        + BaseMessages.getString(PKG, "Git.NameField.Label")
        + ": "
        + commitNameField
        + " / "
        + BaseMessages.getString(PKG, "Git.FullMessageField.Label")
        + ": "
        + commitFullMessageField
        + " / "
        + BaseMessages.getString(PKG, "Git.ShortMessageField.Label")
        + ": "
        + commitShortMessageField
        + " / "
        + BaseMessages.getString(PKG,
            "Git.AuthorCreationDateField.Label")
        + ": "
        + commitAuthorDateField
        + " / "
        + BaseMessages.getString(PKG, "Git.AuthorNameField.Label")
        + ": "
        + commitAuthorNameField
        + " / "
        + BaseMessages.getString(PKG, "Git.AuthorEmailField.Label")
        + ": "
        + commitAuthorEmailField
        + " / "
        + BaseMessages.getString(PKG,
            "Git.CommitterCreationDateField.Label") + ": "
        + commitCommiterDateField + " / "
        + BaseMessages.getString(PKG, "Git.CommitterNameField.Label")
        + ": " + commitCommiterNameField
        + BaseMessages.getString(PKG, "Git.CommitterEmailField.Label")
        + ": " + commitCommiterEmailField;
  }

  public Map<String, String> getConfigFields(boolean mainConfigFields) {
    final HashMap<String, String> map = new HashMap<String, String>();
    map.put(BaseMessages.getString(PKG, "Git.ListMode.Label"), listMode);
    if (mainConfigFields) {
      return map;
    }
    map.put(BaseMessages.getString(PKG, "Git.IdField.Label"), commitIdField);
    map.put(BaseMessages.getString(PKG, "Git.NameField.Label"),
        commitNameField);
    map.put(BaseMessages.getString(PKG, "Git.FullMessageField.Label"),
        commitFullMessageField);
    map.put(BaseMessages.getString(PKG, "Git.ShortMessageField.Label"),
        commitShortMessageField);
    map.put(BaseMessages
        .getString(PKG, "Git.AuthorCreationDateField.Label"),
        commitAuthorDateField);
    map.put(BaseMessages.getString(PKG, "Git.AuthorNameField.Label"),
        commitAuthorNameField);
    map.put(BaseMessages.getString(PKG, "Git.AuthorEmailField.Label"),
        commitAuthorEmailField);
    map.put(BaseMessages.getString(PKG,
        "Git.CommitterCreationDateField.Label"),
        commitCommiterDateField);
    map.put(BaseMessages.getString(PKG, "Git.CommitterNameField.Label"),
        commitCommiterNameField);
    map.put(BaseMessages.getString(PKG, "Git.CommitterEmailField.Label"),
        commitCommiterEmailField);
    return map;
  }

  public GitCommandType getCommandType() {
    return GitCommandType.LIST_BRANCHS;
  }

  public String getXML() {
    try {
      final DocumentBuilderFactory dFact = DocumentBuilderFactory
          .newInstance();
      final DocumentBuilder build = dFact.newDocumentBuilder();
      final Document doc = build.newDocument();
      final Element root = doc.createElement(MAIN_NODE);
      doc.appendChild(root);
      final Element commandName = doc.createElement(COMMAND_NAME_NODE);
      commandName.appendChild(doc
          .createTextNode(GitCommandType.LIST_BRANCHS
              .getOperationName()));
      root.appendChild(commandName);

      final Element listModeNode = doc.createElement("listMode");
      listModeNode.appendChild(doc.createTextNode(listMode));
      root.appendChild(listModeNode);

      final Element commitIdFieldNode = doc
          .createElement("commitIdField");
      commitIdFieldNode.appendChild(doc.createTextNode(commitIdField));
      root.appendChild(commitIdFieldNode);

      final Element commitNameFieldNode = doc
          .createElement("commitNameField");
      commitNameFieldNode
          .appendChild(doc.createTextNode(commitNameField));
      root.appendChild(commitNameFieldNode);

      final Element commitFullMessageFieldNode = doc
          .createElement("commitFullMessageField");
      commitFullMessageFieldNode.appendChild(doc
          .createTextNode(commitFullMessageField));
      root.appendChild(commitFullMessageFieldNode);

      final Element commitShortMessageFieldNode = doc
          .createElement("commitShortMessageField");
      commitShortMessageFieldNode.appendChild(doc
          .createTextNode(commitShortMessageField));
      root.appendChild(commitShortMessageFieldNode);

      final Element commitAuthorDateFieldNode = doc
          .createElement("commitAuthorDateField");
      commitAuthorDateFieldNode.appendChild(doc
          .createTextNode(commitAuthorDateField));
      root.appendChild(commitAuthorDateFieldNode);

      final Element commitAuthorNameFieldNode = doc
          .createElement("commitAuthorNameField");
      commitAuthorNameFieldNode.appendChild(doc
          .createTextNode(commitAuthorNameField));
      root.appendChild(commitAuthorNameFieldNode);

      final Element commitAuthorEmailFieldNode = doc
          .createElement("commitAuthorEmailField");
      commitAuthorEmailFieldNode.appendChild(doc
          .createTextNode(commitAuthorEmailField));
      root.appendChild(commitAuthorEmailFieldNode);

      final Element commitCommiterDateFieldNode = doc
          .createElement("commitCommiterDateField");
      commitCommiterDateFieldNode.appendChild(doc
          .createTextNode(commitCommiterDateField));
      root.appendChild(commitCommiterDateFieldNode);

      final Element commitCommiterNameFieldNode = doc
          .createElement("commitCommiterNameField");
      commitCommiterNameFieldNode.appendChild(doc
          .createTextNode(commitCommiterNameField));
      root.appendChild(commitCommiterNameFieldNode);

      final Element commitCommiterEmailFieldNode = doc
          .createElement("commitCommiterEmailField");
      commitCommiterEmailFieldNode.appendChild(doc
          .createTextNode(commitCommiterEmailField));
      root.appendChild(commitCommiterEmailFieldNode);

      final Transformer tf = TransformerFactory.newInstance()
          .newTransformer();
      tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      tf.setOutputProperty(OutputKeys.INDENT, "yes");
      tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      final Writer out = new StringWriter();
      tf.transform(new DOMSource(doc), new StreamResult(out));
      return out.toString();

    } catch (ParserConfigurationException pce) {
      LOGGER.error(pce.getMessage(), pce);
      return "";
    } catch (TransformerException e) {
      LOGGER.error(e.getMessage(), e);
      return "";
    }
  }

  public List<String[]> call(final GitInfoStep gitInfoStep, Git git,
      CredentialsProvider cp, String gitRepoUrl, File gitRepoFolder)
      throws InvalidRemoteException, TransportException, GitAPIException,
      IllegalArgumentException, IOException {

    final List<String[]> branchs = new ArrayList<String[]>();

    final ListBranchCommand ac = git
        .branchList()
        .setListMode(
            ("remote".equalsIgnoreCase(gitInfoStep
                .environmentSubstitute(this.listMode)) ? ListMode.REMOTE
                : ListMode.ALL));
    final List<Ref> refBranchs = ac.call();

    for (Ref refBranch : refBranchs) {
      final String[] branch = new String[] {null, null, null, null,
          null, null, null, null, null, null};

      branch[0] = refBranch.getObjectId().getName(); // Id
      branch[1] = refBranch.getName(); // Name

      final RevObject object = new RevWalk(git.getRepository())
          .parseAny(refBranch.getObjectId());
      if (object instanceof RevCommit) {
        branch[2] = ((RevCommit) object).getFullMessage(); // Commit
        // message
        branch[3] = ((RevCommit) object).getShortMessage(); // Commit
        // message
        branch[4] = dt.format(((RevCommit) object).getAuthorIdent()
            .getWhen()); // Author Date
        branch[5] = ((RevCommit) object).getAuthorIdent().getName(); // Author
        // name
        branch[6] = ((RevCommit) object).getAuthorIdent()
            .getEmailAddress(); // Author email
        branch[7] = dt.format(((RevCommit) object).getCommitterIdent()
            .getWhen()); // Committer Date
        branch[8] = ((RevCommit) object).getCommitterIdent().getName(); // Committer
        // name
        branch[9] = ((RevCommit) object).getCommitterIdent()
            .getEmailAddress(); // Committer email
      }

      branchs.add(branch);
    }

    return branchs;
  }

  public boolean isConfigurable() {
    return true;
  }

  public String getListMode() {
    return listMode;
  }

  public void setListMode(String listMode) {
    this.listMode = listMode;
  }

  public String getCommitIdField() {
    return commitIdField;
  }

  public void setCommitIdField(String commitIdField) {
    this.commitIdField = commitIdField;
  }

  public String getCommitNameField() {
    return commitNameField;
  }

  public void setCommitNameField(String commitNameField) {
    this.commitNameField = commitNameField;
  }

  public String getCommitFullMessageField() {
    return commitFullMessageField;
  }

  public void setCommitFullMessageField(String commitFullMessageField) {
    this.commitFullMessageField = commitFullMessageField;
  }

  public String getCommitShortMessageField() {
    return commitShortMessageField;
  }

  public void setCommitShortMessageField(String commitShortMessageField) {
    this.commitShortMessageField = commitShortMessageField;
  }

  public String getCommitAuthorDateField() {
    return commitAuthorDateField;
  }

  public void setCommitAuthorDateField(String commitAuthorDateField) {
    this.commitAuthorDateField = commitAuthorDateField;
  }

  public String getCommitAuthorNameField() {
    return commitAuthorNameField;
  }

  public void setCommitAuthorNameField(String commitAuthorNameField) {
    this.commitAuthorNameField = commitAuthorNameField;
  }

  public String getCommitAuthorEmailField() {
    return commitAuthorEmailField;
  }

  public void setCommitAuthorEmailField(String commitAuthorEmailField) {
    this.commitAuthorEmailField = commitAuthorEmailField;
  }

  public String getCommitCommiterDateField() {
    return commitCommiterDateField;
  }

  public void setCommitCommiterDateField(String commitCommiterDateField) {
    this.commitCommiterDateField = commitCommiterDateField;
  }

  public String getCommitCommiterNameField() {
    return commitCommiterNameField;
  }

  public void setCommitCommiterNameField(String commitCommiterNameField) {
    this.commitCommiterNameField = commitCommiterNameField;
  }

  public String getCommitCommiterEmailField() {
    return commitCommiterEmailField;
  }

  public void setCommitCommiterEmailField(String commitCommiterEmailField) {
    this.commitCommiterEmailField = commitCommiterEmailField;
  }

}
