package com.moandjiezana.tent.essayist.config;

import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.tent.Entities;
import com.moandjiezana.tent.essayist.user.UserService;
import fj.data.Option;
import static fj.data.Option.none;
import static fj.data.Option.some;

import com.moandjiezana.tent.client.users.Profile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: pjesi
 * Date: 1/15/13
 * Time: 8:28 PM
 */
public class EntityLookup {

    private EssayistConfig config;
    private UserService userService;

    @Inject
    public EntityLookup(EssayistConfig config, UserService userService) {
        this.config = config;
        this.userService = userService;
    }

    public String getRequestPath(HttpServletRequest request){
        String path = request.getRequestURI()
                .substring(request.getContextPath().length());
        return path;
    }

    public Option<TentRequest> getTentRequest(HttpServletRequest request){
        Option<String> entity = getEntity(request);
        if(entity.isNone()){
            return none();
        }

        TentRequest.Builder builder = new TentRequest.Builder();
        builder.entity(entity.some());

        StringTokenizer st = new StringTokenizer(getRequestPath(request), "/");
        String token = null;
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            if(token.equals("essay")){
                break;
            }
        }
        if(st.hasMoreTokens()){
            builder.post(st.nextToken());
        } else {
            builder.post(token);
        }
        if(st.hasMoreTokens()){
            builder.action(st.nextToken());
        }
        return some(builder.build());

    }

    public Option<String> getEntity(HttpServletRequest request){

        String serverName = request.getServerName();
        String baseDomain = config.getBaseDomain(serverName);

        if(serverName.equals(baseDomain)){
            return getFromPath(request);
        } else {
            Option<User> user = userService.getUserByDomain(serverName);
            if(user.isSome()){
                return some(user.some().getProfile().getCore().getEntity());
            }
            return none();
        }
    }

    private Option<String> getFromPath(HttpServletRequest request){
        String pathInfo = getRequestPath(request);
        String[] path = pathInfo.split("/");
        boolean isEssay = pathInfo.contains("/essay");
        if(path.length < 2){
            return config.getDefaultEntity();
        }

        if(path[1].contains("essay")){
            return config.getDefaultEntity();
        }

        if(isEssay){
            String entity = Entities.expandFromUrl(path[1]);
            return some(entity);
        }

        return none();

        //String entity = Entities.expandFromUrl(path[1]);
        //return some(entity);
    }
}
