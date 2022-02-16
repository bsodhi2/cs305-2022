package cs305_a1;

import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Utils {
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

    public static String replaceNamedParams(String sql, List<String> params) {
        for (String p : params) {
            sql = sql.replace("${"+p+"}", "?");
        }
        return sql;
    }
}
