package cs305_a1;

import org.apache.commons.beanutils.PropertyUtils;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class SqlRunnerImpl implements SqlRunner {
    private Connection dbc;
    private Path queriesFilePath;

    @Override
    public Object selectOne(String queryId, Object queryParam, Class resultType) throws DbAccessException {
        Object result = null;
        try {
            String[] sqlInfo = Utils.getSqlAndParamType(queryId, queriesFilePath);
            List<String> params = Utils.extractOrderedParams(sqlInfo[0]);
            String qry = Utils.replaceNamedParams(sqlInfo[0], params);
            PreparedStatement ps = dbc.prepareStatement(qry);
            for (int i=0; i<params.size(); i++) {
                Object val = PropertyUtils.getNestedProperty(queryParam, params.get(i));
                ps.setObject(i+1, val);
            }
            ResultSet rs = ps.executeQuery();
            int rc = 0;
            while (rs.next()) {
                if (rc > 0) throw new RuntimeException("Excepted a single result, but query returned more than 1 rows.");
                result = mapRow(rs, resultType);
                rc++;
            }
        } catch (Exception ex) {
            throw new DbAccessException("Failed to run SQL.", ex);
        }
        return result;
    }

    private Object mapRow(ResultSet rs, Class resultType) {
        return null;
    }

    @Override
    public List<?> selectMany(String queryId, Object queryParam, Class resultItemType) throws DbAccessException {
        return null;
    }

    @Override
    public int update(String queryId, Object queryParam) {
        return 0;
    }

    @Override
    public int insert(String queryId, Object queryParam) throws DbAccessException {
        return 0;
    }

    @Override
    public int delete(String queryId, Object queryParam) throws DbAccessException {
        return 0;
    }
}
