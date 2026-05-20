SELECT acc.number        AS account,
       mtr.serial_number AS meterNumber,
       --CASE
       --    WHEN mtr.service_id = 1
        --       THEN 'ХВ'
        --   ELSE 'ГВ'
       --    END           AS service,
       mtr.service_id AS service,
       rdg.value AS value,
       mtr.valid_thru AS valid,
       rdg.consumption AS consumption
FROM account acc
         LEFT JOIN meter mtr
                   ON mtr.account_id = acc.id
         LEFT JOIN reading rdg
                   ON rdg.meter_id = mtr.id AND rdg.date = :readingDate
WHERE acc.number = :accountNumber