WITH inserted AS (
INSERT INTO calculation (account_id, service_id, date, amount, method_id)
SELECT acc.id       AS account_id,
       srv.id       AS service_id,
       CURRENT_DATE AS date,
       CASE
           WHEN acc.is_normed = true THEN crg.rate * GREATEST(acc.resident_regd, 1)
           ELSE
               CASE
                   WHEN srv.id = 3 THEN
                       CASE
                           WHEN swr.consumption IS NULL THEN crg.rate * GREATEST(acc.resident_regd, 1)
                           ELSE ROUND(swr.consumption * crg.tariff::numeric, 2)
                           END
                   ELSE
                       CASE
                           WHEN rdg.consumption IS NULL THEN crg.rate * GREATEST(acc.resident_regd, 1)
                           ELSE ROUND(rdg.consumption * crg.tariff::numeric, 2)
                           END
                   END
           END      AS amount,
       CASE
           WHEN acc.is_normed = true THEN 2
           ELSE
               CASE
                   WHEN srv.id = 3 THEN
                       CASE
                           WHEN swr.consumption IS NULL THEN 2
                           ELSE 1
                           END
                   ELSE
                       CASE
                           WHEN rdg.consumption IS NULL THEN 2
                           ELSE 1
                           END
                   END
           END      AS method_id
FROM account acc
         CROSS JOIN service srv
         JOIN (SELECT nrm.service_id                         AS srv_id,
                      ROUND(nrm.rate * trf.rate::numeric, 2) AS rate,
                      trf.rate                               AS tariff
               FROM (SELECT DISTINCT ON (service_id) service_id, rate
                     FROM norm
                     ORDER BY service_id, impl_date DESC) AS nrm
                        JOIN (SELECT DISTINCT ON (service_id) service_id, rate
                              FROM tariff
                              ORDER BY service_id, impl_date DESC) AS trf
                             ON nrm.service_id = trf.service_id) AS crg
              ON crg.srv_id = srv.id
         LEFT JOIN (SELECT mtr.account_id,
                           mtr.service_id,
                           CASE
                               WHEN BOOL_AND(rdgn.consumption IS NOT NULL) THEN SUM(rdgn.consumption)
                               ELSE NULL
                               END AS consumption
                    FROM meter mtr
                             LEFT JOIN
                         (SELECT meter_id,
                                 consumption
                          FROM reading
                          WHERE date = :date) AS rdgn
                         ON mtr.id = rdgn.meter_id
                    GROUP BY mtr.account_id, mtr.service_id) AS rdg
                   ON rdg.account_id = acc.id AND rdg.service_id = srv.id
         LEFT JOIN (SELECT account_id,
                           CASE
                               WHEN BOOL_AND(rdgn.consumption IS NOT NULL) THEN SUM(rdgn.consumption)
                               ELSE NULL
                               END AS consumption
                    FROM meter mtr
                             LEFT JOIN
                         (SELECT meter_id,
                                 consumption
                          FROM reading
                          WHERE date = :date) rdgn
                         ON mtr.id = rdgn.meter_id
                    GROUP BY mtr.account_id) AS swr
                   ON swr.account_id = acc.id
WHERE acc.is_active = true
    )
INSERT INTO transaction (date, sum, account_id)
SELECT
            CURRENT_DATE,
            -SUM(amount),
            account_id
FROM inserted
GROUP BY account_id