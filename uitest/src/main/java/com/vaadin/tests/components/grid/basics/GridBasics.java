package com.vaadin.tests.components.grid.basics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.tests.components.AbstractTestUIWithLog;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.DetailsGenerator;
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.SingleSelectionModel;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.renderers.ProgressBarRenderer;

@Widgetset("com.vaadin.DefaultWidgetSet")
public class GridBasics extends AbstractTestUIWithLog {

    public static final String ROW_STYLE_GENERATOR_ROW_NUMBERS_FOR_3_OF_4 = "Row numbers for 3/4";
    public static final String ROW_STYLE_GENERATOR_NONE = "None";
    public static final String ROW_STYLE_GENERATOR_ROW_NUMBERS = "Row numbers";
    public static final String ROW_STYLE_GENERATOR_EMPTY = "Empty string";
    public static final String ROW_STYLE_GENERATOR_NULL = "Null";
    public static final String CELL_STYLE_GENERATOR_NONE = "None";
    public static final String CELL_STYLE_GENERATOR_PROPERTY_TO_STRING = "Property to string";
    public static final String CELL_STYLE_GENERATOR_SPECIAL = "Special for 1/4 Column 1";
    public static final String CELL_STYLE_GENERATOR_EMPTY = "Empty string";
    public static final String CELL_STYLE_GENERATOR_NULL = "Null";

    public static final String[] COLUMN_CAPTIONS = { "Column 0", "Column 1",
            "Column 2", "Row Number", "Date", "HTML String", "Big Random",
            "Small Random" };

    private final Command toggleReorderListenerCommand = new Command() {
        private Registration registration = null;

        @Override
        public void menuSelected(MenuItem selectedItem) {
            removeRegistration();
            if (selectedItem.isChecked()) {
                registration = grid.addColumnReorderListener(event -> {
                    List<String> columnCaptions = new ArrayList<>();
                    for (Column<DataObject, ?> column : grid.getColumns()) {
                        columnCaptions.add(column.getCaption());
                    }
                    log("Columns reordered, userOriginated: "
                            + event.isUserOriginated());
                    log("Column order: " + columnCaptions.toString());
                });
                log("Registered a column reorder listener.");
            }
        }

        private void removeRegistration() {
            if (registration != null) {
                registration.remove();
                registration = null;
                log("Removed a column reorder listener.");
            }
        }
    };

    private static class DetailedDetailsGenerator
            implements DetailsGenerator<DataObject> {

        @Override
        public Component apply(DataObject dataObj) {
            VerticalLayout cssLayout = new VerticalLayout();
            cssLayout.setHeight("200px");
            cssLayout.setWidth("100%");

            cssLayout.addComponent(
                    new Label("Row Number: " + dataObj.getRowNumber()));
            cssLayout.addComponent(new Label("Date: " + dataObj.getDate()));
            cssLayout.addComponent(
                    new Label("Big Random: " + dataObj.getBigRandom()));
            cssLayout.addComponent(
                    new Label("Small Random: " + dataObj.getSmallRandom()));

            cssLayout
                    .addComponent(new Button("Press me",
                            e -> Notification.show("You clicked on the "
                                    + "button in the details for " + "row "
                                    + dataObj.getRowNumber())));
            return cssLayout;
        }
    }

    private static class PersistingDetailsGenerator
            implements DetailsGenerator<DataObject> {

        private Map<DataObject, Panel> detailsMap = new HashMap<>();

        @Override
        public Component apply(DataObject dataObj) {
            if (!detailsMap.containsKey(dataObj)) {
                Panel panel = new Panel();
                panel.setContent(new Label("One"));
                detailsMap.put(dataObj, panel);
            }
            return detailsMap.get(dataObj);
        }

        public void changeDetailsComponent(MenuItem item) {
            for (DataObject id : detailsMap.keySet()) {
                Panel panel = detailsMap.get(id);
                Label label = (Label) panel.getContent();
                if (label.getValue().equals("One")) {
                    panel.setContent(new Label("Two"));
                } else {
                    panel.setContent(new Label("One"));
                }
            }
        }
    }

    private Grid<DataObject> grid;
    private Map<String, DetailsGenerator<DataObject>> generators = new LinkedHashMap<>();
    private List<DataObject> data;
    private int watchingCount = 0;
    private PersistingDetailsGenerator persistingDetails;
    private List<Column<DataObject, ?>> initialColumnOrder;

    public GridBasics() {
        generators.put("NULL", null);
        generators.put("Detailed", new DetailedDetailsGenerator());
        generators
                .put("\"Watching\"",
                        dataObj -> new Label("You are watching item id "
                                + dataObj.getRowNumber() + " ("
                                + watchingCount++ + ")"));
        persistingDetails = new PersistingDetailsGenerator();
        generators.put("Persisting", persistingDetails);
    }

    @Override
    protected void setup(VaadinRequest request) {
        data = DataObject.generateObjects();

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setSizeFull();

        // Create grid
        grid = new Grid<>();
        grid.setItems(data);

        grid.addColumn(dataObj -> "(" + dataObj.getRowNumber() + ", 0)")
                .setCaption(COLUMN_CAPTIONS[0]);
        grid.addColumn(dataObj -> "(" + dataObj.getRowNumber() + ", 1)")
                .setCaption(COLUMN_CAPTIONS[1]);
        grid.addColumn(dataObj -> "(" + dataObj.getRowNumber() + ", 2)")
                .setCaption(COLUMN_CAPTIONS[2]);

        grid.addColumn(DataObject::getRowNumber, new NumberRenderer())
                .setCaption(COLUMN_CAPTIONS[3]);
        grid.addColumn(DataObject::getDate, new DateRenderer())
                .setCaption(COLUMN_CAPTIONS[4]);
        grid.addColumn(DataObject::getHtmlString, new HtmlRenderer())
                .setCaption(COLUMN_CAPTIONS[5]);
        grid.addColumn(DataObject::getBigRandom, new NumberRenderer())
                .setCaption(COLUMN_CAPTIONS[6]);
        grid.addColumn(data -> data.getSmallRandom() / 5d,
                new ProgressBarRenderer()).setCaption(COLUMN_CAPTIONS[7]);

        ((SingleSelectionModel<DataObject>) grid.getSelectionModel())
                .addSelectionChangeListener(e -> log("Selected: " + e.getValue()));

        layout.addComponent(createMenu());
        layout.addComponent(grid);
        addComponent(layout);
    }

    private Component createMenu() {
        MenuBar menu = new MenuBar();
        MenuItem componentMenu = menu.addItem("Component", null);
        createStateMenu(componentMenu.addItem("State", null));
        createSizeMenu(componentMenu.addItem("Size", null));
        createDetailsMenu(componentMenu.addItem("Details", null));
        createBodyMenu(componentMenu.addItem("Body rows", null));
        createHeaderMenu(componentMenu.addItem("Header", null));
        createFooterMenu(componentMenu.addItem("Footer", null));
        createColumnsMenu(componentMenu.addItem("Columns", null));
        return menu;
    }

    @SuppressWarnings("unchecked")
    private void createColumnsMenu(MenuItem columnsMenu) {
        for (Column<DataObject, ?> col : grid.getColumns()) {
            MenuItem columnMenu = columnsMenu.addItem(col.getCaption(), null);
            columnMenu.addItem("Move left", selectedItem -> {
                int index = grid.getColumns().indexOf(col);
                if (index > 0) {
                    List<Column<DataObject, ?>> columnOrder = new ArrayList<>(
                            grid.getColumns());
                    Collections.swap(columnOrder, index, index - 1);
                    grid.setColumnOrder(columnOrder
                            .toArray(new Column[columnOrder.size()]));
                }
            });
            columnMenu.addItem("Move right", selectedItem -> {
                int index = grid.getColumns().indexOf(col);
                if (index < grid.getColumns().size() - 1) {
                    List<Column<DataObject, ?>> columnOrder = new ArrayList<>(
                            grid.getColumns());
                    Collections.swap(columnOrder, index, index + 1);
                    grid.setColumnOrder(columnOrder
                            .toArray(new Column[columnOrder.size()]));
                }
            });

            MenuItem headerTypeMenu = columnMenu.addItem("Header Type", null);
            headerTypeMenu.addItem("Text Header", selectedItem -> grid
                    .getDefaultHeaderRow().getCell(col).setText("Text Header"));
            headerTypeMenu.addItem("HTML Header",
                    selectedItem -> grid.getDefaultHeaderRow().getCell(col)
                            .setHtml("<b>HTML Header</b>"));
            headerTypeMenu.addItem("Widget Header", selectedItem -> {
                final Button button = new Button("Button Header");
                button.addClickListener(clickEvent -> log("Button clicked!"));
                grid.getDefaultHeaderRow().getCell(col).setComponent(button);
            });

            columnMenu
                    .addItem("Sortable",
                            selectedItem -> col
                                    .setSortable(selectedItem.isChecked()))
                    .setCheckable(true);
            columnMenu
                    .addItem("Hidable",
                            selectedItem -> col
                                    .setHidable(selectedItem.isChecked()))
                    .setCheckable(true);
            columnMenu
                    .addItem("Hidden",
                            selectedItem -> col
                                    .setHidden(selectedItem.isChecked()))
                    .setCheckable(true);
        }
    }

    private void createSizeMenu(MenuItem sizeMenu) {
        MenuItem heightByRows = sizeMenu.addItem("Height by Rows", null);
        DecimalFormat df = new DecimalFormat("0.00");
        Stream.of(0.33, 0.67, 1.00, 1.33, 1.67, 2.00, 2.33, 2.67, 3.00, 3.33,
                3.67, 4.00, 4.33, 4.67)
                .forEach(d -> addGridMethodMenu(heightByRows,
                        df.format(d) + " rows", d, grid::setHeightByRows));
        sizeMenu.addItem("HeightMode Row", item -> {
            grid.setHeightMode(
                    item.isChecked() ? HeightMode.ROW : HeightMode.CSS);
        }).setCheckable(true);

        MenuItem heightMenu = sizeMenu.addItem("Height", null);
        Stream.of(50, 100, 200, 400).map(i -> i + "px").forEach(
                i -> addGridMethodMenu(heightMenu, i, i, grid::setHeight));
    }

    private void createStateMenu(MenuItem stateMenu) {
        MenuItem frozenColMenu = stateMenu.addItem("Frozen column count", null);
        for (int i = -1; i < 3; ++i) {
            addGridMethodMenu(frozenColMenu, "" + i, i,
                    grid::setFrozenColumnCount);
        }
        createRowStyleMenu(stateMenu.addItem("Row style generator", null));
        createCellStyleMenu(stateMenu.addItem("Cell style generator", null));
        stateMenu.addItem("Row description generator",
                item -> grid.setDescriptionGenerator(item.isChecked()
                        ? t -> "Row tooltip for row " + t.getRowNumber()
                        : null))
                .setCheckable(true);
        stateMenu
                .addItem("Cell description generator", item -> grid.getColumns()
                        .stream().findFirst()
                        .ifPresent(c -> c.setDescriptionGenerator(
                                item.isChecked() ? t -> "Cell tooltip for row "
                                        + t.getRowNumber() + ", Column 0"
                                        : null)))
                .setCheckable(true);
        stateMenu.addItem("Item click listener", new Command() {

            private Registration registration = null;

            @Override
            public void menuSelected(MenuItem selectedItem) {
                removeRegistration();
                if (selectedItem.isChecked()) {
                    registration = grid.addItemClickListener(e -> {
                        grid.setDetailsVisible(e.getItem(),
                                !grid.isDetailsVisible(e.getItem()));
                        log("Item click on row " + e.getItem().getRowNumber()
                                + ", Column '" + e.getColumn().getCaption()
                                + "'");
                    });
                    log("Registered an item click listener.");
                }
            }

            private void removeRegistration() {
                if (registration != null) {
                    registration.remove();
                    registration = null;
                    log("Removed an item click listener.");
                }
            }
        }).setCheckable(true);

        stateMenu.addItem("Column reorder listener",
                toggleReorderListenerCommand).setCheckable(true);

        stateMenu
                .addItem("Column Reordering", selectedItem -> grid
                        .setColumnReorderingAllowed(selectedItem.isChecked()))
                .setCheckable(true);
    }

    private void createRowStyleMenu(MenuItem rowStyleMenu) {
        addGridMethodMenu(rowStyleMenu, ROW_STYLE_GENERATOR_NONE,
                (StyleGenerator<DataObject>) t -> null,
                grid::setStyleGenerator);
        addGridMethodMenu(rowStyleMenu, ROW_STYLE_GENERATOR_ROW_NUMBERS,
                t -> "row" + t.getRowNumber(), grid::setStyleGenerator);
        addGridMethodMenu(rowStyleMenu,
                ROW_STYLE_GENERATOR_ROW_NUMBERS_FOR_3_OF_4,
                t -> t.getRowNumber() % 4 != 0 ? "row" + t.getRowNumber()
                        : null,
                grid::setStyleGenerator);
        addGridMethodMenu(rowStyleMenu, ROW_STYLE_GENERATOR_EMPTY, t -> "",
                grid::setStyleGenerator);
        addGridMethodMenu(rowStyleMenu, ROW_STYLE_GENERATOR_NULL, t -> null,
                grid::setStyleGenerator);
    }

    private void createCellStyleMenu(MenuItem cellStyleMenu) {
        addGridMethodMenu(cellStyleMenu, CELL_STYLE_GENERATOR_NONE,
                (StyleGenerator<DataObject>) t -> null,
                sg -> grid.getColumns().forEach(c -> c.setStyleGenerator(sg)));
        addGridMethodMenu(cellStyleMenu, CELL_STYLE_GENERATOR_EMPTY,
                (StyleGenerator<DataObject>) t -> "",
                sg -> grid.getColumns().forEach(c -> c.setStyleGenerator(sg)));
        addGridMethodMenu(cellStyleMenu,
                CELL_STYLE_GENERATOR_PROPERTY_TO_STRING,
                (StyleGenerator<DataObject>) t -> null,
                sg -> grid.getColumns().forEach(c -> c.setStyleGenerator(
                        t -> c.getCaption().replaceAll(" ", "-"))));
        addGridMethodMenu(cellStyleMenu, CELL_STYLE_GENERATOR_SPECIAL,
                (StyleGenerator<DataObject>) t -> null,
                sg -> grid.getColumns().forEach(c -> c.setStyleGenerator(t -> {
                    if (t.getRowNumber() % 4 == 1) {
                        return null;
                    } else if (t.getRowNumber() % 4 == 3
                            && c.getCaption().equals("Column 1")) {
                        return null;
                    }
                    return c.getCaption().replaceAll(" ", "_");
                })));
        addGridMethodMenu(cellStyleMenu, CELL_STYLE_GENERATOR_NULL,
                (StyleGenerator<DataObject>) t -> null,
                sg -> grid.getColumns().forEach(c -> c.setStyleGenerator(sg)));
    }

    private <T> void addGridMethodMenu(MenuItem parent, String name, T value,
            Consumer<T> method) {
        parent.addItem(name, menuItem -> method.accept(value));
    }

    private void createBodyMenu(MenuItem rowMenu) {
        rowMenu.addItem("Toggle first row selection", menuItem -> {
            DataObject item = data.get(0);
            if (grid.getSelectionModel().isSelected(item)) {
                grid.getSelectionModel().deselect(item);
            } else {
                grid.getSelectionModel().select(item);
            }
        });
    }

    private void createHeaderMenu(MenuItem headerMenu) {
        headerMenu.addItem("Append header row", menuItem -> {
            HeaderRow row = grid.appendHeaderRow();

            int i = 0;
            for (Column<?, ?> column : grid.getColumns()) {
                row.getCell(column).setText("Header cell " + i++);
            }
        });
        headerMenu.addItem("Prepend header row", menuItem -> {
            HeaderRow row = grid.prependHeaderRow();

            int i = 0;
            for (Column<?, ?> column : grid.getColumns()) {
                row.getCell(column).setText("Header cell " + i++);
            }
        });
        headerMenu.addItem("Remove first header row", menuItem -> {
            grid.removeHeaderRow(0);
        });
        headerMenu.addItem("Set first row as default", menuItem -> {
            grid.setDefaultHeaderRow(grid.getHeaderRow(0));
        });
        headerMenu.addItem("Set no default row", menuItem -> {
            grid.setDefaultHeaderRow(null);
        });
    }

    private void createFooterMenu(MenuItem footerMenu) {
        footerMenu.addItem("Add default footer row", menuItem -> {
            FooterRow defaultFooter = grid.appendFooterRow();
            grid.getColumns().forEach(
                    column -> defaultFooter.getCell(column).setText(grid
                            .getDefaultHeaderRow().getCell(column).getText()));
            footerMenu.removeChild(menuItem);
        });
        footerMenu.addItem("Append footer row", menuItem -> {
            FooterRow row = grid.appendFooterRow();

            int i = 0;
            for (Column<?, ?> column : grid.getColumns()) {
                row.getCell(column).setText("Footer cell " + i++);
            }
        });
        footerMenu.addItem("Prepend footer row", menuItem -> {
            FooterRow row = grid.prependFooterRow();

            int i = 0;
            for (Column<?, ?> column : grid.getColumns()) {
                row.getCell(column).setText("Footer cell " + i++);
            }
        });
        footerMenu.addItem("Remove first footer row", menuItem -> {
            grid.removeFooterRow(0);
        });
    }

    /* DetailsGenerator related things */

    private void createDetailsMenu(MenuItem detailsMenu) {
        MenuItem generatorsMenu = detailsMenu.addItem("Generators", null);

        generators.forEach((name, gen) -> generatorsMenu.addItem(name,
                item -> grid.setDetailsGenerator(gen)));

        generatorsMenu.addItem("- Change Component",
                persistingDetails::changeDetailsComponent);

        detailsMenu.addItem("Toggle First", item -> {
            DataObject first = data.get(0);
            openOrCloseDetails(first);
            openOrCloseDetails(first);
        });
        detailsMenu.addItem("Open First", item -> {
            DataObject object = data.get(0);
            openOrCloseDetails(object);
        });
        detailsMenu.addItem("Open 1", item -> {
            DataObject object = data.get(1);
            openOrCloseDetails(object);
        });
        detailsMenu.addItem("Open 995", item -> {
            DataObject object = data.get(995);
            openOrCloseDetails(object);
        });
    }

    private void openOrCloseDetails(DataObject dataObj) {
        grid.setDetailsVisible(dataObj, !grid.isDetailsVisible(dataObj));
    }

}
