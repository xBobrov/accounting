SELECT cal.service_id             AS service,
       acc.is_normed              AS isNormed,
       acc.number                 AS account,
       acc.payer                  AS payer,
       acc.address                AS address,
       acc.resident_regd          AS residents,
       trf.rate                   AS tariff,
       nrm.rate                   AS norm,
       CASE
           WHEN cal.service_id = 3
               THEN SUM(rdg.consumption) OVER ()
           ELSE rdg.consumption
           END                    AS consumption,
       cal.amount                 AS sum,
       COALESCE(cal.method_id, 0) AS code
FROM account acc
         LEFT JOIN calculation cal
                   ON acc.id = cal.account_id
         LEFT JOIN
     (SELECT DISTINCT ON (service_id) service_id, rate
      FROM norm
      ORDER BY service_id, impl_date DESC) nrm
     ON nrm.service_id = cal.service_id
         LEFT JOIN (SELECT DISTINCT ON (service_id) service_id, rate
                    FROM tariff
                    ORDER BY service_id, impl_date DESC) trf
                   ON trf.service_id = cal.service_id
         LEFT JOIN
     (SELECT mtr.account_id,
             mtr.service_id,
             SUM(rdg.consumption) AS consumption
      FROM meter mtr
               LEFT JOIN reading rdg
                         ON rdg.meter_id = mtr.id AND rdg.date = :readingDate
      GROUP BY mtr.account_id, mtr.service_id) rdg
     ON rdg.account_id = acc.id AND rdg.service_id = cal.service_id
WHERE acc.number = :accountNumber AND cal.date = :billPeriod
ORDER BY cal.service_id