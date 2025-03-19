package lucee.runtime.type.scope.jakarta;

import org.apache.commons.io.FileCleaningTracker;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * A servlet context listener, which ensures that the {@link FileCleaningTracker}'s reaper thread is
 * terminated, when the web application is destroyed.
 */
public final class JakartaFileCleaner implements ServletContextListener {

	/**
	 * Attribute name, which is used for storing an instance of {@link FileCleaningTracker} in the web
	 * application.
	 */
	public static final String FILE_CLEANING_TRACKER_ATTRIBUTE = JakartaFileCleaner.class.getName() + ".FileCleaningTracker";

	/**
	 * Gets the instance of {@link FileCleaningTracker}, which is associated with the given
	 * {@link ServletContext}.
	 *
	 * @param servletContext The servlet context to query
	 * @return The contexts tracker
	 */
	public static FileCleaningTracker getFileCleaningTracker(final ServletContext servletContext) {
		return (FileCleaningTracker) servletContext.getAttribute(FILE_CLEANING_TRACKER_ATTRIBUTE);
	}

	/**
	 * Sets the instance of {@link FileCleaningTracker}, which is associated with the given
	 * {@link ServletContext}.
	 *
	 * @param servletContext The servlet context to modify
	 * @param tracker The tracker to set
	 */
	public static void setFileCleaningTracker(final ServletContext servletContext, final FileCleaningTracker tracker) {
		servletContext.setAttribute(FILE_CLEANING_TRACKER_ATTRIBUTE, tracker);
	}

	/**
	 * Called when the web application is being destroyed. Calls
	 * {@link FileCleaningTracker#exitWhenFinished()}.
	 *
	 * @param sce The servlet context, used for calling {@link #getFileCleaningTracker(ServletContext)}.
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		getFileCleaningTracker(sce.getServletContext()).exitWhenFinished();
	}

	/**
	 * Called when the web application is initialized. Does nothing.
	 *
	 * @param sce The servlet context, used for calling
	 *            {@link #setFileCleaningTracker(ServletContext, FileCleaningTracker)}.
	 */
	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		setFileCleaningTracker(sce.getServletContext(), new FileCleaningTracker());
	}
}
