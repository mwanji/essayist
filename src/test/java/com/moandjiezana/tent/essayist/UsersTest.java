package com.moandjiezana.tent.essayist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.moandjiezana.tent.essayist.db.DbTest;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * User: pjesi
 * Date: 1/15/13
 * Time: 11:14 PM
 */
public class UsersTest extends DbTest {


    private Users users;

    @Override
    @Before
    public void setup(){

        super.setup();
        users = new Users(getTasks(), getDb().getQueryRunner());

    }

    @Override
    @After
    public void tearDown(){
        super.tearDown();
        users = null;
    }

    @Test
    public void should_start_empty(){
        List<User> userList = users.getAll();
        assertEquals(0, userList.size());
    }

    @Test
    public void should_create_user(){

        User user = new User("http://pjesi.com");
        users.save(user);

        Optional<User> pjesi = users.getUserByEntity("http://pjesi.com");
        assertTrue(pjesi.isPresent());

    }

    @Test
    public void should_persist_user_domain(){
        User user = new User("http://pjesi.com");
        user.setDomain("essays.pjesi.com");
        users.save(user);

        Optional<User> pjesi = users.getUserByDomain("essays.pjesi.com");
        assertTrue(pjesi.isPresent());

    }

}
