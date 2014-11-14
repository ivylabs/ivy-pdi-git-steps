package com.ivyis.di.trans.steps.git.command.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.pentaho.di.core.Const;
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
 * Log Git command implementation.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class LogGitCommand implements GitInfoCommand {
  private static final Class<?> PKG = LogGitCommand.class; // for i18n
  private static final Logger LOGGER = Logger.getLogger(PKG);
  private static final SimpleDateFormat dt = new SimpleDateFormat(
      GitCommand.DATE_FORMAT);

  // Properties
  private String path;
  private String maxCount;
  private String skip;

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

  public LogGitCommand() {}

  public LogGitCommand(Map<String, String> configFields) {
    if (configFields == null) {
      throw new IllegalArgumentException("Unkomnown config fields.");
    }
    this.path = configFields.get(BaseMessages.getString(PKG,
        "Git.Path.Label"));
    this.maxCount = configFields.get(BaseMessages.getString(PKG,
        "Git.MaxCount.Label"));
    this.skip = configFields.get(BaseMessages.getString(PKG,
        "Git.Skip.Label"));
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

  public LogGitCommand(Node xml, String stepName) throws KettleException {
    try {
      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      XPathExpression xPathExpr = xpath.compile("//step[name='"
          + stepName + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/path/text()");
      if (!"".equals((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING))) {
        this.path = (String) xPathExpr.evaluate(xml,
            XPathConstants.STRING);
      }

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "//maxCount/text()");
      if (!"".equals((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING))) {
        this.maxCount = (String) xPathExpr.evaluate(xml,
            XPathConstants.STRING);
      }

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/skip/text()");
      if (!"".equals((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING))) {
        this.skip = (String) xPathExpr.evaluate(xml,
            XPathConstants.STRING);
      }

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
    return BaseMessages.getString(PKG, "Git.Path.Label")
        + ": "
        + path
        + " / "
        + BaseMessages.getString(PKG, "Git.MaxCount.Label")
        + ": "
        + maxCount
        + " / "
        + BaseMessages.getString(PKG, "Git.Skip.Label")
        + ": "
        + skip
        + " / "
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
    map.put(BaseMessages.getString(PKG, "Git.Path.Label"), path);
    map.put(BaseMessages.getString(PKG, "Git.MaxCount.Label"),
        maxCount == null ? null : maxCount.toString());
    map.put(BaseMessages.getString(PKG, "Git.Skip.Label"),
        skip == null ? null : skip.toString());
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
    return GitCommandType.LOG;
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
      commandName.appendChild(doc.createTextNode(GitCommandType.LOG
          .getOperationName()));
      root.appendChild(commandName);
      if (path != null) {
        final Element pathNode = doc.createElement("path");
        pathNode.appendChild(doc.createTextNode(path));
        root.appendChild(pathNode);
      }
      if (maxCount != null) {
        final Element maxCountNode = doc.createElement("maxCount");
        maxCountNode
            .appendChild(doc.createTextNode(maxCount.toString()));
        root.appendChild(maxCountNode);
      }
      if (skip != null) {
        final Element skipNode = doc.createElement("skip");
        skipNode.appendChild(doc.createTextNode(skip.toString()));
        root.appendChild(skipNode);
      }

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

    final RevWalk walk = new RevWalk(git.getRepository());
    LogCommand lc = git.log();

    if (!Const.isEmpty(this.path)) {
      lc = lc.addPath(gitInfoStep.environmentSubstitute(this.path));
    }

    if (this.maxCount != null) {
      lc = lc.setMaxCount(Integer.parseInt(gitInfoStep
          .environmentSubstitute(this.maxCount)));
    }

    if (this.skip != null) {
      lc = lc.setSkip(Integer.parseInt(gitInfoStep
          .environmentSubstitute(this.skip)));
    }

    final Iterable<RevCommit> logs = lc.call();
    final Iterator<RevCommit> i = logs.iterator();

    final List<String[]> commits = new ArrayList<String[]>();

    while (i.hasNext()) {
      final String[] commitRow = new String[] {null, null, null, null,
          null, null, null, null, null, null};
      final RevCommit commit = walk.parseCommit(i.next());
      commitRow[0] = commit.getId().getName();
      commitRow[1] = commit.getName();
      commitRow[2] = commit.getFullMessage();
      commitRow[3] = commit.getShortMessage();
      commitRow[4] = dt.format(commit.getAuthorIdent().getWhen());
      commitRow[5] = commit.getAuthorIdent().getName();
      commitRow[6] = commit.getAuthorIdent().getEmailAddress();
      commitRow[7] = dt.format(commit.getCommitterIdent().getWhen());
      commitRow[8] = commit.getCommitterIdent().getName();
      commitRow[9] = commit.getCommitterIdent().getEmailAddress();

      commits.add(commitRow);

    }

    return commits;
  }

  public boolean isConfigurable() {
    return true;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getMaxCount() {
    return maxCount;
  }

  public void setMaxCount(String maxCount) {
    this.maxCount = maxCount;
  }

  public String getSkip() {
    return skip;
  }

  public void setSkip(String skip) {
    this.skip = skip;
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
