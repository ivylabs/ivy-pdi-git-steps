package com.ivyis.di.trans.steps.git.command;

import com.ivyis.di.trans.steps.git.GitCommandType;

/**
 * Interface responsible for describe the methods of Git commands.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public interface GitCommand {

  static final String MAIN_NODE = "commandProperties";
  static final String COMMAND_NAME_NODE = "commandName";
  static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

  String getDescription();

  String getXML();

  GitCommandType getCommandType();

}
