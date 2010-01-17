package pleocmd.itfc.gui;

import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public final class DataSequenceEditorKit extends StyledEditorKit implements
		ViewFactory {

	private static final long serialVersionUID = -7824068672374046824L;

	@Override
	public ViewFactory getViewFactory() {
		return this;
	}

	@Override
	public View create(final Element elem) {
		return new DataSequenceView(elem);
	}

	@Override
	public String getContentType() {
		return "text/datasequence";
	}

}
