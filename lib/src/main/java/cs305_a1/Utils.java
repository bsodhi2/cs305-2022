package cs305_a1;

import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the utility methods for XML processing and SQL param handling.
 */
public class Utils {
    /**
     * Returns the SQL query and the paramType attribute from a given query in the XML file.
     * @param id Query ID
     * @param queriesFile Path of the XML file.
     * @return An array of strings whose first element is the SQL query, second element is the
     * value of the paramType attribute of the sql element in XML file.
     * @throws XPathExpressionException
     */
    public static String[] getSqlAndParamType(String id, Path queriesFile) throws XPathExpressionException {
        String[] sql = {"", ""};
        XPath xp = XPathFactory.newInstance().newXPath();
        InputSource in = new InputSource(queriesFile.toString());
        sql[0] = (String) xp.evaluate("//sql[@id='"+id+"']/text()", in, XPathConstants.STRING);
        sql[1] = (String) xp.evaluate("//sql[@id='"+id+"']/@paramType", in, XPathConstants.STRING);
        return sql;
    }

    public static void main(String[] a) throws XPathExpressionException {
        String[] s = Utils.getSqlAndParamType("test2", Path.of("/home/xuser/temp/cs305-22/assignment1/queries.xml"));
        System.out.println("SQL: "+s[0]+"\nPar: "+s[1]);
        System.out.println("Params: "+ extractOrderedParams(s[0]));
        System.out.println("After params replace: "+replaceNamedParams(s[0], extractOrderedParams(s[0])));
    }

    /**
     * Given an SQL query, it returns the list of param names (placeholders) in the order in which
     * they appear in the SQL query.
     * @param sql The SQL query to parse.
     * @return
     */
    public static List<String> extractOrderedParams(String sql) {
        List<String> params = new ArrayList<>();
        for (int i=0; i<sql.length();) {
            int t1 = sql.indexOf("${", i);
            if (t1 != -1) {
                int t2 = sql.indexOf("}", t1);
                if (t2 == -1) throw new IllegalStateException(
                        "Improper SQL placeholders! Missing a closing '}' after "+t1);
                params.add(sql.substring(t1+2, t2));
                i = t2;
            } else {
                break;
            }
        }
        return params;
    }

    /**
     * Replaces the named params (i.e., placeholder literals) present in the given SQL
     * with the '?' character, so that the SQL can be used to make a PreparedStatement object.
     * @param sql
     * @param params
     * @return
     */
    public static String replaceNamedParams(String sql, List<String> params) {
        for (String p : params) {
            sql = sql.replace("${"+p+"}", "?");
        }
        return sql;
    }
}
