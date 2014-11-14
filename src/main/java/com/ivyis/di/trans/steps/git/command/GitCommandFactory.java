package com.ivyis.di.trans.steps.git.command;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.ivyis.di.trans.steps.git.GitCommandType;
import com.ivyis.di.trans.steps.git.command.impl.AddGitCommand;
import com.ivyis.di.trans.steps.git.command.impl.CheckoutGitCommand;
import com.ivyis.di.trans.steps.git.command.impl.CloneGitCommand;
import com.ivyis.di.trans.steps.git.command.impl.CommitGitCommand;
import com.ivyis.di.trans.steps.git.command.impl.CurrentBranchGitCommand;
import com.ivyis.di.trans.steps.git.command.impl.InitGitCommand;
import com.ivyis.di.trans.steps.git.command.impl.ListBranchsGitCommand;
import com.ivyis.di.trans.steps.git.command.impl.ListTagsGitCommand;
import com.ivyis.di.trans.steps.git.command.impl.LogGitCommand;
import com.ivyis.di.trans.steps.git.command.impl.PullGitCommand;
import com.ivyis.di.trans.steps.git.command.impl.PushGitCommand;
import com.ivyis.di.trans.steps.git.command.impl.TagGitCommand;

/**
 * Git command factory.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public class GitCommandFactory {
  private static final Logger LOGGER = Logger
      .getLogger(GitCommandFactory.class);

  public static GitCommand getGitCommand(String xml, String stepName)
      throws ParserConfigurationException, SAXException, IOException {
    final InputStream sbis = new ByteArrayInputStream(xml.getBytes("UTF-8"));

    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(false);
    final DocumentBuilder db = dbf.newDocumentBuilder();
    final Document doc = db.parse(sbis);

    return getGitCommand(doc, stepName);
  }

  public static GitCommand getGitCommand(Node node, String stepName) {
    try {
      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      final XPathExpression xPathExpr = xpath.compile("//step[name='"
          + stepName + "']/gitCommands//" + GitCommand.MAIN_NODE
          + "/" + GitCommand.COMMAND_NAME_NODE + "/text()");

      final String commandName = (String) xPathExpr.evaluate(node,
          XPathConstants.STRING);

      switch (GitCommandType.fromValue(commandName)) {
        case LIST_BRANCHS:
          return new ListBranchsGitCommand(node, stepName);
        case LIST_TAGS:
          return new ListTagsGitCommand(node, stepName);
        case LOG:
          return new LogGitCommand(node, stepName);
        case CLONE:
          return new CloneGitCommand(node, stepName);
        case ADD:
          return new AddGitCommand(node, stepName);
        case COMMIT:
          return new CommitGitCommand(node, stepName);
        case INIT:
          return new InitGitCommand(node, stepName);
        case PULL:
          return new PullGitCommand(node, stepName);
        case PUSH:
          return new PushGitCommand(node, stepName);
        case TAG:
          return new TagGitCommand(node, stepName);
        case CHECKOUT:
          return new CheckoutGitCommand(node, stepName);
        case CURRENT_BRANCH:
          return new CurrentBranchGitCommand(node, stepName);
        default:
          break;
      }

    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
    return null;
  }

  public static GitInfoCommand getGitCommand(GitCommandType gitCommandType,
      Map<String, String> configFields) throws KettleException {
    try {
      switch (gitCommandType) {
        case LIST_BRANCHS:
          return new ListBranchsGitCommand(configFields);
        case LIST_TAGS:
          return new ListTagsGitCommand(configFields);
        case LOG:
          return new LogGitCommand(configFields);
        case CURRENT_BRANCH:
          return new CurrentBranchGitCommand(configFields);
        default:
          break;
      }

    } catch (Exception e) {
      throw new KettleException(e);
    }
    return null;
  }

  public static GitOperationsCommand getGitCommand(
      GitCommandType gitCommandType, String description)
      throws KettleException {
    try {
      switch (gitCommandType) {
        case CLONE:
          return new CloneGitCommand(description);
        case ADD:
          return new AddGitCommand(description);
        case COMMIT:
          return new CommitGitCommand(description);
        case INIT:
          return new InitGitCommand(description);
        case PULL:
          return new PullGitCommand(description);
        case PUSH:
          return new PushGitCommand(description);
        case TAG:
          return new TagGitCommand(description);
        case CHECKOUT:
          return new CheckoutGitCommand(description);
        default:
          break;
      }

    } catch (Exception e) {
      throw new KettleException(e);
    }
    return null;
  }

}
