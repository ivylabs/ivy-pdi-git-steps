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
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ivyis.di.trans.steps.git.GitCommandType;
import com.ivyis.di.trans.steps.git.command.GitCommand;
import com.ivyis.di.trans.steps.git.command.GitOperationsCommand;
import com.ivyis.di.trans.steps.git.operations.GitOperationsStep;

/**
 * Pull Git command implementation.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class PullGitCommand implements GitOperationsCommand {
  private static final Class<?> PKG = PullGitCommand.class; // for i18n
  private static final Logger LOGGER = Logger.getLogger(PKG);

  private boolean rebase;

  public PullGitCommand() {}

  public PullGitCommand(String description) {
    if ("".equals(description)) {
      throw new IllegalArgumentException("Unkomnown config fields.");
    }
    final String[] stringProps = description.split(" / ");
    for (String stringProp : stringProps) {
      if (BaseMessages.getString(PKG, "Git.Rebase.Label").equals(
          stringProp.split(":")[0].trim())) {
        this.rebase = Boolean.parseBoolean(stringProp.split(":")[1]
            .trim());
      }
    }
  }

  public PullGitCommand(Node xml, String stepName) {
    try {
      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      final XPathExpression xPathExpr = xpath.compile("//step[name='"
          + stepName + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/rebase/text()");
      this.rebase = Boolean.parseBoolean((String) xPathExpr.evaluate(xml,
          XPathConstants.STRING));

    } catch (XPathExpressionException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  public String getDescription() {
    return BaseMessages.getString(PKG, "Git.Rebase.Label") + ": " + rebase;
  }

  public GitCommandType getCommandType() {
    return GitCommandType.PULL;
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
      commandName.appendChild(doc.createTextNode(GitCommandType.PULL
          .getOperationName()));
      root.appendChild(commandName);

      final Element rebaseNode = doc.createElement("rebase");
      rebaseNode.appendChild(doc.createTextNode(String.valueOf(rebase)));
      root.appendChild(rebaseNode);

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
      WrongRepositoryStateException, InvalidConfigurationException,
      DetachedHeadException, InvalidRemoteException, CanceledException,
      RefNotFoundException, NoHeadException, TransportException,
      GitAPIException {
    git.pull().setCredentialsProvider(cp).setRebase(rebase).call();
    return git;
  }

  public boolean isRebase() {
    return rebase;
  }

  public void setRebase(boolean rebase) {
    this.rebase = rebase;
  }


}
