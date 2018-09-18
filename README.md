# Phenix-Challenge

This project has the purpose of parsing transactions, and computing the top sold products
over a day or a week, among a store or all stores, by quantities or benefits.
It is divided into three modules, each doing a part of the processing :
- indexer : parse transactions files and index them into separate folders, by date/store/product
- aggregator : join prices of the products to the precedent indexed transactions, aggregate them by
date/store/product, then reindex them along the same schema (each file will then contain one aggregated line)
- top : use the indexed aggregation to compute the tops

There is a fourth module root, executing the three operations alltogether, and containing the final archive.

## Full process archive

execution : `scala -J-Xmx512M root/target/scala-2.11/phenix-challenge.jar
[--from/-f date] [--to/-t date] [--date/-d date] [--weeks/-w] [--global/-g]
[--in/-i "path/to/input"] [--out/-o path/to/output]`

- (from, to), date : select a date or a range of date to operate. (from, to) and date should be mutually exclusive
however when used together the last one is taken. format : yyyy-MM-dd
- weeks : tell the top to extends the selection so the interval of date begin a monday and end a sunday.
In this case, it computes weekly statistics
example : --date 2017-09-18 --weeks becomes --from 2017-09-17 --to 2017-09-23
- global : tell the top to compute statistics among all stores alltogether
- in : directory where the transactions files and product references files are located.
Transactions files must be named as "transactions_yyyyMMdd.data". References files format must be "reference_prod-storename_yyyyMMdd".
- out : directory where to store the top created. by default : /tmp/transactions/final. The output formats are
"top_100_(ventes|ca)_(storename|GLOBAL)_yyyyMMdd(-J7).data". "ventes" and "ca" are always generated together.
if --global, storename is replaced by GLOBAL. if --weeks, "-J7" is added, preceded by the monday of the week.

The output format of a line in a top is :
productId|store|yyyyMMdd|quantity|totalPrice

## Indexer

execution : `scala -J-Xmx512M indexer/target/scala-2.11/indexer.jar
[--from/-f date] [--to/-t date] [--date/-d date] [--weeks/-w] [--path/-p "path/to/input"] [--save/-s path/to/output]`

- (from, to), date : select a date or a range of date to operate. (from, to) and date should be mutually exclusive
however when used together the last one is taken. format : yyyy-MM-dd
- weeks : tell the indexer to extends the selection so the interval of date begin a monday and end a sunday
example : --date 2017-09-18 --weeks becomes --from 2017-09-17 --to 2017-09-23
- path : directory where the transactions files are located. Transactions files must be named as "transactions_yyyyMMdd.data".
- save : directory where to store the indexed transactions. by default : /tmp/transactions/indexed-transactions

## Aggregator

execution : `scala -J-Xmx512M aggregator/target/scala-2.11/aggregator.jar
[--from/-f date] [--to/-t date] [--date/-d date] [--weeks/-w]
[--indexed-files/-i "path/to/indexed/files"] [--prices-files/-p path/to/product/ref] [--save/-s path/to/output]`

- (from, to), date : select a date or a range of date to operate. (from, to) and date should be mutually exclusive
however when used together the last one is taken. format : yyyy-MM-dd
- weeks : tell the aggregator to extends the selection so the interval of date begin a monday and end a sunday
example : --date 2017-09-18 --weeks becomes --from 2017-09-17 --to 2017-09-23
- indexed-files : directory where the indexed files where computed by the indexer. By default /tmp/transactions/indexed-transactions
- prices-files : directory where the product references are stored. References files format must be "reference_prod-storename_yyyyMMdd.data"
- save : directory where to store the indexed aggregation. by default : /tmp/transactions/aggregated-transactions

## Top

execution : `scala -J-Xmx512M top/target/scala-2.11/top100.jar
[--from/-f date] [--to/-t date] [--date/-d date] [--weeks/-w] [--global/-g]
[--aggregated-files/-a "path/to/aggregations"] [--save/-s path/to/output]`

- (from, to), date : select a date or a range of date to operate. (from, to) and date should be mutually exclusive
however when used together the last one is taken. format : yyyy-MM-dd
- weeks : tell the top to extends the selection so the interval of date begin a monday and end a sunday.
In this case, it computes weekly statistics
example : --date 2017-09-18 --weeks becomes --from 2017-09-17 --to 2017-09-23
- global : tell the top to compute statistics among all stores alltogether
- path : directory where the aggregated files are located. by default : /tmp/transactions/aggregated-transactions
- save : directory where to store the top created. by default : /tmp/transactions/final. The output formats are
"top_100_(ventes|ca)_(storename|GLOBAL)_yyyyMMdd(-J7).data". "ventes" and "ca" are always generated together.
if --global, storename is replaced by GLOBAL. if --weeks, "-J7" is added, preceded by the monday of the week.
