package com.moandjiezana.tent.essayist.db;

import com.moandjiezana.essayist.utils.Tasks;
import com.moandjiezana.tent.essayist.Users;
import org.junit.After;
import org.junit.Before;

/**
 * User: pjesi
 * Date: 1/15/13
 * Time: 11:27 PM
 */
public class DbTest {

    private DbSetup db;
    private Tasks tasks;

    @Before
    public void setup(){
        db = new DbSetup();
        db.init();
        tasks = new Tasks();
    }

    @After
    public void tearDown(){
        db.destroy();
        db = null;
    }

    public DbSetup getDb() {
        return db;
    }

    public Tasks getTasks() {
        return tasks;
    }
}
