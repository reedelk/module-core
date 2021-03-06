package de.codecentric.reedelk.core.component;

import de.codecentric.reedelk.runtime.api.annotation.*;
import de.codecentric.reedelk.runtime.api.commons.AttributesUtils;
import de.codecentric.reedelk.runtime.api.component.Join;
import de.codecentric.reedelk.runtime.api.converter.ConverterService;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.message.MessageAttributes;
import de.codecentric.reedelk.runtime.api.message.MessageBuilder;
import de.codecentric.reedelk.runtime.api.message.content.MimeType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Join With Delimiter")
@ComponentInput(
        payload = Message[].class,
        description = "The messages to join using the given delimiter")
@ComponentOutput(
        attributes = ComponentOutput.PreviousComponent.class,
        payload = String.class,
        description = "The joined content of the input messages payloads using the delimiter as separator. " +
                "If a message does not have a payload of type string, it is converted to string before joining.")
@Description("Can only be placed after a Fork. It joins the payloads of the messages resulting " +
        "from the execution of the Fork with the provided delimiter. " +
        "A delimiter can be a single character or any other string. " +
        "The mime type property specifies the mime type of the joined payloads. " +
        "This component automatically converts the payload of each single input message to string " +
        "in case they are not a string type already.")
@Component(service = JoinWithDelimiter.class, scope = PROTOTYPE)
public class JoinWithDelimiter implements Join {

    @Property("Mime type")
    @MimeTypeCombo
    @DefaultValue(MimeType.AsString.TEXT_PLAIN)
    @Example(MimeType.AsString.APPLICATION_JSON)
    @Description("Sets the mime type of the joined content in the message.")
    private String mimeType;

    @Property("Delimiter")
    @Example(";")
    @InitValue(",")
    @Description("The delimiter char (or string) to be used to join the content of the messages.")
    private String delimiter;

    @Reference
    private ConverterService converterService;

    @Override
    public Message apply(FlowContext flowContext, List<Message> messagesToJoin) {

        // Join with delimiter supports joins of only string data types.
        // This is why we convert the messages payloads into a string type.
        String combinedPayload = messagesToJoin.stream().map(message -> {
            Object messageData = message.payload();
            return converterService.convert(messageData, String.class);
        }).collect(joining(delimiter));

        MimeType parsedMimeType = MimeType.parse(mimeType, MimeType.TEXT_PLAIN);

        MessageAttributes mergedAttributes = AttributesUtils.merge(messagesToJoin);

        return MessageBuilder.get(JoinWithDelimiter.class)
                .withString(combinedPayload, parsedMimeType)
                .attributes(mergedAttributes)
                .build();
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
