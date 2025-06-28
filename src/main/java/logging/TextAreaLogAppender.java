package logging;

import ch.qos.logback.core.OutputStreamAppender;
import javafx.application.Platform;
import javafx.util.Pair;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class TextAreaLogAppender<E> extends OutputStreamAppender<E> {

	private final TextAreaOutputStream textAreaOutputStream = new TextAreaOutputStream();
	private static final AtomicBoolean inTimeout = new AtomicBoolean(false);

	public TextAreaLogAppender() {}

	public void setTextArea(StyleClassedTextArea textArea) {
		textAreaOutputStream.setTextArea(textArea);
	}

	@Override
	public void start() {
		setOutputStream(textAreaOutputStream);
		super.start();
	}


	private static class TextAreaOutputStream extends OutputStream {

		private StyleClassedTextArea textArea;
		protected byte[] buf;
		protected int count;

		public TextAreaOutputStream() { this(8192); }

		public TextAreaOutputStream(int size) {
			if (size <= 0) {
				throw new IllegalArgumentException("Buffer size <= 0");
			}
			buf = new byte[size];
		}

		@Override
		public synchronized void write(int b) {
			//if the buffer is full and we are in timeout, wait
			boolean bufferoverflow = false;
			while (count >= buf.length && inTimeout.get()) {
				if (!bufferoverflow) {
					bufferoverflow = true;
					System.err.println("Buffer overflow in TextAreaLogAppender!");
				}
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) { e.printStackTrace(); }
			}
			buf[count++] = (byte) b;

			//check not in timeout and set to timeout
			if (inTimeout.compareAndSet(false, true)) {
				//writeout the buffer
				flush();
				//setup some wait time before the next write
				Runnable runnable = () -> {
					try {
						Thread.sleep(250);
						inTimeout.set(false);
					} catch (InterruptedException e) { e.printStackTrace(); }
				};
				Thread thread = new Thread(runnable);
				thread.start();
			}
		}

		public synchronized void flush() {
			if (textArea == null) return;
			if (count > 0) {
				String text = new String(buf, 0, count);
				count = 0;

				Platform.runLater(() -> {
					for (Pair<String, String> segment : AnsiParser.parse(text)) {
						textArea.append(segment.getKey(), segment.getValue());
					}
				});
				//Platform.runLater(() -> textArea.appendText(text));
			}
		}

		public void setTextArea(StyleClassedTextArea textArea) {
			this.textArea = textArea;
		}

	}

}