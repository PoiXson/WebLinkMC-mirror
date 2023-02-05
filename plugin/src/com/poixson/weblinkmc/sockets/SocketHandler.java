package com.poixson.weblinkmc.sockets;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.poixson.tools.JsonChunker;
import com.poixson.tools.JsonChunker.ChunkProcessor;
import com.poixson.utils.Utils;
import com.poixson.weblinkmc.WebLinkPlugin;
import com.poixson.weblinkmc.api.Request_Online;


public class SocketHandler extends Thread implements Closeable, ChunkProcessor {
	protected static final Logger log = WebLinkPlugin.log;
	protected static final String LOG_PREFIX = WebLinkPlugin.LOG_PREFIX;

	protected final WebLinkPlugin plugin;

	protected final Socket socket;
	protected final PrintWriter   out;
	protected final BufferedReader in;

	protected final JsonChunker buffer;



	public SocketHandler(final WebLinkPlugin plugin, final Socket socket)
			throws IOException {
		this.plugin = plugin;
		this.socket = socket;
		this.out = new PrintWriter(socket.getOutputStream(), true);
		this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.buffer = new JsonChunker(this);
	}



	@Override
	public void run() {
		try {
			char chr;
			while (true) {
				chr = (char) this.in.read();
				if (chr == 0) break;
				this.buffer.process(chr);
			}
		} catch (SocketException ignore) {
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			if ("JSON must start with { bracket".equals(e.getMessage()))
				log.warning(LOG_PREFIX + "Invalid request from: " + this.socket.getRemoteSocketAddress().toString());
		}
		this.plugin.unregister(this);
	}



	@Override
	public void process(final String data) {
		final JsonElement json = JsonParser.parseString(data);
		final Map<String, JsonElement> map = json.getAsJsonObject().asMap();
		if (map.containsKey("request")) {
			final String request = map.get("request").getAsString();
			final String result = this.processRequest(request, map);
			if (Utils.notEmpty(result)) {
				this.out.println(result);
				this.out.flush();
			}
		}
	}
	protected String processRequest(final String request, final Map<String, JsonElement> map) {
		try {
			switch (request) {
			case "online": {
				final Request_Online cmd = new Request_Online(this.plugin);
				return cmd.get(5, TimeUnit.SECONDS);
			}
			case "end":  case "exit":
			case "done": case "quit":
				Utils.SafeClose(this);
				return null;
			default:
				(new RuntimeException("Invalid web-link request: " + request))
					.printStackTrace();
				return null;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return null;
	}



	@Override
	public void close() throws IOException {
		IOException ex = null;
		try {
			this.socket.close();
		} catch (IOException e) {
			ex = e;
		}
		Utils.SafeClose(this.out);
		Utils.SafeClose(this.in);
		if (ex != null)
			throw ex;
	}



}
