package com.jpa.entity_manage;

import javax.sql.DataSource;

public interface ICommonDSSetup {
   void  setUrl(String s);
   void setUsername(String s);
   void setPassword(String s);
}
