package io.kineticedge.ks201.streams;

import com.beust.jcommander.Parameter;
import io.kineticedge.ks101.common.config.BaseOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@ToString(callSuper = true)
@Getter
@Setter
public class Options extends BaseOptions  {

    @Parameter(names = { "-g", "--application-id" }, description = "application id")
    private String applicationId = "baggage-claim";

    @Parameter(names = { "--client-id" }, description = "client id")
    private String clientId = "s-" + UUID.randomUUID();

    @Parameter(names = { "--group-instance-id" }, description = "group instance id")
    private String groupInstanceId;

    @Parameter(names = { "--auto-offset-reset" }, description = "where to start consuming from if no offset is provided")
    private String autoOffsetReset = "earliest";

    // one of the following 3 must be given (and only one)

    @Parameter(names = { "--input" }, description = "")
    private String input;

    @Parameter(names = { "--inputs" }, description = "")
    private List<String> inputs;

    @Parameter(names = { "--input-pattern" }, description = "", converter = PatternConverter.class)
    private Pattern inputPattern;

    @Parameter(names = { "--output" }, description = "")
    private String output;

}
