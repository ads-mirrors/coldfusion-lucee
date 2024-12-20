package lucee.loader.util.log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lucee.loader.util.Util;

public class Logging {

	public static void dumpThreadPositions(File target) throws IOException {
		StackTraceElement[] stes;
		String line;
		List<StackTraceElement> elements;
		for (Entry<Thread, StackTraceElement[]> e: Thread.getAllStackTraces().entrySet()) {
			stes = e.getValue();
			if (stes == null || stes.length == 0) continue;
			elements = new ArrayList<>();
			for (int i = 0; i < stes.length; i++) {
				// if (stes[i].getLineNumber() > 0) {
				elements.add(stes[i]);
				// }
			}
			if (elements.size() == 0) continue;
			// print.e(stes);
			line = "{\"stack\":[";
			String del = "";
			for (StackTraceElement ste: elements) {
				line += (del + "\"" + ste.getClassName() + "." + (Util.isEmpty(ste.getMethodName(), true) ? "<init>" : ste.getMethodName()) + "():" + ste.getLineNumber() + "\"");
				del = ",";
			}

			line += "],\"thread\":\"" + e.getKey().getName() + "\",\"id\":" + e.getKey().getId() + ",\"time\":" + System.currentTimeMillis() + "}\n";
			Util.write(target, line, Util.UTF8, true);
		}
	}

	public static void startupLog() {
		String dumpPath = Util._getSystemPropOrEnvVar("lucee.dump.threads", null);
		if (!Util.isEmpty(dumpPath, true)) {

			long start = System.currentTimeMillis();

			int tmp = 100;
			try {
				tmp = Integer.parseInt(Util._getSystemPropOrEnvVar("lucee.dump.threads.interval", null));
			}
			catch (Throwable e) {
			}
			final int interval = tmp;

			tmp = 10000;
			try {
				tmp = Integer.parseInt(Util._getSystemPropOrEnvVar("lucee.dump.threads.max", null));
			}
			catch (Throwable e) {
			}
			int max = tmp;

			// Create a new thread to run the task
			Thread thread = new Thread(() -> {
				while (true) {
					try {
						if ((start + max) < System.currentTimeMillis()) {
							break;
						}
						// Call the dumpThreadPositions method
						File target = new File(dumpPath);
						dumpThreadPositions(target);

						// Pause for the specified interval
						if (interval > 0) Util.sleep(interval);
					}
					catch (IOException e) {
						Util.sleep(1000);
					}
				}
			});

			// Start the thread
			thread.start();
		}

	}

}
