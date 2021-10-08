package confdb.diff;

import confdb.data.OutputModule;

/**
 * OutputModuleComparison
 * ------------------
 * @author Philipp Schieferdecker
 *
 */
public class OutputModuleComparison extends Comparison {
	//
	// member data
	//

	/** old output module */
	private OutputModule oldOutputModule = null;

	/** new output module */
	private OutputModule newOutputModule = null;

	/** force to ignore streams */
	private boolean IgnoreStreams = false;

	//
	// construction
	//

	/** standard constructor */
	public OutputModuleComparison(OutputModule oldOutputModule, OutputModule newOutputModule) {
		this.oldOutputModule = oldOutputModule;
		this.newOutputModule = newOutputModule;
	}

	/** standard constructor */
	public OutputModuleComparison(OutputModule oldOutputModule, OutputModule newOutputModule, boolean ignoreStreams) {
		this.IgnoreStreams = ignoreStreams;
		this.oldOutputModule = oldOutputModule;
		this.newOutputModule = newOutputModule;
	}

	//
	// member functions
	//

	/**
	 * result --------------------------------------------------- determine the
	 * result of the comparison NOTE: this method was modified to avoid checking
	 * streams if needed.
	 */
	public int result() {
		if (oldOutputModule == null && newOutputModule != null)
			return RESULT_ADDED;
		else if (oldOutputModule != null && newOutputModule == null)
			return RESULT_REMOVED;
		else if (IgnoreStreams) {
			// IgnoreStreams avoid comparing "comparisonCount" and Streams.
			if (oldOutputModule.name().equals(newOutputModule.name())
					&& oldOutputModule.className().equals(newOutputModule.className())) {
				return RESULT_IDENTICAL;
			} else
				return RESULT_CHANGED;
		} else {
			if (comparisonCount() == 0 && oldOutputModule.name().equals(newOutputModule.name())
					&& oldOutputModule.className().equals(newOutputModule.className())
					&& oldOutputModule.parentStream().name().equals(newOutputModule.parentStream().name()))
				return RESULT_IDENTICAL;
			else
				return RESULT_CHANGED;
		}
	}

	/** plain-text representation of the comparison */
	public String toString() {
		return (newOutputModule == null)
				? oldOutputModule.name() + " [" + oldOutputModule.className() + "] " + resultAsString()
				: newOutputModule.name() + " [" + newOutputModule.className() + "] " + resultAsString();
	}

	/** html representation of the comparison */
	public String toHtml() {
		return (newOutputModule == null)
				? "<html>" + oldOutputModule.className() + ".<b>" + oldOutputModule.name() + "<b></html>"
				: "<html>" + newOutputModule.className() + ".<b>" + newOutputModule.name() + "<b></html>";
	}

}
