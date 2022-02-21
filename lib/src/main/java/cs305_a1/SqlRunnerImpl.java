package cs305_a1;

import org.apache.commons.beanutils.PropertyUtils;

import javax.xml.xpath.XPathExpressionException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for executing SQL statements for CRUD operations.
 */
public class SqlRunnerImpl implements SqlRunner {
    private Connection dbc;
    private Path queriesFilePath;

    /**
     * Creates an instance of the class.
     * @param dbc DB connection on which to operate.
     * @param queriesFilePath Path of XML file containing queries.
     */
    public SqlRunnerImpl(Connection dbc, Path queriesFilePath) {
        this.dbc = dbc;
        this.queriesFilePath = queriesFilePath;
    }


    @Override
    public Object selectOne(String queryId, Object queryParam, Class resultType) throws DbAccessException {
        List<?> items = selectMany(queryId, queryParam, resultType);
        if (items == null || items.isEmpty()) return null;
        else if (items.size() > 1) throw new DbAccessException("Query returned more than one results!");
        else return items.get(0);
    }

    /**
     * Maps the current row in the given ResultSet to the given class object.
     * @param rs ResultSet instance to use for getting the columns values.
     * @param resType Class/type whose instance will be created and populated with columns values.
     * @return Populated instances of the given class.
     * @throws SQLException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Object mapRow(ResultSet rs, Class resType) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ResultSetMetaData rsmd = rs.getMetaData();
        Object obj = resType.getDeclaredConstructor().newInstance();
        for (int i=1; i<=rsmd.getColumnCount(); i++) {
            String colNm = rsmd.getColumnLabel(i);
            Object val = rs.getObject(i);
            PropertyUtils.setNestedProperty(obj, colNm, val);
        }
        return obj;
    }

    @Override
    public List<?> selectMany(String queryId, Object queryParam, Class resultItemType) throws DbAccessException {
        List<Object> results = new ArrayList<>();
        try {
            ResultSet rs = (ResultSet) executeQuery(queryId, queryParam, QueryType.SELECT);
            while (rs.next()) {
                Object obj = mapRow(rs, resultItemType);
                results.add(obj);
            }
        } catch (Exception ex) {
            throw new DbAccessException("Failed to run SQL.", ex);
        }
        return results;
    }

    /**
     * Executes the specified SQL query from the XML file.
     * @param queryId ID of the SQL query in XML file.
     * @param queryParam Parameter object to be used for placeholders in the query.
     * @param qt Type of the query.
     * @return Result of the query execution. It will depend on the type of the query executed.
     * If the query type is INSERT, then the PK of the inserted row will be returned,
     * if the type is UPDATE or DELETE then the number of rows affected will be returned,
     * if the type is SELECT then the object(s) selected will be returned after mapping.
     * @throws XPathExpressionException
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private Object executeQuery(String queryId, Object queryParam, QueryType qt) throws XPathExpressionException,
            SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String[] sqlInfo = Utils.getSqlAndParamType(queryId, queriesFilePath);
        List<String> params = Utils.extractOrderedParams(sqlInfo[0]);
        String qry = Utils.replaceNamedParams(sqlInfo[0], params);
        PreparedStatement ps = dbc.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
        for (int i=0; i<params.size(); i++) {
            String pname = params.get(i);
            Object parVal = null;
            // Special case for setting primitive/scalar value
            if ("this".equals(pname)) {
                parVal = queryParam;
            } else {
                parVal = PropertyUtils.getNestedProperty(queryParam, pname);
            }
            ps.setObject(i+1, parVal);
        }
        if (qt.equals(QueryType.UPDATE) || qt.equals(QueryType.DELETE)) {
            return ps.executeUpdate();
        } else if (qt.equals(QueryType.INSERT)){
            int rc = ps.executeUpdate();
            if (rc > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                return rs.getObject(1);
            } else {
                throw new RuntimeException("Insert failed.");
            }
        } else if (qt.equals(QueryType.SELECT)) {
            return ps.executeQuery();
        } else {
            throw new IllegalArgumentException("Invalid query type supplied: "+qt);
        }
    }

    @Override
    public int update(String queryId, Object queryParam) throws DbAccessException {
        int rc = 0;
        try {
            rc = (int) executeQuery(queryId, queryParam, QueryType.UPDATE);
        } catch (Exception e) {
            throw new DbAccessException("Failed to update the DB.", e);
        }
        return rc;
    }

    @Override
    public Object insert(String queryId, Object queryParam) throws DbAccessException {
        try {
            return executeQuery(queryId, queryParam, QueryType.INSERT);
        } catch (Exception e) {
            throw new DbAccessException("Failed to insert the record into the DB.", e);
        }
    }

    @Override
    public int delete(String queryId, Object queryParam) throws DbAccessException {
        try {
            return (int) executeQuery(queryId, queryParam, QueryType.DELETE);
        } catch (Exception e) {
            throw new DbAccessException("Failed to update the DB.", e);
        }
    }
}
