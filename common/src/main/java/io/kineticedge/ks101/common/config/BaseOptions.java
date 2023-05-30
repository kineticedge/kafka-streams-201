package io.kineticedge.ks101.common.config;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public abstract class BaseOptions {

    @Parameter(names = "--help", help = true, hidden = true)
    private boolean help;

    @Parameter(names = { "-b", "--bootstrap-servers" }, description = "cluster bootstrap servers")
    private String bootstrapServers = "localhost:19092,localhost:29092,localhost:39092";

}
