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
import org.eclipse.jgit.api.PushCommand;
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
 * Push Git command implementation.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class PushGitCommand implements GitOperationsCommand {
  private static final Class<?> PKG = PushGitCommand.class; // for i18n
  private static final Logger LOGGER = Logger.getLogger(PKG);

  private String referenceToPush;
  private String remote;
  private String receivePack;
  private boolean dryRun;
  private boolean force;
  private boolean thin;
  private boolean pushAllBranches;
  private boolean pushAllTags;

  public PushGitCommand() {}

  public PushGitCommand(String description) {
    if ("".equals(description)) {
      throw new IllegalArgumentException("Unkomnown config fields.");
    }
    final String[] stringProps = description.split(" / ");
    for (String stringProp : stringProps) {
      final String prop = stringProp.split(":")[0].trim();
      if (BaseMessages.getString(PKG, "Git.ReferenceToPush.Label")
          .equals(prop)) {
        this.referenceToPush = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.Remote.Label").equals(
          prop)) {
        this.remote = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.ReceivePack.Label")
          .equals(prop)) {
        this.receivePack = stringProp.split(":")[1].trim();
      } else if (BaseMessages.getString(PKG, "Git.DryRun.Label").equals(
          prop)) {
        this.dryRun = Boolean.parseBoolean(stringProp.split(":")[1]
            .trim());
      } else if (BaseMessages.getString(PKG, "Git.Force.Label").equals(
          prop)) {
        this.force = Boolean.parseBoolean(stringProp.split(":")[1]
            .trim());
      } else if (BaseMessages.getString(PKG, "Git.Thin.Label").equals(
          prop)) {
        this.thin = Boolean.parseBoolean(stringProp.split(":")[1]
            .trim());
      } else if (BaseMessages.getString(PKG, "Git.PushAllBranches.Label")
          .equals(prop)) {
        this.pushAllBranches = Boolean.parseBoolean(stringProp
            .split(":")[1].trim());
      } else if (BaseMessages.getString(PKG, "Git.PushAllTag.Label")
          .equals(prop)) {
        this.pushAllTags = Boolean
            .parseBoolean(stringProp.split(":")[1].trim());
      }
    }
  }

  public PushGitCommand(Node xml, String stepName) {
    try {
      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      XPathExpression xPathExpr = xpath.compile("//step[name='"
          + stepName + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/referenceToPush/text()");
      this.referenceToPush = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/remote/text()");
      this.remote = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/receivePack/text()");
      this.receivePack = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/dryRun/text()");
      this.dryRun = Boolean.parseBoolean((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING));

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/force/text()");
      this.force = Boolean.parseBoolean((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING));

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/thin/text()");
      this.thin = Boolean.parseBoolean((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING));

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/pushAllBranches/text()");
      this.pushAllBranches = Boolean.parseBoolean((String) xPathExpr
          .evaluate(xml, XPathConstants.STRING));

      xPathExpr = xpath.compile("//step[name='" + stepName
          + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/pushAllTag/text()");
      this.pushAllTags = Boolean.parseBoolean((String) xPathExpr
          .evaluate(xml, XPathConstants.STRING));

    } catch (XPathExpressionException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  public String getDescription() {
    return BaseMessages.getString(PKG, "Git.ReferenceToPush.Label") + ": "
        + (referenceToPush == null ? "" : referenceToPush) + " / "
        + BaseMessages.getString(PKG, "Git.Remote.Label") + ": "
        + (remote == null ? "" : remote) + " / "
        + BaseMessages.getString(PKG, "Git.ReceivePack.Label") + ": "
        + (receivePack == null ? "" : receivePack) + " / "
        + BaseMessages.getString(PKG, "Git.DryRun.Label") + ": "
        + dryRun + " / "
        + BaseMessages.getString(PKG, "Git.Force.Label") + ": " + force
        + " / " + BaseMessages.getString(PKG, "Git.Thin.Label") + ": "
        + thin + " / "
        + BaseMessages.getString(PKG, "Git.PushAllBranches.Label")
        + ": " + pushAllBranches + " / "
        + BaseMessages.getString(PKG, "Git.PushAllTag.Label") + ": "
        + pushAllTags;
  }

  public GitCommandType getCommandType() {
    return GitCommandType.PUSH;
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
      commandName.appendChild(doc.createTextNode(GitCommandType.PUSH
          .getOperationName()));
      root.appendChild(commandName);

      final Element referenceToPushNode = doc
          .createElement("referenceToPush");
      referenceToPushNode
          .appendChild(doc.createTextNode(referenceToPush));
      root.appendChild(referenceToPushNode);

      final Element remoteNode = doc.createElement("remote");
      remoteNode.appendChild(doc.createTextNode(remote));
      root.appendChild(remoteNode);

      final Element receivePackNode = doc.createElement("receivePack");
      receivePackNode.appendChild(doc.createTextNode(receivePack));
      root.appendChild(receivePackNode);

      final Element dryRunNode = doc.createElement("dryRun");
      dryRunNode.appendChild(doc.createTextNode(String.valueOf(dryRun)));
      root.appendChild(dryRunNode);

      final Element forceNode = doc.createElement("force");
      forceNode.appendChild(doc.createTextNode(String.valueOf(force)));
      root.appendChild(forceNode);

      final Element thinNode = doc.createElement("thin");
      thinNode.appendChild(doc.createTextNode(String.valueOf(thin)));
      root.appendChild(thinNode);

      final Element pushAllBranchesNode = doc
          .createElement("pushAllBranches");
      pushAllBranchesNode.appendChild(doc.createTextNode(String
          .valueOf(pushAllBranches)));
      root.appendChild(pushAllBranchesNode);

      final Element pushAllTagNode = doc.createElement("pushAllTag");
      pushAllTagNode.appendChild(doc.createTextNode(String
          .valueOf(pushAllTags)));
      root.appendChild(pushAllTagNode);

      final Transformer tf = TransformerFactory.newInstance()
          .newTransformer();
      tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      tf.setOutputProperty(OutputKeys.INDENT, "yes");
      tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      final Writer out = new StringWriter();
      tf.transform(new DOMSource(doc), new StreamResult(out));
      return out.toString();

    } catch (ParserConfigurationException pce) {
      return "";
    } catch (TransformerException e) {
      LOGGER.error(e.getMessage(), e);
      return "";
    }
  }

  public Git call(final GitOperationsStep gitOperationsStep, Git git,
      CredentialsProvider cp, String gitRepoUrl, File gitRepoFolder)
      throws IllegalArgumentException, IOException,
      InvalidRemoteException, TransportException, GitAPIException {

    PushCommand pc = git.push().setDryRun(dryRun).setForce(force)
        .setThin(thin);
    if (cp != null) {
      pc = pc.setCredentialsProvider(cp);
    }
    if (!Const.isEmpty(this.receivePack)) {
      pc = pc.setReceivePack(gitOperationsStep
          .environmentSubstitute(receivePack));
    }
    if (!Const.isEmpty(this.referenceToPush)) {
      pc = pc.add(gitOperationsStep
          .environmentSubstitute(this.referenceToPush));
    }
    if (!Const.isEmpty(this.remote)) {
      pc = pc.setRemote(gitOperationsStep
          .environmentSubstitute(this.remote));
    }
    if (this.pushAllBranches) {
      pc = pc.setPushAll();
    }
    if (this.pushAllTags) {
      pc = pc.setPushTags();
    }
    pc.call();
    return git;
  }

  public String getReferenceToPush() {
    return referenceToPush;
  }

  public void setReferenceToPush(String referenceToPush) {
    this.referenceToPush = referenceToPush;
  }

  public String getRemote() {
    return remote;
  }

  public void setRemote(String remote) {
    this.remote = remote;
  }

  public String getReceivePack() {
    return receivePack;
  }

  public void setReceivePack(String receivePack) {
    this.receivePack = receivePack;
  }

  public boolean isDryRun() {
    return dryRun;
  }

  public void setDryRun(boolean dryRun) {
    this.dryRun = dryRun;
  }

  public boolean isForce() {
    return force;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

  public boolean isThin() {
    return thin;
  }

  public void setThin(boolean thin) {
    this.thin = thin;
  }

  public boolean isPushAllBranches() {
    return pushAllBranches;
  }

  public void setPushAllBranches(boolean pushAllBranches) {
    this.pushAllBranches = pushAllBranches;
  }

  public boolean isPushAllTags() {
    return pushAllTags;
  }

  public void setPushAllTags(boolean pushAllTags) {
    this.pushAllTags = pushAllTags;
  }

}
