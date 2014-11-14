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
import org.eclipse.jgit.api.CheckoutCommand;
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
 * Checkout Git command implementation.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class CheckoutGitCommand implements GitOperationsCommand {
  private static final Class<?> PKG = CheckoutGitCommand.class; // for i18n
  private static final Logger LOGGER = Logger.getLogger(PKG);

  private String path;
  private String name;
  private String startPoint;
  private boolean allPaths;
  private boolean force;

  public CheckoutGitCommand() {}

  public CheckoutGitCommand(String description) {
    if ("".equals(description)) {
      throw new IllegalArgumentException("Unkomnown config fields.");
    }
    final String[] stringProps = description.split(" / ");
    for (String stringProp : stringProps) {
      if (BaseMessages.getString(PKG, "Git.Path.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.path = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.Name.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.name = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.StartPoint.Label")
          .equals(stringProp.split(":")[0].trim())) {
        this.startPoint = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.AllPaths.Label")
          .equals(stringProp.split(":")[0].trim())) {
        this.allPaths = Boolean.parseBoolean(stringProp.split(":")[1]
            .trim());
      } else if (BaseMessages.getString(PKG, "Git.Force.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.force = Boolean.parseBoolean(stringProp.split(":")[1]
            .trim());
      }
    }
  }

  public CheckoutGitCommand(Node xml, String stepName) throws KettleException {
    try {
      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      XPathExpression xPathExpr = xpath.compile("//step[name='"
          + stepName + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/path/text()");
      this.path = (String) xPathExpr.evaluate(xml, XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/name/text()");
      this.name = (String) xPathExpr.evaluate(xml, XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/startPoint/text()");
      this.startPoint = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/allPaths/text()");
      this.allPaths = Boolean.parseBoolean((String) xPathExpr.evaluate(
          xml, XPathConstants.STRING));

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/force/text()");
      this.force = Boolean.parseBoolean((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING));
    } catch (XPathExpressionException e) {
      throw new KettleException(e);
    }
  }

  public String getDescription() {
    return BaseMessages.getString(PKG, "Git.Path.Label") + ": "
        + (path == null ? "" : path) + " / "
        + BaseMessages.getString(PKG, "Git.Name.Label") + ": "
        + (name == null ? "" : name) + " / "
        + BaseMessages.getString(PKG, "Git.StartPoint.Label") + ": "
        + (startPoint == null ? "" : startPoint) + " / "
        + BaseMessages.getString(PKG, "Git.AllPaths.Label") + ": "
        + allPaths + " / "
        + BaseMessages.getString(PKG, "Git.Force.Label") + ": " + force;
  }

  public GitCommandType getCommandType() {
    return GitCommandType.CHECKOUT;
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
      commandName.appendChild(doc.createTextNode(GitCommandType.CHECKOUT
          .getOperationName()));
      root.appendChild(commandName);

      final Element pathNode = doc.createElement("path");
      pathNode.appendChild(doc.createTextNode(path));
      root.appendChild(pathNode);

      final Element nameNode = doc.createElement("name");
      nameNode.appendChild(doc.createTextNode(name));
      root.appendChild(nameNode);

      final Element startPointNode = doc.createElement("startPoint");
      startPointNode.appendChild(doc.createTextNode(startPoint));
      root.appendChild(startPointNode);

      final Element allPathsNode = doc.createElement("allPaths");
      allPathsNode.appendChild(doc.createTextNode(String
          .valueOf(allPaths)));
      root.appendChild(allPathsNode);

      final Element forceNode = doc.createElement("force");
      forceNode.appendChild(doc.createTextNode(String.valueOf(force)));
      root.appendChild(forceNode);

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

    CheckoutCommand cc = git.checkout().setAllPaths(this.allPaths)
        .setForce(this.force);
    if (!Const.isEmpty(this.path)) {
      cc = cc.addPath(gitOperationsStep.environmentSubstitute(this.path));
    }
    if (!Const.isEmpty(this.name)) {
      cc = cc.setName(gitOperationsStep.environmentSubstitute(this.name));
    }
    if (!Const.isEmpty(this.startPoint)) {
      cc = cc.setStartPoint(gitOperationsStep
          .environmentSubstitute(this.startPoint));
    }

    cc.call();

    return git;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStartPoint() {
    return startPoint;
  }

  public void setStartPoint(String startPoint) {
    this.startPoint = startPoint;
  }

  public boolean isAllPaths() {
    return allPaths;
  }

  public void setAllPaths(boolean allPaths) {
    this.allPaths = allPaths;
  }

  public boolean isForce() {
    return force;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

}
