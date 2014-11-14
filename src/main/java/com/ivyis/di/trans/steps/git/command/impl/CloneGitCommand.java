package com.ivyis.di.trans.steps.git.command.impl;

import java.io.File;
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
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
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
 * Clone Git command implementation.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class CloneGitCommand implements GitOperationsCommand {
  private static final Class<?> PKG = CloneGitCommand.class; // for i18n
  private static final Logger LOGGER = Logger.getLogger(PKG);

  private String branchName;
  private boolean cloneSubModules;
  private boolean cloneAllBranches;

  public CloneGitCommand() {}

  public CloneGitCommand(String description) {
    if ("".equals(description)) {
      throw new IllegalArgumentException("Unkomnown config fields.");
    }
    final String[] stringProps = description.split(" / ");
    for (String stringProp : stringProps) {
      if (BaseMessages.getString(PKG, "Git.BranchField.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.branchName = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG,
          "Git.CloneSubModulesField.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.cloneSubModules = Boolean.parseBoolean(stringProp
            .split(":")[1].trim());
      } else if (BaseMessages.getString(PKG,
          "Git.CloneAllBranchesField.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.cloneAllBranches = Boolean.parseBoolean(stringProp
            .split(":")[1].trim());
      }
    }
  }

  public CloneGitCommand(Node xml, String stepName) {
    try {
      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      XPathExpression xPathExpr = xpath.compile("//step[name='"
          + stepName + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/branchName/text()");
      this.branchName = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/cloneSubModules/text()");
      this.cloneSubModules = Boolean.parseBoolean((String) xPathExpr
          .evaluate(xml, XPathConstants.STRING));

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/cloneAllBranches/text()");
      this.cloneAllBranches = Boolean.parseBoolean((String) xPathExpr
          .evaluate(xml, XPathConstants.STRING));

    } catch (XPathExpressionException e) {
      LOGGER.error(e.getMessage(), e);
    }
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
      commandName.appendChild(doc.createTextNode(GitCommandType.CLONE
          .getOperationName()));
      root.appendChild(commandName);

      final Element branchNameNode = doc.createElement("branchName");
      branchNameNode.appendChild(doc.createTextNode(branchName));
      root.appendChild(branchNameNode);

      final Element cloneSubModulesNode = doc
          .createElement("cloneSubModules");
      cloneSubModulesNode.appendChild(doc.createTextNode(String
          .valueOf(cloneSubModules)));
      root.appendChild(cloneSubModulesNode);

      final Element cloneAllBranchesNode = doc
          .createElement("cloneAllBranches");
      cloneAllBranchesNode.appendChild(doc.createTextNode(String
          .valueOf(cloneAllBranches)));
      root.appendChild(cloneAllBranchesNode);

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
      throws InvalidRemoteException, TransportException, GitAPIException {

    CloneCommand cc = Git.cloneRepository().setURI(gitRepoUrl)
        .setDirectory(gitRepoFolder);

    if (!Const.isEmpty(this.branchName)) {
      cc = cc.setBranch(gitOperationsStep
          .environmentSubstitute(this.branchName));
    }
    cc.setCloneAllBranches(this.cloneAllBranches).setCloneSubmodules(
        this.cloneSubModules);
    if (cp != null) {
      cc.setCredentialsProvider(cp);
    }
    return cc.setBare(false).call();
  }

  public String getDescription() {
    return BaseMessages.getString(PKG, "Git.BranchField.Label")
        + ": "
        + (branchName == null ? "" : branchName)
        + " / "
        + BaseMessages.getString(PKG, "Git.CloneSubModulesField.Label")
        + ": "
        + cloneSubModules
        + " / "
        + BaseMessages
            .getString(PKG, "Git.CloneAllBranchesField.Label")
        + ": " + cloneAllBranches;
  }

  public String getBranchName() {
    return branchName;
  }

  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  public boolean isCloneSubModules() {
    return cloneSubModules;
  }

  public void setCloneSubModules(boolean cloneSubModules) {
    this.cloneSubModules = cloneSubModules;
  }

  public boolean isCloneAllBranches() {
    return cloneAllBranches;
  }

  public void setCloneAllBranches(boolean cloneAllBranches) {
    this.cloneAllBranches = cloneAllBranches;
  }

  public GitCommandType getCommandType() {
    return GitCommandType.CLONE;
  }

}
