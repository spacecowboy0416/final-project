package com.finalproject.coordi.recommendation.mapper.typehandler;

import com.finalproject.coordi.recommendation.domain.enums.ProductEnums.ProductCategoryCode;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * ProductCategoryCode enum을 DB 코드 문자열로 매핑한다.
 */
public class ProductCategoryCodeTypeHandler extends BaseTypeHandler<ProductCategoryCode> {
    @Override
    public void setNonNullParameter(
        PreparedStatement ps,
        int i,
        ProductCategoryCode parameter,
        JdbcType jdbcType
    ) throws SQLException {
        ps.setString(i, parameter.getCode());
    }

    @Override
    public ProductCategoryCode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return resolve(rs.getString(columnName));
    }

    @Override
    public ProductCategoryCode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return resolve(rs.getString(columnIndex));
    }

    @Override
    public ProductCategoryCode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return resolve(cs.getString(columnIndex));
    }

    private ProductCategoryCode resolve(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        for (ProductCategoryCode value : ProductCategoryCode.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown ProductCategoryCode code: " + code);
    }
}
