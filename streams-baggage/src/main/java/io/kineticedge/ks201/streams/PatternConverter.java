package io.kineticedge.ks201.streams;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternConverter extends BaseConverter<Pattern> {


    public PatternConverter(String optionName) {
        super(optionName);
    }

    public Pattern convert(String value) {

        try {
            return Pattern.compile(value);
        } catch (PatternSyntaxException e) {
            throw new ParameterException(e.getMessage());
        }
    }
}