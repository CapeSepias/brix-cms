package brix.plugin.site.resource.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import brix.auth.Action.Context;
import brix.jcr.wrapper.BrixNode;
import brix.plugin.site.ManageNodeTabFactory;
import brix.plugin.site.SitePlugin;
import brix.plugin.site.resource.ResourceNodePlugin;
import brix.web.tab.CachingAbstractTab;
import brix.web.tab.IBrixTab;

public class ManageResourceNodeTabFactory implements ManageNodeTabFactory
{

    public List<IBrixTab> getManageNodeTabs(IModel<BrixNode> nodeModel)
    {
        if (ResourceNodePlugin.TYPE.equals(nodeModel.getObject().getNodeType()))
        {
            return getTabs(nodeModel);
        }
        else
        {
            return null;
        }
    }
  
    private static List<IBrixTab> getTabs(final IModel<BrixNode> nodeModel)
    {
        List<IBrixTab> tabs = new ArrayList<IBrixTab>();

        tabs.add(new CachingAbstractTab(new Model<String>("Properties"))
        {
            @Override
            public Panel newPanel(String panelId)
            {
                return new ViewPropertiesTab(panelId, nodeModel);
            }

            @Override
            public boolean isVisible()
            {
                return hasViewPermission(nodeModel);
            }
        });

        return tabs;
    }

    private static boolean hasViewPermission(IModel<BrixNode> model)
    {
    	return SitePlugin.get().canViewNode(model.getObject(), Context.ADMINISTRATION);
    }

}