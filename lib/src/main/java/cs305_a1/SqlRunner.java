package cs305_a1;

import java.util.List;

/**
 * A class implementing this interface will execute arbitrary SQL statements
 * (INSERT, UPDATE, SELECT, DELETE). The SQL queries will be specified in an
 * XML file named queries.xml by default and placed at a well-known location.
 * The structure of the XML file will be as follows:
 * <queries>
 *     <sql id="findMovies" paramType="org.foo.Bar">
 *      <![CDATA[
 *      SELECT a, b, c FROM my_table WHERE x IN (${propX[]}) AND y=${propY};
 *      ]]>
 *     </sql>
 *     <sql id="addMovie" paramType="org.foo.Bar">
 *        <![CDATA[
 *        INSERT INTO my_table(x, y, x) VALUES(${propX}, ${propY}, ${propZ});
 *        ]]>
 *     </sql>
 * </queries>
 * The value of columns selected by the query will be populated into the matching property of the class whose type will be specified in the selectOne or selectMany method’s arguments. Matching will be done in strict mode by default, which
 * means that if the class does not have a matching property for a selected
 * column, then the method will throw an exception.
 *
 * For “paramType”, the value can also be a primitive type such as int, float, string, or a collection/array of such primitives. In such a case the placeholder in the SQL query for the primitive type can be named ${value}, and the value supplied will be directly substituted when running the query.
 */
public interface SqlRunner {
    public enum QueryType {
        SELECT, UPDATE, INSERT, DELETE
    }
    /**
     * Executes a select query that returns a single or no record.
     * @param queryId Unique ID of the query in the queries.xml file.
     * @param queryParam Parameter(s) to be used in the query.
     * @param resultType Type of the object that will be returned after
     *                   populating it with the data returned by the SQL.
     * @return The object populated with the SQL results.
     */
    Object selectOne(String queryId, Object queryParam, Class resultType) throws DbAccessException;

    /**
     * Same as {@link #selectOne(String, Object, Class)} except that this one
     * returns multiple rows.
     * @param queryId
     * @param queryParam
     * @param resultItemType
     * @return
     */
    List<?> selectMany(String queryId, Object queryParam, Class resultItemType) throws DbAccessException;

    /**
     * Execute an update statement and return the number of rows affected.
     * @param queryId
     * @param queryParam
     * @return
     */
    int update(String queryId, Object queryParam) throws DbAccessException;

    /**
     * Execute an insert statement and return the number of rows affected.
     * @param queryId
     * @param queryParam
     * @return
     */
    Object insert(String queryId, Object queryParam) throws DbAccessException;

    /**
     * Execute a delete statement and return the number of rows affected.
     * @param queryId
     * @param queryParam
     * @return
     */
    int delete(String queryId, Object queryParam) throws DbAccessException;
}
