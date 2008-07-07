package brix.plugin.site.page.tile;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import brix.Brix;
import brix.jcr.wrapper.BrixNode;
import brix.plugin.site.page.PageNode;
import brix.plugin.site.page.TemplateNode;
import brix.plugin.site.page.global.GlobalContainerNode;
import brix.plugin.site.page.tile.admin.TileEditorPanel;
import brix.registry.ExtensionPoint;
import brix.registry.ExtensionPointRegistry;
import brix.web.nodepage.PageParametersAware;
import brix.web.tile.unknown.UnknownTile;

/**
 * Tiles allow to put Wicket components on Brix pages and templates. Tile (class
 * that implements Tile interface) is a singleton that acts like factory for
 * tile editor and viewer panel.
 * <p>
 * When user adds tile to page or template a new node is created that can
 * contain the tile instance configuration options. The node is passed to the
 * {@link #newViewer(String, IModel)} method. The editor can the node passed as
 * argument of the {@link TileEditorPanel#save(BrixNode)} method.
 * 
 * @author Matej Knopp
 * @author ivaynberg
 */
public interface Tile
{
	public static ExtensionPoint<Tile> POINT = new ExtensionPoint<Tile>()
	{
		public brix.registry.ExtensionPoint.Multiplicity getMultiplicity()
		{
			return Multiplicity.COLLECTION;
		}

		public String getUuid()
		{
			return Tile.class.getName();
		}
	};

	/**
	 * Utility class for retrieving registered tiles
	 * 
	 * @author ivaynberg
	 */
	public static class Helper
	{
		public static Collection<Tile> getTiles(Brix brix)
		{
			final ExtensionPointRegistry registry = brix.getConfig().getRegistry();
			return registry.lookupCollection(Tile.POINT);
		}

		public static Tile getTileOfType(String type, Brix brix)
		{
			for (Tile t : getTiles(brix))
			{
				if (t.getTypeName().equals(type))
				{
					return t;
				}
			}
			return UnknownTile.INSTANCE;
		}

	}

	/**
	 * Creates a new viewer component for this tile. The viewer will be placed
	 * at appropriate place to page or template (specified by the use of
	 * &lt;brix:tile&gt; tag).
	 * 
	 * @see PageParametersAware
	 * 
	 * @param id
	 *            component id
	 * @param tileNode
	 *            node that contains tile configuration options
	 * @return tile viewer component
	 */
	Component newViewer(String id, IModel<BrixNode> tileNode);

	/**
	 * Creates a new editor panel for this tile. The editor panel will be shown
	 * in administration interface.
	 * 
	 * @param id
	 *            component id
	 * @param tileContainerNode
	 *            Node that will contain (or already contains) the tile. Can be
	 *            either {@link PageNode}, {@link TemplateNode} or
	 *            {@link GlobalContainerNode}
	 * @return tile editor component
	 */
	TileEditorPanel newEditor(String id, IModel<BrixNode> tileContainerNode);

	/**
	 * Returns the user readable tile name.
	 * 
	 * @return
	 */
	String getDisplayName();

	/**
	 * Return a string uniquely identifying tile type.
	 * 
	 * @return
	 */
	String getTypeName();

	/**
	 * Returns true if this tile requires use SSL. Can be useful for e.g. login
	 * tile.
	 * 
	 * @param tileNode
	 * @return
	 */
	boolean requiresSSL(IModel<BrixNode> tileNode);
}