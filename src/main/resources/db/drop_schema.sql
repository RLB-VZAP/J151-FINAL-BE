/*
    Tears the whole database down.

    Dropping the database removes every table, view and trigger with it, so
    there is nothing to enumerate. The previous version of this file listed
    individual DROP TABLE statements ahead of the DROP DATABASE, but they ran
    before any database had been selected, so the script aborted on line 1 with
    "No database selected" and never reached the drop that actually mattered.

    Usage:
      mysql -u <user> -p < drop_schema.sql
*/

DROP DATABASE IF EXISTS `tryton_fantasy_rugby`;
