package com.compare.xsd.renderers;

import com.compare.xsd.model.xsd.XsdNode;
import com.compare.xsd.model.xsd.impl.XsdDocument;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Renders a {@link XsdDocument} within a {@link TreeTableView}.
 */
@EqualsAndHashCode
@ToString
@Getter
public class TreeViewRender implements RenderView {
    private final TreeTableView<XsdNode> treeView;
    private final PropertyViewRender propertyViewRender;

    private XsdDocument document;

    //region Constructors

    /**
     * Initialize a new instance of {@link TreeViewRender}.
     *
     * @param treeView           Set the tree view to use for the rendering.
     * @param propertyViewRender Set the view to render the properties in.
     */
    public TreeViewRender(TreeTableView<XsdNode> treeView, PropertyViewRender propertyViewRender) {
        Assert.notNull(treeView, "treeView cannot be null");
        this.treeView = treeView;
        this.propertyViewRender = propertyViewRender;

        init();
    }

    //endregion

    //region Implementation of RenderView

    /**
     * Get if a document is currently being rendered.
     *
     * @return Returns true if a document is being rendered, else false.
     */
    public boolean isRendering() {
        return this.document != null;
    }

    @Override
    public Node getNode() {
        return this.treeView;
    }

    //endregion

    //region Methods

    /**
     * Refresh the tree view.
     */
    public void refresh() {
        if (isRendering()) {
            this.render(this.document);
        }
    }

    /**
     * Render the given {@link XsdDocument} in the tree view.
     *
     * @param xsdDocument Set the xsd document.
     */
    public void render(XsdDocument xsdDocument) {
        Assert.notNull(xsdDocument, "xsdDocument cannot be null");
        TreeItem<XsdNode> rootItem = new TreeItem<>(xsdDocument);

        renderChildren(xsdDocument.getNodes(), rootItem);

        rootItem.setExpanded(true);
        this.treeView.setRoot(rootItem);
        this.document = xsdDocument;
    }

    public void clear() {
        this.document = null;
        this.treeView.setRoot(null);
    }

    //endregion

    //region Functions

    private void init() {
        treeView.getColumns().clear();

        addNameColumn();
        addColorColumn();
        addTypeColumn();
        addCardinalityColumn();
        addSelectionListener();
    }

    private void renderChildren(List<XsdNode> elements, TreeItem<XsdNode> parent) {
        for (XsdNode element : elements) {
            TreeItem<XsdNode> elementTree = new TreeItem<>(element);

            if (CollectionUtils.isNotEmpty(element.getNodes())) {
                renderChildren(element.getNodes(), elementTree);
            }

            elementTree.setExpanded(true);
            parent.getChildren().add(elementTree);
        }
    }

    private void addNameColumn() {
        TreeTableColumn<XsdNode, String> column = createNewColumn("Name", 350);

        column.setCellValueFactory(cellData -> {
            TreeItem<XsdNode> treeItem = cellData.getValue();
            XsdNode node = treeItem.getValue();

            return new ReadOnlyStringWrapper(node.getName());
        });

        column.setCellFactory(treeColumn -> new TreeTableCell<XsdNode, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                TreeItem<XsdNode> treeItem = getTreeTableRow().getTreeItem();
                XsdNode xsdNode = treeItem != null ? treeItem.getValue() : null;

                setText(empty ? null : item);
                setTooltip(empty ? null : new Tooltip(item));
                setGraphic(empty || xsdNode == null ? null : new ImageView(xsdNode.getIcon()));
            }
        });
    }

    private void addTypeColumn() {
        TreeTableColumn<XsdNode, String> column = createNewColumn("Type", 80);

        column.setCellValueFactory(cellData -> {
            TreeItem<XsdNode> treeItem = cellData.getValue();
            XsdNode node = treeItem.getValue();

            return new ReadOnlyStringWrapper(node.getType());
        });

        column.setCellFactory(treeColumn -> new TreeTableCell<XsdNode, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty ? null : item);
                setTooltip(empty ? null : new Tooltip(item));
            }
        });
    }

    private void addCardinalityColumn() {
        TreeTableColumn<XsdNode, String> column = createNewColumn("Cardinality", 50);

        column.setCellValueFactory(cellData -> {
            TreeItem<XsdNode> treeItem = cellData.getValue();
            XsdNode node = treeItem.getValue();

            return new ReadOnlyStringWrapper(node.getCardinality());
        });

        column.setCellFactory(treeColumn -> new TreeTableCell<XsdNode, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty ? null : item);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void addColorColumn() {
        TreeTableColumn<XsdNode, String> column = createNewColumn("", 20);

        column.setCellFactory(treeColumn -> new TreeTableCell<XsdNode, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                TreeItem<XsdNode> treeItem = getTreeTableRow().getTreeItem();
                XsdNode xsdNode = treeItem != null ? treeItem.getValue() : null;
                Image modificationColor = xsdNode != null ? xsdNode.getModificationColor() : null;

                setGraphic(modificationColor == null ? null : new ImageView(modificationColor));
                setAlignment(Pos.CENTER);
            }
        });
    }

    private TreeTableColumn<XsdNode, String> createNewColumn(String name, double width) {
        TreeTableColumn<XsdNode, String> column = new TreeTableColumn<>(name);

        column.setPrefWidth(width);
        column.setSortable(false);
        column.setEditable(false);

        treeView.getColumns().add(column);

        return column;
    }

    private void addSelectionListener() {
        this.treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.propertyViewRender.render(newValue.getValue());
            }
        });
    }

    //endregion
}
