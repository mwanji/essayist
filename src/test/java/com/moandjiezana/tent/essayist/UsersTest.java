package com.moandjiezana.tent.essayist;

import com.moandjiezana.essayist.utils.Tasks;
import com.moandjiezana.tent.essayist.db.DbSetup;
import com.moandjiezana.tent.essayist.db.DbTest;
import fj.data.Option;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: pjesi
 * Date: 1/15/13
 * Time: 11:14 PM
 */
public class UsersTest extends DbTest {


    private Users users;

    @Before
    public void setup(){

        super.setup();
        users = new Users(getTasks(), getDb().getQueryRunner());

    }

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

        Option<User> pjesi = users.getUserByEntity("http://pjesi.com");
        assertTrue(pjesi.isSome());

    }

    @Test
    public void should_persist_user_domain(){
        User user = new User("http://pjesi.com");
        user.setDomain("essays.pjesi.com");
        users.save(user);

        Option<User> pjesi = users.getUserByDomain("essays.pjesi.com");
        assertTrue(pjesi.isSome());

    }

}
