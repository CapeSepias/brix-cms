package brix.plugin.site.page.tile.admin;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import brix.jcr.wrapper.BrixNode;

public abstract class TileEditorPanel extends Panel
{

    public TileEditorPanel(String id)
    {
        super(id);
    }

    public TileEditorPanel(String id, IModel<?> model)
    {
        super(id, model);
    }

    abstract public void load(BrixNode node);

    abstract public void save(BrixNode node);
}