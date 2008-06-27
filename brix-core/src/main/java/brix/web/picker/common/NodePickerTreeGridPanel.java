package brix.web.picker.common;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.tree.TreeModel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import brix.jcr.wrapper.BrixNode;
import brix.web.tree.AbstractTreeModel;
import brix.web.tree.FilteredJcrTreeNode;
import brix.web.tree.JcrTreeNode;
import brix.web.tree.NodeFilter;
import brix.web.util.AbstractModel;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.SizeUnit;
import com.inmethod.grid.column.CheckBoxColumn;
import com.inmethod.grid.column.PropertyColumn;
import com.inmethod.grid.column.tree.AbstractTreeColumn;
import com.inmethod.grid.treegrid.TreeGrid;

public abstract class NodePickerTreeGridPanel<T> extends Panel<T>
{

	public NodePickerTreeGridPanel(String id, IModel<T> model, NodeFilter visibilityFilter, NodeFilter enabledFilter)
	{
		super(id, model);
		this.visibilityFilter = visibilityFilter;		
		this.enabledFilter = enabledFilter != null ? enabledFilter : ALLOW_ALL_FILTER;
	}

	public NodePickerTreeGridPanel(String id, NodeFilter visibilityFilter, NodeFilter enabledFilter)
	{
		super(id);
		
		this.visibilityFilter = visibilityFilter;		
		this.enabledFilter = enabledFilter != null ? enabledFilter : ALLOW_ALL_FILTER;
	}
	
	private final static NodeFilter ALLOW_ALL_FILTER = new NodeFilter() 
	{
		public boolean isNodeAllowed(BrixNode node)
		{
			return true;
		}
	};
	
	public NodeFilter getVisibilityFilter()
	{
		return visibilityFilter;
	}

	@Override
	protected void onBeforeRender()
	{
		if (!hasBeenRendered())
		{
			initComponents();
		}
		expandToSelectedNodes();
		super.onBeforeRender();
	}

	private BrixNode getNode(IModel<?> model)
	{
		Object object = model.getObject();
		if (object instanceof JcrTreeNode)
		{
			IModel<BrixNode> nodeModel = ((JcrTreeNode) object).getNodeModel();
			return nodeModel != null ? nodeModel.getObject() : null;
		}
		else
		{
			return null;
		}
	}
	
	private final NodeFilter visibilityFilter;
	private final NodeFilter enabledFilter;

	private boolean isNodeEnabled(JcrTreeNode node)
	{
		BrixNode n = node.getNodeModel() != null ? node.getNodeModel().getObject() : null;
		return enabledFilter.isNodeAllowed(n);
	}
	
	protected void initComponents()
	{
		grid = new TreeGrid("grid", newTreeModel(), newGridColumns())
		{
			@Override
			protected void onRowClicked(AjaxRequestTarget target, IModel rowModel)
			{
				BrixNode node = getNode(rowModel);
				if (isNodeEnabled((JcrTreeNode) rowModel.getObject()) && node != null)
				{
					if (isItemSelected(rowModel) == false)
					{
						selectItem(rowModel, true);
						onNodeSelected(node);
					}
					else
					{
						selectItem(rowModel, false);
						onNodeDeselected(node);
					}
					update();
				}
			}

			@Override
			protected void onRowPopulated(WebMarkupContainer rowComponent)
			{
				super.onRowPopulated(rowComponent);
				rowComponent.add(new AbstractBehavior()
				{
					@Override
					public void onComponentTag(Component component, ComponentTag tag)
					{
						BrixNode node = getNode(component.getModel());
						if (!isNodeEnabled((JcrTreeNode) component.getModelObject()) || node == null)
						{
							tag.put("class", "disabled");
						}
					}
				});
			}
		};

		configureGrid(grid);		
		add(grid);
	};

	private void expandToNode(JcrTreeNode node)
	{
		boolean first = true;
		while (node != null && node.getNodeModel() != null && node.getNodeModel().getObject() != null)
		{
			BrixNode n = node.getNodeModel().getObject();
			if (!first)
			{
				getGrid().getTreeState().expandNode(node);
			}
			else
			{
				first = false;
			}
			
			if (n.getDepth() > 0)
			{
				node = TreeAwareNode.Util.getTreeNode((BrixNode) n.getParent(), visibilityFilter);	
			}
			else
			{
				node = null;
			}
		}
	}

	protected void expandToSelectedNodes()
	{
		for (IModel<?> model : getGrid().getSelectedItems())
		{
			JcrTreeNode node = (JcrTreeNode) model.getObject();
			expandToNode(node);
		}
	}

	protected void configureGrid(TreeGrid grid)
	{
		grid.getTree().setRootLess(true);
		grid.setClickRowToSelect(true);
		grid.setContentHeight(18, SizeUnit.EM);
	}

	protected void onNodeSelected(BrixNode node)
	{
	}

	protected void onNodeDeselected(BrixNode node)
	{
	}

	private TreeGrid grid;

	public TreeGrid getGrid()
	{
		return grid;
	}

	protected List<IGridColumn> newGridColumns()
	{
		IGridColumn columns[] = {
				new NodePickerCheckBoxColumn("checkbox"),
				new TreeColumn("name", new ResourceModel("name")).setInitialSize(300),
				new NodePropertyColumn(new ResourceModel("type"), "userVisibleType"),
				new DatePropertyColumn(new ResourceModel("lastModified"), "lastModified"),
				new NodePropertyColumn(new ResourceModel("lastModifiedBy"), "lastModifiedBy") };
		return Arrays.asList(columns);
	};

	private class TreeColumn extends AbstractTreeColumn
	{

		public TreeColumn(String columnId, IModel headerModel)
		{
			super(columnId, headerModel);
		}

		@Override
		protected Component newNodeComponent(String id, final IModel model)
		{
			IModel<String> labelModel = new AbstractModel<String>()
			{
				@Override
				public String getObject()
				{
					BrixNode node = getNode(model);
					if (node != null)
					{
						return node.getUserVisibleName();
					}
					else
					{
						return model.getObject().toString();
					}
				}
			};
			return new Label<String>(id, labelModel);
		}
		
		@Override
		public int getColSpan(IModel rowModel)
		{
			BrixNode node = getNode(rowModel);
			return node != null ? 1 : 4;
		}
	};
	
	private class NodePropertyColumn extends PropertyColumn
	{

		public NodePropertyColumn(IModel headerModel, String propertyExpression)
		{
			super(headerModel, propertyExpression);
		}
		
		@Override
		protected Object getModelObject(IModel rowModel)
		{
			return getNode(rowModel);
		}
	};
	
	protected TreeModel newTreeModel()
	{
		return new AbstractTreeModel()
		{
			public JcrTreeNode getRoot()
			{
				return new FilteredJcrTreeNode(getRootNode(), visibilityFilter);
			}
		};
	};

	protected abstract JcrTreeNode getRootNode();

	protected class NodePickerCheckBoxColumn extends CheckBoxColumn
	{

		public NodePickerCheckBoxColumn(String columnId)
		{
			super(columnId);
		}

		@Override
		protected boolean isCheckBoxEnabled(IModel model)
		{
			BrixNode node = getNode(model);
			return isNodeEnabled((JcrTreeNode) model.getObject()) && node != null;
		}

	};

	protected class DatePropertyColumn extends NodePropertyColumn
	{
		public DatePropertyColumn(IModel<?> headerModel, String propertyExpression)
		{
			super(headerModel, propertyExpression);
		}

		@Override
		protected CharSequence convertToString(Object object)
		{
			if (object instanceof Date)
			{
				Date date = (Date) object;
				return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
			}
			else
			{
				return null;
			}
		}
	};

}
