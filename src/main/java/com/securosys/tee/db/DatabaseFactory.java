/**
 * Copyright (c)2025 Securosys SA, authors: Tomasz Madej
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 **/
package com.securosys.tee.db;

import com.securosys.tee.dto.JvmInput;

public class DatabaseFactory {
    public static DatabaseAdapter create(JvmInput input) {
        if(input.getDatabase()==null){
            throw new UnsupportedOperationException("JvmInput does not contains database connection.");
        }
        DatabaseDialect databaseDialect = DatabaseDialect.fromUrl(input.getDatabase().getUrl());
        switch (databaseDialect) {
            case POSTGRESQL:
                return new PostgresDialectAdapter(input.getDatabase(),input.getMasterDatabase());
            case MARIADB:
                return new MariaDbDialectAdapter(input.getDatabase());
            case H2:
                return new H2DialectAdapter(input.getDatabase());
            default:
                throw new UnsupportedOperationException("Unsupported database dialect");
        }
    }
    public static void loadDriver(JvmInput input) {
        if(input.getDatabase()==null){
            throw new UnsupportedOperationException("JvmInput does not contains database connection.");
        }
        DatabaseDialect dialect=DatabaseDialect.fromUrl(input.getDatabase().getUrl());
        String driverClassName = getDriverClassName(dialect);

        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found for: " + dialect, e);
        }
    }

    private static String getDriverClassName(DatabaseDialect dialect) {
        switch (dialect) {
            case POSTGRESQL:
                return "org.postgresql.Driver";
            case MARIADB:
                return "org.mariadb.jdbc.Driver";
            case H2:
                return "org.h2.Driver";

        }
        return null;
    }
}
