package com.ivyis.di.trans.steps.git.command.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

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
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ivyis.di.trans.steps.git.GitCommandType;
import com.ivyis.di.trans.steps.git.command.GitCommand;
import com.ivyis.di.trans.steps.git.command.GitOperationsCommand;
import com.ivyis.di.trans.steps.git.operations.GitOperationsStep;

/**
 * Commit Git command implementation.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class CommitGitCommand implements GitOperationsCommand {
  private static final Class<?> PKG = CommitGitCommand.class; // for i18n
  private static final Logger LOGGER = Logger.getLogger(PKG);

  private String authorName;
  private String authorEmail;
  private String committerName;
  private String committerEmail;
  private String commitMessage;
  private boolean amend;
  private boolean insertChangeId;
  private boolean all;

  public CommitGitCommand() {}

  public CommitGitCommand(String description) {
    if ("".equals(description)) {
      throw new IllegalArgumentException("Unkomnown config fields.");
    }
    final String[] stringProps = description.split(" / ");
    for (String stringProp : stringProps) {
      final String prop = stringProp.split(":")[0].trim();
      if (BaseMessages.getString(PKG, "Git.AuthorName.Label")
          .equals(prop)) {
        this.authorName = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.AuthorEmail.Label")
          .equals(prop)) {
        this.authorEmail = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.CommitterName.Label")
          .equals(prop)) {
        this.committerName = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.CommitterEmail.Label")
          .equals(prop)) {
        this.committerEmail = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.CommitMessage.Label")
          .equals(prop)) {
        this.commitMessage = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.Amend.Label").equals(
          prop)) {
        this.amend = Boolean.parseBoolean(stringProp.split(":")[1]
            .trim());
      } else if (BaseMessages.getString(PKG, "Git.InsertChangeId.Label")
          .equals(prop)) {
        this.insertChangeId = Boolean.parseBoolean(stringProp
            .split(":")[1].trim());
      } else if (BaseMessages.getString(PKG, "Git.All.Label")
          .equals(prop)) {
        this.all = Boolean
            .parseBoolean(stringProp.split(":")[1].trim());
      }
    }
  }

  public CommitGitCommand(Node xml, String stepName) {
    try {
      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      XPathExpression xPathExpr = xpath.compile("//step[name='"
          + stepName + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/authorName/text()");
      this.authorName = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/authorEmail/text()");
      this.authorEmail = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/committerName/text()");
      this.committerName = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/committerEmail/text()");
      this.committerEmail = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/commitMessage/text()");
      this.commitMessage = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/amend/text()");
      this.amend = Boolean.parseBoolean((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING));

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/insertChangeId/text()");
      this.insertChangeId = Boolean.parseBoolean((String) xPathExpr
          .evaluate(xml, XPathConstants.STRING));

      xPathExpr = xpath
          .compile("//step[name='" + stepName + "']/gitCommands//"
              + GitCommand.MAIN_NODE + "/all/text()");
      this.all = Boolean.parseBoolean((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING));

    } catch (XPathExpressionException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  public String getDescription() {
    return BaseMessages.getString(PKG, "Git.AuthorName.Label") + ": "
        + (authorName == null ? "" : authorName) + " / "
        + BaseMessages.getString(PKG, "Git.AuthorEmail.Label") + ": "
        + (authorEmail == null ? "" : authorEmail) + " / "
        + BaseMessages.getString(PKG, "Git.CommitterName.Label") + ": "
        + (committerName == null ? "" : committerName) + " / "
        + BaseMessages.getString(PKG, "Git.CommitterEmail.Label")
        + ": " + (committerEmail == null ? "" : committerEmail) + " / "
        + BaseMessages.getString(PKG, "Git.CommitMessage.Label") + ": "
        + (commitMessage == null ? "" : commitMessage) + " / "
        + BaseMessages.getString(PKG, "Git.Amend.Label") + ": " + amend
        + " / "
        + BaseMessages.getString(PKG, "Git.InsertChangeId.Label")
        + ": " + insertChangeId + " / "
        + BaseMessages.getString(PKG, "Git.All.Label") + ": " + all
        + " / ";
  }

  public GitCommandType getCommandType() {
    return GitCommandType.COMMIT;
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
      commandName.appendChild(doc.createTextNode(GitCommandType.COMMIT
          .getOperationName()));
      root.appendChild(commandName);

      final Element authorNameNode = doc.createElement("authorName");
      authorNameNode.appendChild(doc.createTextNode(authorName));
      root.appendChild(authorNameNode);

      final Element authorEmailNode = doc.createElement("authorEmail");
      authorEmailNode.appendChild(doc.createTextNode(authorEmail));
      root.appendChild(authorEmailNode);

      final Element committerNameNode = doc
          .createElement("committerName");
      committerNameNode.appendChild(doc.createTextNode(committerName));
      root.appendChild(committerNameNode);

      final Element committerEmailNode = doc
          .createElement("committerEmail");
      committerEmailNode.appendChild(doc.createTextNode(committerEmail));
      root.appendChild(committerEmailNode);

      final Element commitMessageNode = doc
          .createElement("commitMessage");
      commitMessageNode.appendChild(doc.createTextNode(commitMessage));
      root.appendChild(commitMessageNode);

      final Element amendNode = doc.createElement("amend");
      amendNode.appendChild(doc.createTextNode(String.valueOf(amend)));
      root.appendChild(amendNode);

      final Element insertChangeIdNode = doc
          .createElement("insertChangeId");
      insertChangeIdNode.appendChild(doc.createTextNode(String
          .valueOf(insertChangeId)));
      root.appendChild(insertChangeIdNode);

      final Element allNode = doc.createElement("all");
      allNode.appendChild(doc.createTextNode(String.valueOf(all)));
      root.appendChild(allNode);

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

  public Git call(final GitOperationsStep gitOperationsStep, Git git,
      CredentialsProvider cp, String gitRepoUrl, File gitRepoFolder)
      throws IllegalArgumentException, IOException, NoHeadException,
      NoMessageException, UnmergedPathsException,
      ConcurrentRefUpdateException, WrongRepositoryStateException,
      GitAPIException {
    CommitCommand cc = git
        .commit()
        .setAuthor(
            gitOperationsStep
                .environmentSubstitute(this.authorName == null ? ""
                    : this.authorName),
            gitOperationsStep
                .environmentSubstitute(this.authorEmail == null ? ""
                    : this.authorEmail))
        .setCommitter(
            gitOperationsStep
                .environmentSubstitute(this.committerName == null ? ""
                    : this.committerName),
            gitOperationsStep
                .environmentSubstitute(this.committerEmail == null ? ""
                    : this.committerName));

    if (!Const.isEmpty(this.commitMessage)) {
      cc = cc.setMessage(gitOperationsStep
          .environmentSubstitute(this.commitMessage));
    }
    cc.setAll(all).setInsertChangeId(insertChangeId).setAmend(amend).call();
    return git;
  }

  public String getAuthorName() {
    return authorName;
  }

  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  public String getAuthorEmail() {
    return authorEmail;
  }

  public void setAuthorEmail(String authorEmail) {
    this.authorEmail = authorEmail;
  }

  public String getCommitterName() {
    return committerName;
  }

  public void setCommitterName(String committerName) {
    this.committerName = committerName;
  }

  public String getCommitterEmail() {
    return committerEmail;
  }

  public void setCommitterEmail(String committerEmail) {
    this.committerEmail = committerEmail;
  }

  public String getCommitMessage() {
    return commitMessage;
  }

  public void setCommitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
  }

  public boolean isAmend() {
    return amend;
  }

  public void setAmend(boolean amend) {
    this.amend = amend;
  }

  public boolean isInsertChangeId() {
    return insertChangeId;
  }

  public void setInsertChangeId(boolean insertChangeId) {
    this.insertChangeId = insertChangeId;
  }

  public boolean isAll() {
    return all;
  }

  public void setAll(boolean all) {
    this.all = all;
  }

}
