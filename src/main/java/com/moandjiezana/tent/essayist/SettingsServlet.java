package com.moandjiezana.tent.essayist;

import com.moandjiezana.essayist.posts.EssayistMetadataContent;
import com.moandjiezana.essayist.utils.Tasks;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Mention;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.posts.content.EssayContent;
import com.moandjiezana.tent.client.users.Permissions;
import com.moandjiezana.tent.essayist.auth.Authenticated;
import com.moandjiezana.tent.essayist.config.EssayistConfig;
import com.moandjiezana.tent.essayist.config.Routes;
import com.moandjiezana.tent.essayist.tent.Entities;
import com.moandjiezana.tent.essayist.text.TextTransformation;
import com.moandjiezana.tent.essayist.user.UserService;
import fj.data.Option;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * User: pjesi
 * Date: 1/23/13
 * Time: 8:58 PM
 */
@Singleton
@Authenticated
public class SettingsServlet extends HttpServlet {

    private Templates templates;
    private Provider<EssayistSession> sessions;
    private Tasks tasks;
    private Provider<Routes> routes;
    private TextTransformation textTransformation;
    private EssayistConfig config;
    private final UserService userService;

    @Inject
    public SettingsServlet(TextTransformation textTransformation, Provider<EssayistSession> sessions,
                        Templates templates, Provider<Routes> routes, Tasks tasks, EssayistConfig config,
                        UserService userService) {
        this.textTransformation = textTransformation;
        this.sessions = sessions;
        this.templates = templates;
        this.routes = routes;
        this.tasks = tasks;
        this.config = config;
        this.userService = userService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        String domain = sessions.get().getUser().getDomain();

        templates.settings().render(resp.getWriter(), domain);
        //templates.newEssay().render(resp.getWriter());



    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String domain = req.getParameter("domain");
        User user = sessions.get().getUser();
        Option<User> domainOwner = userService.getUserByDomain(domain);
        if(domainOwner.isSome()){
            if(domainOwner.some().getId().equals(user.getId())){
                // nothing to do
                redirect(req, resp, null);
                return;
            } else {
                // domain is taken
                redirect(req, resp, "domain_taken");
                return;
            }
        }

        // new domain

        user.setDomain(domain);
        userService.save(user);

        redirect(req, resp, null);

    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String error) throws IOException {

        StringBuilder stringBuilder = new StringBuilder(req.getContextPath())
            .append("/settings");
        if(StringUtils.isNotEmpty(error)){
            stringBuilder.append("?error=").append(error);
        }

        resp.sendRedirect(stringBuilder.toString());

    }




    private TentClient newTentClient() {
        User user = sessions.get().getUser();

        TentClient tentClient = new TentClient(user.getProfile());
        tentClient.getAsync().setAccessToken(user.getAccessToken());
        tentClient.getAsync().setRegistrationResponse(user.getRegistration());

        return tentClient;
    }

}
