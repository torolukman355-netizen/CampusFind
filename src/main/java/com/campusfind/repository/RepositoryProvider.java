package com.campusfind.repository;

/**
 * Abstraksi penyedia seluruh Repository (DAO Layer) yang dirancang siap-SQL (SQL-Ready).
 * Aplikasi cukup bergantung pada interface ini sehingga implementasi dapat berpindah dari
 * penyimpanan File lokal ke JDBC / MySQL / PostgreSQL tanpa mengubah Service maupun Controller Web.
 */
public interface RepositoryProvider {
    ItemRepository items();
    KlaimRepository klaim();
    PosRepository pos();
    NotificationRepository notifications();
    UserRepository users();
    String getBackendType();
}
