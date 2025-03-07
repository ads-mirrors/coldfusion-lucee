package lucee.runtime.type.scope.jakarta;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;

/**
 * A JakartaServletFileUpload for {@link DiskFileItem} and {@link DiskFileItemFactory}.
 */
public class JakartaServletDiskFileUpload extends JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> {

	public JakartaServletDiskFileUpload() {
		super(DiskFileItemFactory.builder().get());
	}

	public JakartaServletDiskFileUpload(final DiskFileItemFactory fileItemFactory) {
		super(fileItemFactory);
	}

}