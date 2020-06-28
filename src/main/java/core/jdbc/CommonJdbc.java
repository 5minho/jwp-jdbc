package core.jdbc;

import core.jdbc.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommonJdbc implements JdbcOperation {

    private static final Logger log = LoggerFactory.getLogger(CommonJdbc.class);

    private final Connection connection;

    public CommonJdbc() {
        connection = TransactionManager.getConnection();
    }

    @Override
    public <T> T queryForSingleObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) throws DataAccessException {
        ResultSet rs = null;
        try (
                final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            Objects.requireNonNull(pss).setValues(pstmt);
            rs = pstmt.executeQuery();

            T ret = null;
            if (rs.next()) {
                ret = rowMapper.mapRow(rs, 1);
            }
            return ret;
        } catch (SQLException throwables) {
            throw new DataAccessException("Unable to access to datasource.", throwables);
        } finally {
            close(rs);
        }
    }

    @Override
    public <T> T queryForSingleObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return queryForSingleObject(sql, rowMapper, ps -> setArguments(ps, args));
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) throws DataAccessException {
        ResultSet rs = null;
        try (
                final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            rs = pstmt.executeQuery();

            final List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rowNum++));
            }
            return results;
        } catch (SQLException throwables) {
            throw new DataAccessException(":'(");
        } finally {
            close(rs);
        }
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return query(sql, rowMapper, ps -> setArguments(ps, args));
    }

    @Override
    public int update(String sql, PreparedStatementSetter pss) {
        try (
                final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            Objects.requireNonNull(pss).setValues(pstmt);
            final int affectedRows = pstmt.executeUpdate();
            log.debug("affected rows: {}", affectedRows);
            return affectedRows;
        } catch (SQLException throwables) {
            log.debug(throwables.getMessage(), throwables);
            throw new DataAccessException("메세지는 나중에 적자..");
        }
    }

    @Override
    public int update(String sql, Object... args) throws DataAccessException {
        return update(sql, ps -> {
            final int length = args != null ? args.length : 0;
            for (int i = 1; i <= length; i++) {
                ps.setObject(i, args[i - 1]);
            }
        });
    }

    private void setArguments(PreparedStatement pstmt, Object[] values) throws SQLException {
        final int length = values != null ? values.length : 0;
        for (int i = 1; i <= length; i++) {
            pstmt.setObject(i, values[i - 1]);
        }
    }

    private void close(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to close object.");
        }
    }

}