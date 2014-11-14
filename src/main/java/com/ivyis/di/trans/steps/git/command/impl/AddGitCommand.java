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
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ivyis.di.trans.steps.git.GitCommandType;
import com.ivyis.di.trans.steps.git.command.GitCommand;
import com.ivyis.di.trans.steps.git.command.GitOperationsCommand;
import com.ivyis.di.trans.steps.git.operations.GitOperationsStep;

/**
 * Add Git command implementation.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class AddGitCommand implements GitOperationsCommand {
  private static final Class<?> PKG = AddGitCommand.class; // for i18n
  private static final Logger LOGGER = Logger.getLogger(PKG);

  private String filepattern;
  private boolean update;

  public AddGitCommand() {}

  public AddGitCommand(String description) {
    if ("".equals(description)) {
      throw new IllegalArgumentException("Unkomnown config fields.");
    }
    final String[] stringProps = description.split(" / ");
    for (String stringProp : stringProps) {
      if (BaseMessages.getString(PKG, "Git.FilePattern.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.filepattern = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.Update.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.update = Boolean.parseBoolean(stringProp.split(":")[1]
            .trim());
      }
    }
  }

  public AddGitCommand(Node xml, String stepName) throws KettleException {
    try {
      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      XPathExpression xPathExpr = xpath.compile("//step[name='"
          + stepName + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/filepattern/text()");
      this.filepattern = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/update/text()");
      this.update = Boolean.parseBoolean((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING));

    } catch (XPathExpressionException e) {
      throw new KettleException(e);
    }
  }

  public String getDescription() {
    return BaseMessages.getString(PKG, "Git.FilePattern.Label") + ": "
        + (filepattern == null ? "" : filepattern) + " / "
        + BaseMessages.getString(PKG, "Git.Update.Label") + ": "
        + update;
  }

  public GitCommandType getCommandType() {
    return GitCommandType.ADD;
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
      commandName.appendChild(doc.createTextNode(GitCommandType.ADD
          .getOperationName()));
      root.appendChild(commandName);

      final Element filepatternNode = doc.createElement("filepattern");
      filepatternNode.appendChild(doc.createTextNode(filepattern));
      root.appendChild(filepatternNode);

      final Element updateNode = doc.createElement("update");
      updateNode.appendChild(doc.createTextNode(String.valueOf(update)));
      root.appendChild(updateNode);

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
      NoFilepatternException, GitAPIException {

    AddCommand ac = git.add();

    if (!Const.isEmpty(this.filepattern)) {
      ac = ac.addFilepattern(gitOperationsStep
          .environmentSubstitute(this.filepattern));
    }

    ac.setUpdate(update).call();
    return git;
  }

  public String getFilepattern() {
    return filepattern;
  }

  public void setFilepattern(String filepattern) {
    this.filepattern = filepattern;
  }

  public boolean isUpdate() {
    return update;
  }

  public void setUpdate(boolean update) {
    this.update = update;
  }

}
