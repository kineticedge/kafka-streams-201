package io.kineticedge.ks201.streams;

import io.kineticedge.ks101.common.config.OptionsUtil;

public class Main {

	public static void main(String[] args) throws Exception{

		final Options options = OptionsUtil.parse(Options.class, args);

		if (options == null) {
			return;
		}

		final Streams stream = new Streams();

		System.out.println(options);
		System.out.println(options.getBootstrapServers());
		stream.start(options);
	}
}

