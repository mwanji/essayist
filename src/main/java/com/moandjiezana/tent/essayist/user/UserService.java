package com.moandjiezana.tent.essayist.user;

import com.moandjiezana.tent.essayist.User;
import fj.data.Option;

import java.util.List;

/**
 * User: pjesi
 * Date: 1/15/13
 * Time: 10:57 PM
 */
public interface UserService {

    /**
     * Lookup a user by a custom domain.
     * @param domain
     * @return an optional user if a user has the domain mapped
     */
    Option<User> getUserByDomain(String domain);

    /**
     * Lookup a user by an Tent entity.
     * @param entity
     * @return an optional user if entity exists locally
     */
    Option<User> getUserByEntity(final String entity);

    List<User> getAll();

}
