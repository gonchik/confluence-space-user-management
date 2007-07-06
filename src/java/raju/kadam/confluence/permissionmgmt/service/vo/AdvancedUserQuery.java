package raju.kadam.confluence.permissionmgmt.service.vo;

import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedQueryType;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.velocity.util.StringUtils;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 21, 2007
 * Time: 11:34:18 AM
 */
public class AdvancedUserQuery {

    private String partialUserName;
    private String userNameSearchType = AdvancedQueryType.SUBSTRING_STARTS_WITH;

    private String partialFullName;
    private String fullNameSearchType = AdvancedQueryType.SUBSTRING_STARTS_WITH;

    private String partialEmail;
    private String emailSearchType = AdvancedQueryType.SUBSTRING_STARTS_WITH;

    private String partialGroupName;
    private String groupNameSearchType = AdvancedQueryType.SUBSTRING_STARTS_WITH;


    public String getPartialUserName() {
        return partialUserName;
    }

    public void setPartialUserName(String partialUserName) {
        this.partialUserName = partialUserName;
    }

    public String getUserNameSearchType() {
        return userNameSearchType;
    }

    public void setUserNameSearchType(String userNameSearchType) {
        this.userNameSearchType = userNameSearchType;
    }

    public String getPartialFullName() {
        return partialFullName;
    }

    public void setPartialFullName(String partialFullName) {
        this.partialFullName = partialFullName;
    }

    public String getFullNameSearchType() {
        return fullNameSearchType;
    }

    public void setFullNameSearchType(String fullNameSearchType) {
        this.fullNameSearchType = fullNameSearchType;
    }

    public String getPartialEmail() {
        return partialEmail;
    }

    public void setPartialEmail(String partialEmail) {
        this.partialEmail = partialEmail;
    }

    public String getEmailSearchType() {
        return emailSearchType;
    }

    public void setEmailSearchType(String emailSearchType) {
        this.emailSearchType = emailSearchType;
    }

    public String getPartialGroupName() {
        return partialGroupName;
    }

    public void setPartialGroupName(String partialGroupName) {
        this.partialGroupName = partialGroupName;
    }

    public String getGroupNameSearchType() {
        return groupNameSearchType;
    }

    public void setGroupNameSearchType(String groupNameSearchType) {
        this.groupNameSearchType = groupNameSearchType;
    }

    private boolean isValidSearchType(String type) {
        boolean result = false;
        if ( type != null &&
                (AdvancedQueryType.SUBSTRING_CONTAINS.equals(type) ||
                        AdvancedQueryType.SUBSTRING_ENDS_WITH.equals(type) ||
                        AdvancedQueryType.SUBSTRING_STARTS_WITH.equals(type) )
                ) {
            result = true;
        }
        return result;
    }

    public boolean isDefined() {
        boolean result = false;
        if ( isUsernameSearchDefined() ||
                isFullnameSearchDefined() ||
                isEmailSearchDefined() ||
                isGroupnameSearchDefined() ) {
            result = true;
        }
        return result;
    }

    public boolean isUsernameSearchDefined() {
        boolean result = false;
        if (getPartialUserName() != null && !"".equals(getPartialUserName()) &&
            isValidSearchType( getUserNameSearchType() )) {
            result = true;
        }
        return result;
    }

    public boolean isFullnameSearchDefined() {
        boolean result = false;
        if (getPartialFullName() != null && !"".equals(getPartialFullName()) &&
                isValidSearchType( getFullNameSearchType() )) {
            result = true;
        }
        return result;
    }

    public boolean isEmailSearchDefined() {
        boolean result = false;
        if (getPartialEmail() != null && !"".equals(getPartialEmail()) &&
                isValidSearchType( getEmailSearchType() )) {
            result = true;
        }
        return result;
    }

    public boolean isGroupnameSearchDefined() {
        boolean result = false;
        if (getPartialGroupName() != null && !"".equals(getPartialGroupName()) &&
                isValidSearchType( getGroupNameSearchType() )) {
            result = true;
        }
        return result;
    }

    // atlassian-user is really picky about doing == instead of .equals to check the TermQuery constant, so we do this
    public void makeSearchTypesMatchTermQueryConstantInstances() {
        this.setUserNameSearchType(getConstantInstance(getUserNameSearchType()));
        this.setFullNameSearchType(getConstantInstance(getFullNameSearchType()));
        this.setEmailSearchType(getConstantInstance(getEmailSearchType()));
        this.setGroupNameSearchType(getConstantInstance(getGroupNameSearchType()));
    }

    // atlassian-user is really picky about doing == instead of .equals to check the TermQuery constant, so we do this
    public String getConstantInstance(String s) {
        String result = null;
        if (s!=null) {
            if (AdvancedQueryType.SUBSTRING_CONTAINS.equalsIgnoreCase(s) ) {
                result = AdvancedQueryType.SUBSTRING_CONTAINS;
            }
            if (AdvancedQueryType.SUBSTRING_ENDS_WITH.equalsIgnoreCase(s) ) {
                result = AdvancedQueryType.SUBSTRING_ENDS_WITH;
            }
            if (AdvancedQueryType.SUBSTRING_STARTS_WITH.equalsIgnoreCase(s) ) {
                result = AdvancedQueryType.SUBSTRING_STARTS_WITH;
            }
        }
        return result;
    }

    public boolean isValid()
    {
        boolean isValid = false;

        if ( isValidSearchType( getUserNameSearchType() ) &&
             isValidSearchType( getEmailSearchType() ) &&
             isValidSearchType( getFullNameSearchType() ) &&
             isValidSearchType( getGroupNameSearchType() ) &&
             getPartialEmail() != null &&
             getPartialFullName() != null &&
             getPartialGroupName() != null )
        {
            isValid = true;
        }

        return isValid;
    }
}