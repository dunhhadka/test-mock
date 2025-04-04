package com.example.demo.repository;

import com.example.demo.entity.HistoryEntity;
import com.example.demo.model.params.HistoryParams;
import com.example.demo.model.params.StatisticInYear;
import com.example.demo.model.params.StatisticProductParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryProductRepository extends BaseRepository<HistoryEntity> {
        @Query("SELECT h FROM HistoryEntity h WHERE " +
                        "(lower(h.productName) like lower(concat('%',:productName,'%')) OR :productName IS NULL) AND " +
                        "(lower(h.action) like lower(concat('%',:action,'%')) OR :action IS NULL) " +
                        "ORDER BY " +
                        "CASE WHEN :orderBy = 'productNameAsc' THEN  h.productName END ASC," +
                        "CASE WHEN :orderBy = 'productNameDesc' THEN  h.productName END DESC ")
        Page<HistoryEntity> findByNameAndAction(
                        @Param("productName") String productName,
                        @Param("action") String action,
                        @Param("orderBy") String orderBy,
                        Pageable pageable);

        @Query(value = "select * from history_product where "
                        + "(:#{#history.productName} is null or lower(history_product.product_name) like concat('%',lower(:#{#history.productName}),'%')) and "
                        + "(:#{#history.note} is null or lower(history_product.note) like concat('%',lower(:#{#history.note}),'%')) and "
                        + "(:#{#history.action} is null or history_product.action like concat('%',:#{#history.action},'%')) and "
                        + "(:#{#history.unit} is null or history_product.unit like concat('%',:#{#history.unit},'%')) and "
                        + "(:#{#history.priceIn} is null or history_product.price_in = :#{#history.priceIn}) and "
                        + "(:#{#history.priceOut} is null or history_product.price_out = :#{#history.priceOut}) and "
                        + "(:#{#history.startTime} is null or :#{#history.endTime} is null or history_product.create_date between :#{#history.startTime} and :#{#history.endTime})", nativeQuery = true)
        Page<HistoryEntity> getByConditions(@Param("history") HistoryParams historyParams, Pageable pageable);

        @Query(value = "select p.id , p.code, p.name , p.price_in , p.price_out , " +
                        "CAST(-1 * SUM(CASE WHEN h_p.difference < 0 THEN h_p.difference ELSE 0 END) AS SIGNED) AS ex, "
                        +
                        "CAST(SUM(CASE WHEN h_p.difference >= 0 THEN h_p.difference ELSE 0 END) AS SIGNED) AS im ," +
                        "-1*sum( " +
                        "case " +
                        " when h_p.difference >= 0 then h_p.difference*p.price_in " +
                        "        else h_p.difference*p.price_out " +
                        "end " +
                        ") as income " +
                        "from history_product as h_p " +
                        "inner join products as p on h_p.product_id = p.id " +
                        "group by p.id " +
                        ") " +
                        "" +
                        "select c.* , (c.price_out*ex - c.price_in*im) as income " +
                        "from Caculation as c " +
                        "order by c.id; ", nativeQuery = true)
        List<Object[]> statisticHistoryProduct();

        // @Query(value = "WITH CaculationIn AS (" +
        // "SELECT p.id, p.code, p.name, p.unit, p.price_in AS price," +
        // " SUM(h_p.difference) AS total_quantity_in," +
        // // " SUM(h_p.price_in) AS total_price_in," +
        // " SUM(h_p.price_in * h_p.difference) AS total_income_in " +
        // "FROM history_product AS h_p " +
        // "INNER JOIN products AS p ON h_p.product_id = p.id " +
        // "WHERE h_p.difference > 0 " +
        // "GROUP BY p.id, p.code, p.name, p.unit, p.price_in " +
        // ") " +
        // "SELECT c.id, c.code, c.name, c.unit, c.price," +
        // " c.total_quantity_in AS total_quantity," +
        // " (c.total_quantity_in * c.total_price_in) AS total_income " +
        // "FROM CaculationIn AS c " +
        // "ORDER BY c.id", nativeQuery = true)
        @Query(value = "WITH CalculationIn AS (" +
                        "SELECT h_p.product_id as id, p.code, h_p.product_name as name, h_p.unit, h_p.price_in AS price,"
                        +
                        "       SUM(h_p.difference) AS total_quantity_in," +
                        "       SUM(-h_p.price_in * h_p.difference) AS total_income_in " +
                        "FROM history_product AS h_p " +
                        "INNER JOIN products AS p ON h_p.product_id = p.id " +
                        "WHERE h_p.difference > 0 " +
                        "AND (:#{#params.startTime} is null or :#{#params.endTime} is null or " +
                        "h_p.create_date between :#{#params.startTime} and :#{#params.endTime}) " +
                        "GROUP BY h_p.product_id, p.code, h_p.product_name, h_p.unit, h_p.price_in " +
                        ") " +
                        "SELECT c_in.id, c_in.code, c_in.name, c_in.unit, c_in.price," +
                        "       c_in.total_quantity_in AS total_quantity," +
                        "       c_in.total_income_in AS total_income " +
                        "FROM CalculationIn AS c_in " +
                        "ORDER BY c_in.id", nativeQuery = true)
        Page<Object[]> statisticHistoryProductIn(
                        @Param("params") StatisticProductParams statisticProductParams, Pageable pageable);

        // @Query(value = "WITH CaculationOut AS (" +
        // "SELECT p.id, p.code, p.name, p.unit, p.price_out AS price," +
        // " SUM(h_p.difference) AS total_quantity_out," +
        // // " SUM(h_p.price_out) AS total_price_out," +
        // " SUM(h_p.price_out * h_p.difference) AS total_income_out " +
        // "FROM history_product AS h_p " +
        // "INNER JOIN products AS p ON h_p.product_id = p.id " +
        // "WHERE h_p.difference < 0 " +
        // "GROUP BY p.id, p.code, p.name, p.unit, p.price_out " +
        // ") " +
        // "SELECT c_out.id, c_out.code, c_out.name, c_out.unit, c_out.price," +
        // " c_out.total_quantity_out AS total_quantity," +
        // " (c_out.total_quantity_out * c_out.total_price_out) AS total_income " +
        // "FROM CaculationOut AS c_out " +
        // "ORDER BY c_out.id", nativeQuery = true)
        @Query(value = "WITH CalculationOut AS (" +
                        "SELECT h_p.product_id as id, p.code, h_p.product_name as name, h_p.unit, h_p.price_out AS price,"
                        +
                        "       SUM(-h_p.difference) AS total_quantity_out," +
                        "       SUM(-h_p.price_out * h_p.difference) AS total_income_out " +
                        "FROM history_product AS h_p " +
                        "INNER JOIN products AS p ON h_p.product_id = p.id " +
                        "WHERE h_p.difference < 0 " +
                        "AND (:#{#params.startTime} is null or :#{#params.endTime} is null or " +
                        "h_p.create_date between :#{#params.startTime} and :#{#params.endTime}) " +
                        "GROUP BY h_p.product_id, p.code, h_p.product_name, h_p.unit, h_p.price_out " +
                        ") " +
                        "SELECT c_out.id, c_out.code, c_out.name, c_out.unit, c_out.price," +
                        "       c_out.total_quantity_out AS total_quantity," +
                        "       c_out.total_income_out AS total_income " +
                        "FROM CalculationOut AS c_out " +
                        "ORDER BY c_out.id", nativeQuery = true)
        Page<Object[]> statisticHistoryProductOut(
                        @Param("params") StatisticProductParams statisticProductParams, Pageable pageable);

        @Query(value = "with recursive dates as ( " +
                        "select curdate() - interval (1 - day(curdate())) day as yyyymm, 1 as lev " +
                        "union all " +
                        "select yyyymm - interval 1 month, lev + 1 " +
                        "from dates " +
                        "where lev < 12) " +
                        "select DATE_FORMAT(d.yyyymm, '%m') as month, DATE_FORMAT(d.yyyymm, '%Y') as year, sum(hp.difference * hp.price_in) as expense "
                        +
                        "from dates d left join " +
                        "history_product hp " +
                        "on hp.create_date >= d.yyyymm - interval 1 month " +
                        "and hp.create_date < d.yyyymm  " +
                        "and hp.difference > 0 " +
                        "group by year, month " +
                        "order by year, month;", nativeQuery = true)
        List<Object[]> statisticProductInYear();

        @Query(value = "with recursive dates as ( " +
                        "select curdate() - interval (1 - day(curdate())) day as yyyymm, 1 as lev " +
                        "union all " +
                        "select yyyymm - interval 1 month, lev + 1 " +
                        "from dates " +
                        "where lev < 12) " +
                        "select DATE_FORMAT(d.yyyymm, '%m') as month, DATE_FORMAT(d.yyyymm, '%Y') as year, sum(hp.difference * hp.price_out) as expense "
                        +
                        "from dates d left join " +
                        "history_product hp " +
                        "on hp.create_date >= d.yyyymm - interval 1 month " +
                        "and hp.create_date < d.yyyymm  " +
                        "and hp.difference < 0 " +
                        "group by year, month " +
                        "order by year, month;", nativeQuery = true)

        List<Object[]> statisticProductOutInYear();

}

/*
 * 2023-05-16 2023-05-31
 * 0h00p 2023-05-16 \\ 00h s 2023-05-17
 */
