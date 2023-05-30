package io.kineticedge.ks201.producer;

import com.beust.jcommander.Parameter;
import io.kineticedge.ks101.common.config.BaseOptions;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Options extends BaseOptions {

    @Parameter(names = { "--pause" }, description = "")
    private Long pause = 1000L;

    @Parameter(names = { "--topic" }, description = "")
    private String topic;

}
