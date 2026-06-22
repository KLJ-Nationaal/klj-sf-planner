package logging;

import ch.qos.logback.core.OutputStreamAppender;
import javafx.application.Platform;
import javafx.util.Pair;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TextAreaLogAppender<E> extends OutputStreamAppender<E> {

	private final TextAreaOutputStream textAreaOutputStream = new TextAreaOutputStream();

	public void setTextArea(StyleClassedTextArea textArea) {
		textAreaOutputStream.setTextArea(textArea);
	}

	@Override
	public void start() {
		setOutputStream(textAreaOutputStream);
		super.start();
	}

	@Override
	public void stop() {
		textAreaOutputStream.shutdown();
		super.stop();
	}

	private static class TextAreaOutputStream extends OutputStream {

		private static final int BUF_SIZE = 65536; // 64KB — much more headroom
		private static final int MAX_AREA_CHARS = 20_000_000; // trim if UI text grows too large

		private StyleClassedTextArea textArea;
		private byte[] buf = new byte[BUF_SIZE];
		private int count = 0;

		private final ScheduledExecutorService flusher = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "log-area-flusher");
			t.setDaemon(true);
			return t;
		});

		public TextAreaOutputStream() {
			flusher.scheduleAtFixedRate(this::tryFlush, 100, 100, TimeUnit.MILLISECONDS);
		}

		@Override
		public synchronized void write(int b) {
			if (count < buf.length) {
				buf[count++] = (byte) b;
			}
			// else: silently drop — matches neverBlock semantics of the AsyncAppender
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) {
			// Bulk write — avoids per-byte lock acquisition
			int space = buf.length - count;
			int toCopy = Math.min(len, space);
			System.arraycopy(b, off, buf, count, toCopy);
			count += toCopy;
		}

		@Override
		public void flush() {

		}

		private final AtomicBoolean uiTaskPending = new AtomicBoolean(false);

		private void tryFlush() {
			// If a UI task is already queued, let the buffer keep filling — don't queue another
			if (!uiTaskPending.compareAndSet(false, true)) return;

			final String text;
			synchronized (this) {
				if (count == 0) {
					uiTaskPending.set(false);
					return;
				}
				text = new String(buf, 0, count);
				count = 0;
			}

			// Build off the FX thread
			List<Pair<String, String>> segments = AnsiParser.parse(text);
			StringBuilder sb = new StringBuilder();
			StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
			boolean hasContent = false;
			for (Pair<String, String> segment : segments) {
				String s = segment.getKey();
				if (!s.isEmpty()) {
					sb.append(s);
					spansBuilder.add(Collections.singleton(segment.getValue()), s.length());
					hasContent = true;
				}
			}
			if (!hasContent) {
				uiTaskPending.set(false);
				return;
			}

			final String fullText = sb.toString();
			final StyleSpans<Collection<String>> spans = spansBuilder.create();

			Platform.runLater(() -> {
				try {
					int insertAt = textArea.getLength();
					textArea.appendText(fullText);
					textArea.setStyleSpans(insertAt, spans);
					if (textArea.getLength() > MAX_AREA_CHARS) {
						// Trim to 50% so we don't re-trim every single flush
						textArea.deleteText(0, textArea.getLength() - (MAX_AREA_CHARS / 2));
					}
				} finally {
					uiTaskPending.set(false); // always release, even on exception
				}
			});
		}

		public void shutdown() {
			flusher.shutdown();
		}

		public synchronized void setTextArea(StyleClassedTextArea textArea) {
			this.textArea = textArea;
		}
	}
}