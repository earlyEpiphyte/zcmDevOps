package io.onedev.server.web.component.entity.reference;

import io.onedev.server.util.Referenceable;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class ReferencePanel extends Panel {
	
	public ReferencePanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("referenceHelp") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("title", "通过以下字符串在markdown或提交消息中引用此" + getReferenceable().getType() 
						+ "，如果引用当前项目，可以省略项目名称");
			}
			
		});
		
		String reference = Referenceable.asReference(getReferenceable());
		
		add(new Label("reference", reference));
		add(new CopyToClipboardLink("copy", Model.of(reference)));
	}
	
	protected abstract Referenceable getReferenceable();
}
