package di.madgik.cloudb.adapter;

import com.foundationdb.sql.query.OutputColumn;
import com.foundationdb.sql.query.SQLQuery;
import com.foundationdb.sql.query.Table;

/**
 * Created by panossakkos on 2/21/14.
 */

public class CloudQuery {

    protected final SQLQuery sqlQuery;

    public RootQuery rootQuery = new RootQuery();
    public InternalQuery internalQuery = new InternalQuery();
    public LeafQuery leafQuery = new LeafQuery();

    public CloudQuery(SQLQuery sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String toShellSQLString() {
        StringBuilder output = new StringBuilder();

        output.append("SQL query:\n\t");
        output.append(this.sqlQuery.toSQLString());
        output.append("\nRoot query:\n\t");
        output.append(this.rootQuery.toSQLString());
        output.append("\nInternal query:\n\t");
        output.append(this.internalQuery.toSQLString());
        output.append("\nLeaf query:\n\t");
        output.append(this.leafQuery.toSQLString());

        return output.toString();
    }

    public String toSQLString() {

        this.rootQuery.outputColumns = this.internalQuery.outputColumns = this.leafQuery.outputColumns;
        this.rootQuery.inputTables.addAll(0, this.leafQuery.inputTables);
        this.internalQuery.inputTables.addAll(0, this.leafQuery.inputTables);

        for (OutputColumn column : this.rootQuery.outputColumns) {
            column.outputName = null;
        }

        for (OutputColumn column : this.internalQuery.outputColumns) {
            column.outputName = null;
        }

        StringBuilder output = new StringBuilder();

        output.append(this.rootQuery.toSQLString().
                replace("Internal internal", "\n(" + this.internalQuery.toSQLString().
                replace("Leaf leaf", "\n(" + this.leafQuery.toSQLString() + ") as leaf ") + ") as internal"));

        return output.toString();
    }

    public String toAdpSqlString() {
        StringBuilder output = new StringBuilder();

        output.append("DISTRIBUTED CREATE TEMPORARY TABLE leaf as direct\n");
        output.append(this.leafQuery.toSQLString() + ";\n");

        output.append("\nDISTRIBUTED CREATE TEMPORARY TABLE internal to 1 as tree\n");
        output.append(this.internalQuery.toSQLString() + ";\n");

        output.append("\nDISTRIBUTED CREATE TEMPORARY TABLE root as\n");
        output.append(this.rootQuery.toSQLString() + ";\n");

        return output.toString();
    }

    @Override
    public String toString () {
        StringBuilder output = new StringBuilder();

        output.append("SQL query:");
        output.append(this.sqlQuery);
        output.append("\nRoot query:");
        output.append(this.rootQuery);
        output.append("\nInternal query:");
        output.append(this.internalQuery);
        output.append("\nLeaf query:");
        output.append(this.leafQuery);

        return output.toString();
    }
}
