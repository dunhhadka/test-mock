package com.example.demo.repository;

import com.example.demo.entity.OrderServiceEntity;
import com.example.demo.model.params.StatisticServiceParams;
import com.example.demo.model.params.TimeParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderServiceRepository extends BaseRepository<OrderServiceEntity> {

        @Query(value = "select * " +
                        "from order_service " +
                        "where order_id = ?1", nativeQuery = true)
        List<OrderServiceEntity> getAllByOrderId(Long orderId);

        @Query(value = "SELECT " +
                        "    o_s.service_id AS service_id, " +
                        "    s.code AS service_code, " +
                        "    SUM(o_s.quantity) AS total_quantity, " +
                        "    o_s.name AS order_service_name, " +
                        "    o_s.price AS order_service_price, " +
                        // " o_s.salary_dispatcher AS salary_dispatcher, " +
                        // " o_s.salary_repairer AS salary_repairer, " +
                        // " (o_s.price - o_s.salary_dispatcher - o_s.salary_repairer) AS total_profit,
                        // " +
                        "    (o_s.price) * SUM(o_s.quantity) AS total_profit_sum "
                        +
                        "FROM " +
                        "    order_service AS o_s " +
                        "INNER JOIN " +
                        "    services AS s ON o_s.service_id = s.id " +
                        "WHERE " +
                        "(:#{#params.startTime} is null or :#{#params.endTime} is null or " +
                        "o_s.create_date between :#{#params.startTime} and :#{#params.endTime}) " +
                        "GROUP BY " +
                        "    s.code, o_s.service_id, o_s.name, o_s.price " +
                        "ORDER BY " +
                        "    o_s.service_id", nativeQuery = true)
        Page<Object[]> statisticService(@Param("params") StatisticServiceParams statisticServiceParams,
                        Pageable pageable);

        @Query(value = "select o_s.service_id , s.name, s.price ,sum(quantity) as sum " +
                        "from order_service as o_s " +
                        "inner join services as s on s.id = o_s.service_id " +
                        "where " +
                        "(:#{#time.startTime} is null or :#{#time.endTime} is null " +
                        "or o_s.create_date between :#{#time.startTime} and :#{#time.endTime}) " +
                        "group by o_s.service_id " +
                        "order by sum desc;", nativeQuery = true)
        List<Object[]> statisticTopInService(@Param("time") TimeParams timeParams);

        @Query(value = "with recursive dates as ( " +
                        "      select curdate() - interval (1 - day(curdate())) day as yyyymm, 1 as lev " +
                        "      union all " +
                        "      select yyyymm - interval 1 month, lev + 1 " +
                        "      from dates " +
                        "      where lev < 12 " +
                        "     ) " +
                        "select DATE_FORMAT(d.yyyymm, '%m') as month, DATE_FORMAT(d.yyyymm, '%Y') as year, sum(t.price*t.quantity) as cnt "
                        +
                        "from dates d left join " +
                        "     order_service t " +
                        "     on t.create_date >= d.yyyymm - interval 1 month and  " +
                        "        t.create_date < d.yyyymm  " +
                        "group by month, year " +
                        "order by year, month;", nativeQuery = true)
        List<Object[]> statisticServiceUsageInTimeMonth();

}
