package de.codecentric.reedelk.core.component;

import de.codecentric.reedelk.runtime.api.annotation.*;
import de.codecentric.reedelk.runtime.api.component.ProcessorSync;
import de.codecentric.reedelk.runtime.api.converter.ConverterService;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.message.MessageBuilder;
import de.codecentric.reedelk.runtime.api.message.content.MimeType;
import de.codecentric.reedelk.runtime.api.message.content.TypedContent;
import de.codecentric.reedelk.runtime.api.message.content.TypedPublisher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Payload To String")
@ComponentOutput(
        attributes = ComponentOutput.PreviousComponent.class,
        payload = String.class,
        description = "Converts the payload to a string")
@ComponentInput(
        payload = Object.class,
        description = "Any payload input to be converted to a string")
@Description("Transforms the message payload to string type. This component can be used when the payload " +
                "is a byte array or a byte array stream and we want to convert it to a string for further processing. " +
                "This might be necessary for instance when the result of a REST Call does not have a mime type assigned. " +
                "In this case the result will be a byte array and in order to further process the content with a script " +
                "we must convert it to a string type.")
@Component(service = PayloadToString.class, scope = PROTOTYPE)
public class PayloadToString implements ProcessorSync {

    @Property("Mime Type")
    @MimeTypeCombo
    @DefaultValue(MimeType.AsString.TEXT_PLAIN)
    @Example(MimeType.AsString.APPLICATION_JSON)
    @Description("Sets the new mime type of the payload content.")
    private String mimeType;

    @Reference
    private ConverterService converterService;

    private MimeType wantedMimeType;

    @Override
    public void initialize() {
        this.wantedMimeType = MimeType.parse(mimeType, MimeType.TEXT_PLAIN);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        TypedContent<?, ?> content = message.content();
        if (content.isStream()) {
            // Content is not consumed we keep it as a stream
            // by just converting each element emitted by the
            // stream to a string.
            TypedPublisher<?> stream = content.stream();
            TypedPublisher<String> output = converterService.convert(stream, String.class);
            return MessageBuilder.get(PayloadToString.class)
                    .withTypedPublisher(output, wantedMimeType)
                    .attributes(message.attributes())
                    .build();

        } else {
            // Content is consumed.
            Object data = content.data();
            String converted = converterService.convert(data, String.class);
            return MessageBuilder.get(PayloadToString.class)
                    .withString(converted, wantedMimeType)
                    .attributes(message.attributes())
                    .build();
        }
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
