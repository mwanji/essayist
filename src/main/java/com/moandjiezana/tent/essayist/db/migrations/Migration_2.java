package com.moandjiezana.tent.essayist.db.migrations;

import static com.eroi.migrate.Define.length;
import static com.eroi.migrate.Define.notnull;
import static com.eroi.migrate.Define.DataTypes.VARCHAR;
import static com.eroi.migrate.Execute.addColumn;
import static com.eroi.migrate.Execute.dropColumn;

import com.eroi.migrate.Define;
import com.eroi.migrate.Migration;

public class Migration_2 implements Migration {

  @Override
  public void up() {
    addColumn(Define.column("DOMAIN", VARCHAR, notnull(), length(1000), Define.defaultValue("")), "AUTHORIZATIONS");
  }

  @Override
  public void down() {
    dropColumn("DOMAIN", "AUTHORIZATIONS");
  }

}
