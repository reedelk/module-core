package com.reedelk.core.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.MimeTypeUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.resource.ResourceText;
import org.osgi.service.component.annotations.Component;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Resource Read Text")
@Description("Reads a file from the project's resources folder and sets its content into the flow message. " +
                "The type of the message payload is string. This component might be used to load text files (e.g .txt, .json, .xml) " +
                "from the project's resources folder. The Mime Type property assign the mime type of the file to the " +
                "message payload. If Auto Mime Type is selected, the mime type is automatically determined from " +
                "the file extension.")
@Component(service = ResourceReadText.class, scope = PROTOTYPE)
public class ResourceReadText extends ResourceReadComponent implements ProcessorSync {

    @Property("Resource file")
    @Hint("assets/sample.txt")
    @Example("assets/data_model.json")
    @HintBrowseFile("Select Resource Text File ...")
    @Description("The path and name of the file to be read from the project's resources folder.")
    private ResourceText resourceFile;

    @Property("Auto mime type")
    @Example("true")
    @InitValue("true")
    @DefaultValue("false")
    @Description("If true, the mime type of the payload is determined from the extension of the resource read.")
    private boolean autoMimeType;

    @Property("Mime type")
    @MimeTypeCombo
    @Example(MimeType.AsString.APPLICATION_JSON)
    @DefaultValue(MimeType.AsString.TEXT_PLAIN)
    @When(propertyName = "autoMimeType", propertyValue = "false")
    @When(propertyName = "autoMimeType", propertyValue = When.BLANK)
    @Description("The mime type of the resource read from local project's resources directory.")
    private String mimeType;

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        Publisher<String> data = resourceFile.data();

        String resourceFilePath = resourceFile.path();

        MimeType mimeType = MimeTypeUtils.fromFileExtensionOrParse(autoMimeType, resourceFilePath, this.mimeType, MimeType.TEXT_PLAIN);

        Map<String, Serializable> attributes = createAttributes(resourceFilePath);

        return MessageBuilder.get(ResourceReadText.class)
                .attributes(attributes)
                .withString(data, mimeType)
                .build();
    }

    public void setResourceFile(ResourceText resourceFile) {
        this.resourceFile = resourceFile;
    }

    public void setAutoMimeType(boolean autoMimeType) {
        this.autoMimeType = autoMimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
