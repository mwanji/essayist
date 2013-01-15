package com.moandjiezana.tent.essayist.db.migrations;

import static com.eroi.migrate.Define.autoincrement;
import static com.eroi.migrate.Define.column;
import static com.eroi.migrate.Define.notnull;
import static com.eroi.migrate.Define.primarykey;
import static com.eroi.migrate.Define.table;
import static com.eroi.migrate.Define.DataTypes.LONGVARCHAR;
import static com.eroi.migrate.Define.DataTypes.VARCHAR;
import static com.eroi.migrate.Execute.createTable;

import com.eroi.migrate.Define;
import com.eroi.migrate.Execute;
import com.eroi.migrate.Migration;

public class Migration_1 implements Migration {
  
  @Override
  public void up() {
    createTable(table("AUTHORIZATIONS", 
        column("ID", Define.DataTypes.BIGINT, primarykey(), autoincrement(), notnull()),
        column("ENTITY", VARCHAR, notnull(), Define.length(1000)),
        column("PROFILE", LONGVARCHAR),
        column("REGISTRATION", LONGVARCHAR),
        column("DOMAIN", VARCHAR, notnull(), Define.length(1000)), // TODO this is probably cheating but not sure how to do migration
        column("ACCESSTOKEN", LONGVARCHAR)));
  }

  @Override
  public void down() {
    Execute.dropTable("authorizations");
  }

}
