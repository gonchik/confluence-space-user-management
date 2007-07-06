package raju.kadam.confluence.permissionmgmt.service.impl;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;
import com.atlassian.user.search.query.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.service.*;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQueryResults;
import raju.kadam.confluence.permissionmgmt.util.UserUtil;
import raju.kadam.util.LDAP.LDAPUser;
import raju.kadam.util.LDAP.LDAPUtil;
import raju.kadam.util.ListUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:48:36 AM
 */
public class ConfluenceUserManagementService implements UserManagementService {

    private UserAccessor userAccessor;
    private CustomPermissionConfiguration customPermissionConfiguration;
    private Log log = LogFactory.getLog(this.getClass());

    public ConfluenceUserManagementService() {
        log.debug("ConfluenceUserManagementService start constructor");
        userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
        //customPermissionConfiguration = (CustomPermissionConfiguration) ConfluenceUtil.loadComponentWithRetry("customPermissionConfiguration");
        log.debug("ConfluenceUserManagementService end constructor");
    }

    private List findIntersection( List existingUsersList, List returnedUsers, boolean ranQueryAtLeastOnce) {
        List users = null;
        if (ranQueryAtLeastOnce) {
            users = UserUtil.findIntersectionOfUsers(existingUsersList, returnedUsers);
        }
        else {
            users = returnedUsers;
        }

        return users;
    }

    public AdvancedUserQueryResults findUsers(AdvancedUserQuery advancedUserQuery, ServiceContext context) throws FindException {

        AdvancedUserQueryResults results = new AdvancedUserQueryResults();

        List users = new ArrayList();
        boolean ranQueryAtLeastOnce = false;
        if (advancedUserQuery.isFullnameSearchDefined()) {
            try {
                UserNameTermQuery query = new UserNameTermQuery(advancedUserQuery.getPartialUserName(), advancedUserQuery.getUserNameSearchType());
                SearchResult result = userAccessor.findUsers(query);
                List returnedUsers = PagerUtils.toList(result.pager());
                results.setUserNameFieldMessage("" + returnedUsers.size() + " returned");
                users = findIntersection(users, returnedUsers, ranQueryAtLeastOnce);
                ranQueryAtLeastOnce = true;
            }
            catch (EntityException e) {
                log.warn("query by username failed due to EntityException", e);
                results.setUserNameFieldMessage("" + e);
            }
            catch (IllegalArgumentException e) {
                // if search type is not allowed
                log.warn("Bad value '" + advancedUserQuery.getPartialUserName() + "' for search type '" + advancedUserQuery.getUserNameSearchType() + "'", e);
                results.setUserNameFieldMessage("Bad value '" + advancedUserQuery.getPartialUserName() + "' for search type '" + advancedUserQuery.getUserNameSearchType() + "'");
            }
        }

        if (advancedUserQuery.isFullnameSearchDefined()) {
            try {
                FullNameTermQuery query = new FullNameTermQuery(advancedUserQuery.getPartialFullName(), advancedUserQuery.getFullNameSearchType());
                SearchResult result = userAccessor.findUsers(query);
                List returnedUsers = PagerUtils.toList(result.pager());
                results.setFullNameFieldMessage("" + returnedUsers.size() + " returned");
                users = findIntersection(users, returnedUsers, ranQueryAtLeastOnce);
                ranQueryAtLeastOnce = true;
            }
            catch (EntityException e) {
                log.warn("query by user fullname failed due to EntityException", e);
                results.setFullNameFieldMessage("" + e);
            }
            catch (IllegalArgumentException e) {
                // if search type is not allowed
                log.warn("Bad value '" + advancedUserQuery.getPartialFullName() + "' for search type '" + advancedUserQuery.getFullNameSearchType() + "'", e);
                results.setFullNameFieldMessage("Bad value '" + advancedUserQuery.getPartialFullName() + "' for search type '" + advancedUserQuery.getFullNameSearchType() + "'");
            }
        }

        if (advancedUserQuery.isEmailSearchDefined()) {
            try {
                EmailTermQuery query = new EmailTermQuery(advancedUserQuery.getPartialEmail(), advancedUserQuery.getEmailSearchType());
                SearchResult result = userAccessor.findUsers(query);
                List returnedUsers = PagerUtils.toList(result.pager());
                results.setEmailFieldMessage("" + returnedUsers.size() + " returned");
                users = findIntersection(users, returnedUsers, ranQueryAtLeastOnce);
                ranQueryAtLeastOnce = true;
            }
            catch (EntityException e) {
                log.warn("query by user email failed due to EntityException", e);
                results.setEmailFieldMessage("" + e);
            }
            catch (IllegalArgumentException e) {
                // if search type is not allowed
                log.warn("Bad value '" + advancedUserQuery.getPartialEmail() + "' for search type '" + advancedUserQuery.getEmailSearchType() + "'", e);
                results.setEmailFieldMessage("Bad value '" + advancedUserQuery.getPartialEmail() + "' for search type '" + advancedUserQuery.getEmailSearchType() + "'");
            }
        }

        if (advancedUserQuery.isGroupnameSearchDefined()) {
            try {
                GroupNameTermQuery query = new GroupNameTermQuery(advancedUserQuery.getPartialGroupName(), advancedUserQuery.getGroupNameSearchType());
                SearchResult result = userAccessor.findGroups(query);
                List groups = PagerUtils.toList(result.pager());
                List returnedUsers = new ArrayList();
                for (int i = 0; i < groups.size(); i++) {
                    returnedUsers.addAll(findUsersForGroup((Group) groups.get(i)));
                }
                results.setGroupNameFieldMessage("" + returnedUsers.size() + " returned");
                users = findIntersection(users, returnedUsers, ranQueryAtLeastOnce);
                ranQueryAtLeastOnce = true;
            }
            catch (EntityException e) {
                log.warn("query by groupname failed due to EntityException", e);
                results.setGroupNameFieldMessage("" + e);
            }
            catch (IllegalArgumentException e) {
                // if search type is not allowed
                log.warn("Bad value '" + advancedUserQuery.getPartialGroupName() + "' for search type '" + advancedUserQuery.getGroupNameSearchType() + "'", e);
                results.setGroupNameFieldMessage("Bad value '" + advancedUserQuery.getPartialGroupName() + "' for search type '" + advancedUserQuery.getGroupNameSearchType() + "'");
            }
        }

        results.setUsers(users);

        return results;
    }

    public List findUsersForGroup(String groupName, ServiceContext context) {
        Group group = userAccessor.getGroup(groupName);
        return findUsersForGroup(group);
    }

    private List findUsersForGroup(Group group) {
        List results = new ArrayList();
        Pager pager = userAccessor.getMemberNames(group);
        List memberNames = PagerUtils.toList(pager);
        //TODO: too many queries. will be unacceptably slow with lots of users and needs workaround.
        for (int i = 0; i < memberNames.size(); i++) {
            String username = (String) memberNames.get(i);
            User user = userAccessor.getUser(username);
            results.add(user);
        }

        return results;
    }

    public List findUsersWhoseNameStartsWith(String partialName, ServiceContext context) {

        List users = new ArrayList();

        try {
            UserNameTermQuery query = new UserNameTermQuery(partialName, TermQuery.SUBSTRING_STARTS_WITH);
            SearchResult result = userAccessor.findUsers(query);
            users = PagerUtils.toList(result.pager());
        }
        catch (EntityException e) {
            e.printStackTrace();
        }

        return users;
    }

    private List getUsersForUsernames(List userNames) {
        List users = new ArrayList();
        if (userNames!=null) {
            for (int i=0;i<userNames.size();i++) {
                users.add(userAccessor.getUser((String)userNames.get(i)));
            }
        }
        return users;
    }

    public void addUsersByUsernameToGroup(List userNames, String groupName, ServiceContext context) throws AddException {
        List groupNames = ListUtil.createListOfOneItem(groupName);
        addUsersByUsernameToGroupsByGroupname(userNames, groupNames);
    }

    private void addUsersByUsernameToGroupsByGroupname(List userNames, List groupNames) throws AddException {

        CustomPermissionConfiguration config = getCustomPermissionConfiguration();

        AddException ex = null;

        boolean isLDAPPresent = config.getLdapAuthUsed().equals(CustomPermissionConfigConstants.YES) ? true : false;

        //Associate selected user-groups to all users.
        for (Iterator itr = userNames.iterator(); itr.hasNext();) {
            //First check if given user is present or not
            String userid = (String) itr.next();
            User currUser = userAccessor.getUser(userid);
            if (currUser == null) {
                //create an user
                //userid doesn't exists, if LDAP present then we will create User if it exists in LDAP.
                if (isLDAPPresent) {
                    //create an user.

                    // TODO: the option to create users if they don't exist using LDAP info should be in config

                    // TODO: consider adding option and ability to create users if they don't exist, even if LDAP not used

                    currUser = createConfUser(userid, isLDAPPresent, config.getCompanyLDAPUrl(), config.getCompanyLDAPBaseDN());
                }

                //if user details not found in LDAP too, then retun userid in errorids
                if (currUser == null) {

                    if (ex == null) {
                        ex = new AddException(ErrorReason.USER_NOT_FOUND);
                    }

                    //for some reason we are unable to create user.
                    //add it to our notCreatedUser List.
                    ex.addId(userid);

                    continue;

                } else {
                    //Add this user to default group confluence-users
                    userAccessor.addMembership("confluence-users", userid);
                }
            }

            //If user exists then associate him/her to all selected usergroups
            if (currUser != null) {
                //Associate this user to all selected user-groups
                for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
                    userAccessor.addMembership((String) iterator.next(), userid);
                }

            }
        }

        // If we failed, throw exception
        if (ex != null) {
            throw ex;
        }
    }

    //This method will be used to create an user when Confluence is used for Managing Wiki Users
    private User createConfUser(String creationUserName, boolean isLDAPAvailable, String companyLDAPUrl, String companyLDAPBaseDN) {
        User vUser = null;
        LDAPUser lUser = null;

        log.debug("create a confluence user -> " + creationUserName);

        try {
            //if LDAP Lookup is available, get information from there.
            if (isLDAPAvailable) {
                //log.debug("LDAP Lookup available");
                //Get user details from LDAP.
                lUser = LDAPUtil.getLDAPUser(creationUserName, companyLDAPUrl, companyLDAPBaseDN);
                if (lUser != null) {
                    vUser = userAccessor.addUser(creationUserName, creationUserName, lUser.getEmail(), lUser.getFullName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vUser;
    }


    public void removeUsersByUsernameFromGroup(List userNames, String groupName, ServiceContext context) throws RemoveException {
        List groupNames = ListUtil.createListOfOneItem(groupName);
        removeUsersByUsernamesFromGroupsByGroupname(userNames, groupNames);
    }

    private void removeUsersByUsernamesFromGroupsByGroupname(List userNames, List groupNames) {

        for (Iterator itr = userNames.iterator(); itr.hasNext();) {
            String userid = (String) itr.next();
            for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
                userAccessor.removeMembership((String) iterator.next(), userid);
            }
        }
    }

    public boolean isMemberOf(String userName, String groupName) {
        boolean result = false;
        Group group = userAccessor.getGroup(groupName);
        if (group!=null) {
            Pager pager = userAccessor.getMemberNames(group);
            List memberNames = PagerUtils.toList(pager);
            if (memberNames!=null) {
                result = memberNames.contains(userName);
            }
        }
        return result;
    }


    public UserAccessor getUserAccessor() {
        return userAccessor;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }
}