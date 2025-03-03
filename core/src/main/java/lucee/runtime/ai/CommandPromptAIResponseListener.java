package lucee.runtime.ai;

import java.io.PrintStream;

public class CommandPromptAIResponseListener implements AIResponseListener {

	public static short OUT = 1;
	public static short ERR = 2;
	private short streamType;

	public CommandPromptAIResponseListener(short streamType) {
		this.streamType = streamType;
	}

	@Override
	public void listen(String part, int chunkIndex, boolean isComplete) {
		PrintStream stream = streamType == OUT ? System.err : System.err;

		// stream.print("-------------- index:" + chunkIndex + ";complete?" + isComplete + "
		// --------------");
		stream.print(part);
	}
}
