package com.ivyis.di.trans.steps.git.command.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.InstanceCreationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ivyis.di.trans.steps.git.GitCommandType;
import com.ivyis.di.trans.steps.git.command.GitCommand;
import com.ivyis.di.trans.steps.git.command.GitInfoCommand;
import com.ivyis.di.trans.steps.git.dialog.BaseGitCommandDialog;
import com.ivyis.di.trans.steps.git.info.GitInfoStep;

/**
 * Current Branch Git command implementation.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class CurrentBranchGitCommand implements GitInfoCommand {
  private static final Class<?> PKG = CurrentBranchGitCommand.class; // for
  // i18n
  private static final Logger LOGGER = Logger.getLogger(PKG);

  // Fields name
  private String fieldName;

  public CurrentBranchGitCommand() {}

  public CurrentBranchGitCommand(Map<String, String> configFields)
      throws InstanceCreationException {
    this.fieldName = configFields.get(BaseMessages.getString(PKG,
        "Git.NameField.Label"));
  }

  public CurrentBranchGitCommand(Node xml, String stepName)
      throws KettleException {
    try {
      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      final XPathExpression xPathExpr = xpath.compile("//step[name='"
          + stepName + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/fieldName/text()");
      this.fieldName = (String) xPathExpr.evaluate(xml,
          XPathConstants.STRING);

    } catch (XPathExpressionException e) {
      throw new KettleException(e);
    }
  }

  public String getDescription() {
    return BaseMessages.getString(PKG, "Git.NameField.Label") + ": "
        + fieldName;
  }

  public Map<String, String> getConfigFields(boolean mainConfigFields) {
    final HashMap<String, String> map = new HashMap<String, String>();
    if (mainConfigFields) {
      return map;
    }
    map.put(BaseMessages.getString(PKG, "Git.NameField.Label"), fieldName);
    return map;
  }

  public GitCommandType getCommandType() {
    return GitCommandType.CURRENT_BRANCH;
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
          .createTextNode(GitCommandType.CURRENT_BRANCH
              .getOperationName()));
      root.appendChild(commandName);

      final Element commitNameFieldNode = doc.createElement("fieldName");
      commitNameFieldNode.appendChild(doc.createTextNode(fieldName));
      root.appendChild(commitNameFieldNode);

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
      throws IllegalArgumentException, IOException,
      NoFilepatternException, GitAPIException {

    final List<String[]> resp = new ArrayList<String[]>();

    final String[] branch = new String[] {git.getRepository().getBranch()};
    resp.add(branch);

    return resp;
  }

  public boolean isConfigurable() {
    return BaseGitCommandDialog.NO_CONFIGURATION;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

}
