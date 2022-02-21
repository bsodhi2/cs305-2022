package cs305_a1;

import cs305_a1.dto.Address;
import cs305_a1.dto.Country;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqlRunnerImplTest {

    private SqlRunner sr;
    private Connection conn;
    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://localhost/sakila?user=app_user&password=test123");
        sr = new SqlRunnerImpl(conn, Path.of("/home/xyzuser/temp/cs305-22/assignment1/queries.xml"));
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void selectOne() throws DbAccessException {
        Address ad = (Address) sr.selectOne("selectAddress", 2, Address.class);
        assertEquals(2, ad.getId());
        assertEquals("28 MySQL Boulevard", ad.getAddress());
        assertEquals("QLD", ad.getDistrict());
        assertEquals("", ad.getPhone());

        // Nested
        ad = (Address) sr.selectOne("test2", 3, Address.class);
        assertEquals(3, ad.getId());
        assertEquals("23 Workhaven Lane", ad.getAddress());
        assertEquals("Alberta", ad.getDistrict());
        assertEquals("14033335568", ad.getPhone());
        assertEquals("Lethbridge", ad.getCity().getCity());
        assertEquals("Canada", ad.getCity().getCountry().getCountry());
    }

    @Test
    void selectMany() throws DbAccessException {
        List<Address> al = (List<Address>) sr.selectMany("getAddrList", "Canada", Address.class);
        assertEquals(7, al.size());
    }

    @Test
    void update() throws DbAccessException {
        Address sd = new Address();
        sd.setAddress2("THIS IS A NEW ONE");
        sd.setId(481);
        sr.update("updateAddr", sd);
    }

    @Test
    void insert() throws DbAccessException {
        Country ct = new Country();
        ct.setCountry("NEW COUNTRY");
        ct.setLastUpdated(new Date());
        BigInteger id = (BigInteger) sr.insert("addCountry", ct);
        assertTrue(id.intValue() > 0);
    }

    @Test
    void delete() {
        //TODO;
    }
}