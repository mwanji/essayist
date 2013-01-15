package com.moandjiezana.tent.essayist.config;

import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.user.UserService;
import fj.data.Option;
import static fj.data.Option.none;
import static fj.data.Option.some;

import com.moandjiezana.tent.client.users.Profile;

import javax.servlet.http.HttpServletRequest;

/**
 * User: pjesi
 * Date: 1/15/13
 * Time: 8:28 PM
 */
public class EntityLookup {


    private EssayistConfig config;
    private UserService userService;

    public EntityLookup(EssayistConfig config, UserService userService) {
        this.config = config;
        this.userService = userService;
    }

    public Option<String> getEntity(HttpServletRequest request){

        String serverName = request.getServerName();
        String baseDomain = config.getBaseDomain(serverName);

        if(serverName.equals(baseDomain)){
            return none();
        } else {
            Option<User> user = userService.getUserByDomain(serverName);
            if(user.isSome()){
                return some(user.some().getProfile().getCore().getEntity());
            }
            return none();
        }
    }
}
