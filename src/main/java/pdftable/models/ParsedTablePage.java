package pdftable.models;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parsed page model.
 */
public class ParsedTablePage {

    public class ParsedTableRow {

        private List<String> cells;

        public ParsedTableRow(List<String> cells) {
            this.cells = cells;
        }

        public List<String> getCells() {
            return cells;
        }

        public String getCell(int index) {
            return cells.get(index);
        }

        @Override
        public String toString() {
            List<String> escapedCells = cells.stream().map(c -> StringEscapeUtils.escapeJava(c)).collect(Collectors.toList());
            return String.format("<%s@%s; cells:%s>",
                    this.getClass().getSimpleName(), System.identityHashCode(this), Arrays.toString(escapedCells.toArray()));
        }
    }

    private List<ParsedTableRow> rows;
    private int pageNum;

    private ParsedTablePage() {
        rows = new ArrayList<>();
    }

    public ParsedTablePage(int pageNumber) {
        this();
        pageNum = pageNumber;
    }

    public List<ParsedTableRow> getRows() {
        return rows;
    }

    public void addRow(List<String> cells) {
        rows.add(new ParsedTableRow(cells));
    }

    public ParsedTableRow getRow(int index) {
        return rows.get(index);
    }

    public int getPageNum() {
        return pageNum;
    }

    @Override
    public String toString() {
        return String.format("<%s@%s; rows:%s>",
                this.getClass().getSimpleName(), System.identityHashCode(this), Arrays.toString(rows.toArray()));
    }
}
