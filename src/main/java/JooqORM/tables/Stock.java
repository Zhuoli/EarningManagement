/**
 * This class is generated by jOOQ
 */
package JooqORM.tables;


import JooqORM.Earningmanagerdb;
import JooqORM.Keys;
import JooqORM.tables.records.StockRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.8.4"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Stock extends TableImpl<StockRecord> {

    private static final long serialVersionUID = -2063172785;

    /**
     * The reference instance of <code>EarningManagerDB.stock</code>
     */
    public static final Stock STOCK = new Stock();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<StockRecord> getRecordType() {
        return StockRecord.class;
    }

    /**
     * The column <code>EarningManagerDB.stock.symbol</code>.
     */
    public final TableField<StockRecord, String> SYMBOL = createField("symbol", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

    /**
     * The column <code>EarningManagerDB.stock.report_date</code>.
     */
    public final TableField<StockRecord, String> REPORT_DATE = createField("report_date", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

    /**
     * The column <code>EarningManagerDB.stock.shared_average_cost</code>.
     */
    public final TableField<StockRecord, Double> SHARED_AVERAGE_COST = createField("shared_average_cost", org.jooq.impl.SQLDataType.FLOAT.nullable(false), this, "");

    /**
     * The column <code>EarningManagerDB.stock.shares</code>.
     */
    public final TableField<StockRecord, Integer> SHARES = createField("shares", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>EarningManagerDB.stock.target_price</code>.
     */
    public final TableField<StockRecord, Double> TARGET_PRICE = createField("target_price", org.jooq.impl.SQLDataType.FLOAT, this, "");

    /**
     * Create a <code>EarningManagerDB.stock</code> table reference
     */
    public Stock() {
        this("stock", null);
    }

    /**
     * Create an aliased <code>EarningManagerDB.stock</code> table reference
     */
    public Stock(String alias) {
        this(alias, STOCK);
    }

    private Stock(String alias, Table<StockRecord> aliased) {
        this(alias, aliased, null);
    }

    private Stock(String alias, Table<StockRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Earningmanagerdb.EARNINGMANAGERDB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<StockRecord> getPrimaryKey() {
        return Keys.KEY_STOCK_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<StockRecord>> getKeys() {
        return Arrays.<UniqueKey<StockRecord>>asList(Keys.KEY_STOCK_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stock as(String alias) {
        return new Stock(alias, this);
    }

    /**
     * Rename this table
     */
    public Stock rename(String name) {
        return new Stock(name, null);
    }
}
