package brix.plugin.site.admin.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import brix.auth.Action.Context;
import brix.jcr.wrapper.BrixNode;
import brix.plugin.site.ManageNodeTabFactory;
import brix.plugin.site.SiteNodePlugin;
import brix.plugin.site.SitePlugin;
import brix.web.tab.CachingAbstractTab;
import brix.web.tab.IBrixTab;

public class ConvertNodeTabFactory implements ManageNodeTabFactory
{

	public List<IBrixTab> getManageNodeTabs(IModel<BrixNode> nodeModel)
	{
		List<IBrixTab> result = new ArrayList<IBrixTab>();

		if (hasEditPermission(nodeModel) && hasConverterForNode(nodeModel))
		{
			result.add(newTab(nodeModel));
		}

		return result;
	}

	private static IBrixTab newTab(final IModel<BrixNode> nodeModel)
	{
		return new CachingAbstractTab(new Model<String>("Convert"), -1)
		{
			@Override
			public Panel newPanel(String panelId)
			{
				return new ConvertTab(panelId, nodeModel);
			}
		};
	};

	private static boolean hasConverterForNode(IModel<BrixNode> nodeModel)
	{
		Collection<SiteNodePlugin> plugins = SitePlugin.get().getNodePlugins();
		BrixNode node = nodeModel.getObject();
		for (SiteNodePlugin plugin : plugins)
		{
			if (plugin.getConverterForNode(node) != null)
			{
				return true;
			}
		}
		return false;
	};

	private static boolean hasEditPermission(IModel<BrixNode> nodeModel)
	{
		return SitePlugin.get().canEditNode(nodeModel.getObject(), Context.ADMINISTRATION);
	}
}