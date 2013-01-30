package com.moandjiezana.tent.essayist.config;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import com.google.common.base.Optional;
import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.tent.Entities;
import com.moandjiezana.tent.essayist.user.UserService;

import java.util.StringTokenizer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

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

    public Optional<TentRequest> getTentRequest(HttpServletRequest request){
      Optional<String> entity = getEntity(request);
        if(!entity.isPresent()){
            return absent();
        }

        TentRequest.Builder builder = new TentRequest.Builder();
        builder.entity(entity.get());

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
        return of(builder.build());

    }

    public Optional<String> getEntity(HttpServletRequest request){

        String serverName = request.getServerName();
        String baseDomain = config.getBaseDomain(serverName);

        if(serverName.equals(baseDomain)){
            return getFromPath(request);
        } else {
          Optional<User> user = userService.getUserByDomain(serverName);
            if(user.isPresent()){
                return of(user.get().getProfile().getCore().getEntity());
            }
            return absent();
        }
    }

    private Optional<String> getFromPath(HttpServletRequest request){
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
            return of(entity);
        }

        return absent();

        //String entity = Entities.expandFromUrl(path[1]);
        //return some(entity);
    }
}
