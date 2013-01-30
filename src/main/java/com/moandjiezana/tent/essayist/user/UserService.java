package com.moandjiezana.tent.essayist.user;

import com.google.common.base.Optional;
import com.moandjiezana.tent.essayist.User;

import java.util.List;

public interface UserService {

    /**
     * Lookup a user by a custom domain.
     * @param domain
     * @return an optional user if a user has the domain mapped
     */
  Optional<User> getUserByDomain(String domain);

    /**
     * Lookup a user by an Tent entity.
     * @param entity
     * @return an optional user if entity exists locally
     */
  Optional<User> getUserByEntity(final String entity);

    List<User> getAll();

    void save(User user);

}
