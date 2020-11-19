package net.securustech.ews.data.elasticsearch.rest.extension.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

import java.util.Map;

@Getter
@Setter
public class EsDocumentCommon {
    public static final String DOC_TYPE_NAME = "docTypeNm";

    private String docTypeNm;
    private String createdBy;
    private String createdDt;
    private String modifiedBy;
    private String modifiedDt;

    public void buildCommon(EsDocumentBuilder builder) {
        Assert.notNull(docTypeNm, "Doc type name is null");

        builder.add(DOC_TYPE_NAME, docTypeNm);
        if (createdBy != null)
            builder.add("createdBy", createdBy);
        if (createdDt != null)
            builder.add("createdDt", createdDt);
        if (modifiedBy != null)
            builder.add("modifiedBy", modifiedBy);
        if (modifiedDt != null)
            builder.add("modifiedDt", modifiedDt);
    }
}
