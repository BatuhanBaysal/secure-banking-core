package com.batuhan.banking_service.repository;

import org.hibernate.dialect.H2Dialect;

/**
 * Custom Hibernate Dialect for H2.
 * Bridge class used to maintain compatibility between PostgreSQL production syntax
 * and H2 in-memory test database, ensuring specific SQL functions execute correctly during integration tests.
 */
public class H2PostgreSQLDialect extends H2Dialect {

}