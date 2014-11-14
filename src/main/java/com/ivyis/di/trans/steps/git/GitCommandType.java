package com.ivyis.di.trans.steps.git;

import org.pentaho.di.i18n.BaseMessages;

/**
 * Emun of Git commands allowed in PDI.
 * 
 * @author <a href="mailto:joel.latino@ivy-is.co.uk">Joel Latino</a>
 * @since 1.0.0
 */
public enum GitCommandType {
  CURRENT_BRANCH("currentBranch", false), LIST_BRANCHS("listBranchs", false), LIST_TAGS(
      "listTags", false), LOG("log", false), INIT("init", true), ADD(
      "add", true), CLONE("clone", true), COMMIT("commit", true), PULL(
      "pull", true), PUSH("push", true), TAG("tag", true), CHECKOUT(
      "checkout", true);

  /** for i18n purposes. **/
  private static final Class<?> PKG = GitCommandType.class;
  private static final int INFO_COMMANDS_NUMBER = 4;
  private static final int OPERATION_COMMANDS_NUMBER = 8;

  private final String operationName;
  private final boolean operations;

  public String getOperationName() {
    return operationName;
  }

  public boolean isOperations() {
    return operations;
  }

  public String getOperationLabel() {
    return BaseMessages.getString(PKG, "Git." + operationName + ".Label");
  }

  private GitCommandType(String operationName, boolean operations) {
    this.operationName = operationName;
    this.operations = operations;
  }

  public static GitCommandType fromValue(final String value) {
    for (GitCommandType type : GitCommandType.values()) {
      if (type.getOperationName().equalsIgnoreCase(value)) {
        return type;
      }
    }
    return null;
  }

  public static GitCommandType fromLabel(final String label) {
    if (label == null) {
      return null;
    }
    for (GitCommandType type : GitCommandType.values()) {
      if (BaseMessages.getString(PKG,
          "Git." + type.getOperationName() + ".Label")
          .equalsIgnoreCase(label)) {
        return type;
      }
    }
    return null;
  }

  public static String[] getOperationTypesLabel() {
    int i = 0;
    final String[] gitOperationTypeLabels = new String[OPERATION_COMMANDS_NUMBER];
    for (GitCommandType type : GitCommandType.values()) {
      if (type.isOperations()) {
        gitOperationTypeLabels[i++] = BaseMessages.getString(PKG,
            "Git." + type.getOperationName() + ".Label");
      }
    }
    return gitOperationTypeLabels;
  }

  public static String[] getInfoTypesLabel() {
    int i = 0;
    final String[] gitOperationTypeLabels = new String[INFO_COMMANDS_NUMBER];
    for (GitCommandType type : GitCommandType.values()) {
      if (!type.isOperations()) {
        gitOperationTypeLabels[i++] = BaseMessages.getString(PKG,
            "Git." + type.getOperationName() + ".Label");
      }
    }
    return gitOperationTypeLabels;
  }
}
