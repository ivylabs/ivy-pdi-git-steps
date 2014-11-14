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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidTagNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
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
 * Tag Git command implementation.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class TagGitCommand implements GitOperationsCommand {
  private static final Class<?> PKG = TagGitCommand.class; // for i18n
  private static final Logger LOGGER = Logger.getLogger(PKG);

  private String message;
  private String name;
  private boolean annotated;
  private boolean forceUpdate;
  private boolean signed;

  public TagGitCommand() {}

  public TagGitCommand(String description) {
    if ("".equals(description)) {
      throw new IllegalArgumentException("Unkomnown config fields.");
    }
    final String[] stringProps = description.split(" / ");
    for (String stringProp : stringProps) {
      if (BaseMessages.getString(PKG, "Git.Message.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.message = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.Name.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.name = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.Annotated.Label")
          .equals(stringProp.split(":")[0].trim())) {
        this.annotated = Boolean.parseBoolean(stringProp.split(":")[1]
            .trim());
      } else if (BaseMessages.getString(PKG, "Git.ForceUpdate.Label")
          .equals(stringProp.split(":")[0].trim())) {
        this.forceUpdate = Boolean
            .parseBoolean(stringProp.split(":")[1].trim());
      } else if (BaseMessages.getString(PKG, "Git.Signed.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.signed = Boolean.parseBoolean(stringProp.split(":")[1]
            .trim());
      }
    }
  }

  public TagGitCommand(Node xml, String stepName) {
    try {
      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      XPathExpression xPathExpr = xpath.compile("//step[name='"
          + stepName + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/message/text()");
      this.message = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/name/text()");
      this.name = (String) xPathExpr.evaluate(xml, XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/annotated/text()");
      this.annotated = Boolean.parseBoolean((String) xPathExpr.evaluate(
          xml, XPathConstants.STRING));

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/forceUpdate/text()");
      this.forceUpdate = Boolean.parseBoolean((String) xPathExpr
          .evaluate(xml, XPathConstants.STRING));

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/signed/text()");
      this.signed = Boolean.parseBoolean((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING));

    } catch (XPathExpressionException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  public String getDescription() {
    return BaseMessages.getString(PKG, "Git.Message.Label") + ": "
        + (message == null ? "" : message) + " / "
        + BaseMessages.getString(PKG, "Git.Name.Label") + ": "
        + (name == null ? "" : name) + " / "
        + BaseMessages.getString(PKG, "Git.Annotated.Label") + ": "
        + annotated + " / "
        + BaseMessages.getString(PKG, "Git.ForceUpdate.Label") + ": "
        + forceUpdate + " / "
        + BaseMessages.getString(PKG, "Git.Signed.Label") + ": "
        + signed;
  }

  public GitCommandType getCommandType() {
    return GitCommandType.TAG;
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
      commandName.appendChild(doc.createTextNode(GitCommandType.TAG
          .getOperationName()));
      root.appendChild(commandName);

      final Element messageNode = doc.createElement("message");
      messageNode.appendChild(doc.createTextNode(message));
      root.appendChild(messageNode);

      final Element nameNode = doc.createElement("name");
      nameNode.appendChild(doc.createTextNode(name));
      root.appendChild(nameNode);

      final Element annotatedNode = doc.createElement("annotated");
      annotatedNode.appendChild(doc.createTextNode(String
          .valueOf(annotated)));
      root.appendChild(annotatedNode);

      final Element forceUpdateNode = doc.createElement("forceUpdate");
      forceUpdateNode.appendChild(doc.createTextNode(String
          .valueOf(forceUpdate)));
      root.appendChild(forceUpdateNode);

      final Element signedNode = doc.createElement("signed");
      signedNode.appendChild(doc.createTextNode(String.valueOf(signed)));
      root.appendChild(signedNode);

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
      throws IllegalArgumentException, IOException,
      ConcurrentRefUpdateException, InvalidTagNameException,
      NoHeadException, GitAPIException {
    TagCommand tc = git.tag().setAnnotated(annotated)
        .setForceUpdate(forceUpdate).setSigned(signed);
    if (!Const.isEmpty(this.message)) {
      tc = tc.setMessage(gitOperationsStep
          .environmentSubstitute(this.message));
    }
    if (!Const.isEmpty(this.name)) {
      tc = tc.setName(gitOperationsStep.environmentSubstitute(this.name));
    }
    tc.call();
    return git;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isAnnotated() {
    return annotated;
  }

  public void setAnnotated(boolean annotated) {
    this.annotated = annotated;
  }

  public boolean isForceUpdate() {
    return forceUpdate;
  }

  public void setForceUpdate(boolean forceUpdate) {
    this.forceUpdate = forceUpdate;
  }

  public boolean isSigned() {
    return signed;
  }

  public void setSigned(boolean signed) {
    this.signed = signed;
  }

}
