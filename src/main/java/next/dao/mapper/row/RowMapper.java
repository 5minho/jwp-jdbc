package next.dao.mapper.row;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
    public T mapResult(ResultSet rs) throws SQLException;
}
