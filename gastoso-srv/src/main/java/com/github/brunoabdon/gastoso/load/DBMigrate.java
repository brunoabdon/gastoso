/*
 * Copyright (C) 2015 Bruno Abdon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.brunoabdon.gastoso.load;

import java.util.StringTokenizer;
import org.flywaydb.core.Flyway;

/**
 *
 * @author Bruno Abdon
 */
public class DBMigrate {
    
    public static void main(String[] args) {
        
        final String databaseUrl = System.getenv("DATABASE_URL");
        final StringTokenizer st = new StringTokenizer(databaseUrl, ":@/");
//        final String dbVendor = 
        st.nextToken();
        final String userName = st.nextToken();
        final String password = st.nextToken();
        final String host = st.nextToken();
        final String port = st.nextToken();
        final String databaseName = st.nextToken();
        
        final String jdbcUrl = 
            String.format(
                "jdbc:postgresql://%s:%s/%s?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory", 
                host, 
                port, 
                databaseName);
        
        final Flyway flyway = new Flyway();
        
        flyway.setDataSource(jdbcUrl, userName, password);

        flyway.migrate();
    }
}